# Insights — cálculos, confiança e API

Este documento explica **como** cada número é obtido, com **exemplo didático**, e descreve o contrato HTTP. Implementação em Java puro nas calculadoras; sem LLM.

Pré-requisitos: [modelo loja/estoque](../modelo/Loja-Estoque-e-Cardapio.md), [lotes](02-Lotes-e-FEFO.md).

---

## 1. Endpoint principal

```http
GET /api/stores/{storeId}/insights
```

Visão **por loja** (fonte principal).

```http
GET /api/insights
```

TODO: listagem **global** provisória — remover quando o front passar a usar o `storeId` (mesmo padrão de `GET /api/stock-items`).  
A demanda soma vendas `ENTREGUE` de **todas** as lojas; o estoque continua compartilhado.

**Autenticação:** cookie JWT (como o resto da API) — qualquer usuário autenticado na V1.  
**Autorização por role:** pendente; ver [Autorizacao-por-Roles.md](../Autorizacao-por-Roles.md).

### Query params opcionais (demo / testes)

| Param | Default | Uso |
|---|---|---|
| `date` | hoje (`America/Sao_Paulo`) | Simular “briefing do dia X” |
| `expiryWindowDays` | `3` | Lotes que vencem até `date + N` |
| `horizonDays` | `1` | Horizonte da demanda (V1 = “hoje”) |

---

## 2. Configuração (`application.properties`)

```properties
app.insights.forecast-weeks=8
app.insights.min-samples-high=6
app.insights.min-samples-medium=3
app.insights.expiry-window-days=3
app.insights.horizon-days=1
app.insights.timezone=America/Sao_Paulo
```

| Propriedade | Significado |
|---|---|
| `forecast-weeks` | Quantas semanas olhar para trás |
| `min-samples-high` | Se ≥ N weekdays com dados na janela → confiança HIGH |
| `min-samples-medium` | Se ≥ N mas < high → MEDIUM; senão LOW |
| `expiry-window-days` | Janela de validade para alertas |
| `horizon-days` | Dias à frente para consumo previsto (V1: 1) |
| `timezone` | Define “hoje” e weekday |

Injetar `java.time.Clock` nos services para testes fixarem a data.

---

## 3. Previsão de demanda por produto

### 3.1 Método: `WEEKDAY_AVERAGE`

**Ideia:** “Toda terça-feira, quantas unidades deste produto esta loja costuma vender?”

**Passos:**

1. `targetDate` = data de referência (ex. terça, 31/03/2026).
2. `weekday` = dia da semana de `targetDate`.
3. Listar todas as datas `d` nos últimos `forecast-weeks` semanas onde `d` tem o **mesmo weekday** que `targetDate`.
4. Para cada produto ativo e cada data `d`, somar unidades vendidas na loja em vendas `ENTREGUE` (via `sale_items`).
5. **Média** desses valores (incluir **zero** nos dias em que não houve venda daquele produto).

**Por que incluir zero?**  
Se você só média “dias em que vendeu”, uma terça com 50 burgers e outra sem venda viram “50” em vez de “25”. Incluir zeros é mais conservador e honesto com poucos dados.

### 3.2 Exemplo numérico

Loja 1, produto **Classic Burger** (id=1), `forecast-weeks=4`, `targetDate` = terça.

| Terça (histórico) | Unidades vendidas (ENTREGUE) |
|---|---|
| T-7 dias | 20 |
| T-14 dias | 0 |
| T-21 dias | 16 |
| T-28 dias | 24 |

```text
predictedQuantity = (20 + 0 + 16 + 24) / 4 = 15
```

Resposta por produto:

```json
{
  "productId": 1,
  "productName": "Classic Burger",
  "predictedQuantity": 15,
  "method": "WEEKDAY_AVERAGE",
  "sampleSize": 4,
  "confidence": "MEDIUM"
}
```

`sampleSize` = quantidade de weekdays na janela (aqui 4).  
`confidence` conforme limites `min-samples-high` / `min-samples-medium`.

Produto sem nenhuma venda na janela: `predictedQuantity: 0`, `confidence: "LOW"`, `sampleSize: 0` (ou N com zeros).

---

## 4. Compras sugeridas (insumos)

### 4.1 Propagação pela composição

Para cada produto `p` com previsão `Qp` e composição `(stock_item_id → qty por unidade)`:

```text
consumo_previsto(item) += Qp × qty_composição(p, item)
```

Some para **todos** os produtos ativos com previsão.

### 4.2 Gap de compra

```text
quantityToBuy = max(0, consumo_previsto - currentQuantity)
```

Também marcar `belowMinimum = (currentQuantity <= minimumStock)` mesmo quando `quantityToBuy == 0`.

### 4.3 Exemplo

Previsão hoje:

- Classic Burger: **15** unidades  
- Composição: 1 pão (item 1) por burger  

```text
consumo pão = 15 × 1 = 15
currentQuantity pão = 8
quantityToBuy = max(0, 15 - 8) = 7
```

Se `minimumStock` do pão = 10 → `belowMinimum = true` (alerta crítico separado).

Item na resposta:

```json
{
  "stockItemId": 1,
  "name": "Pão Brioche Artesanal",
  "unit": "UNIDADE",
  "currentQuantity": 8,
  "minimumStock": 10,
  "predictedConsumption": 15,
  "quantityToBuy": 7,
  "belowMinimum": true,
  "affectedProductIds": [1]
}
```

Ordenação sugerida: maior `quantityToBuy` primeiro; depois itens só `belowMinimum`.

**Lembrete:** saldo é **global** na V1; a demanda que alimenta o cálculo é **da loja** do path.

---

## 5. Estoque crítico

Regra simples, independente da previsão:

```text
crítico se currentQuantity <= minimumStock
```

Para cada item crítico, listar produtos ativos cuja composição usa esse item (para o dono saber **o que para de sair** se faltar insumo).

---

## 6. Lotes a vencer

Com lotes implementados ([02-Lotes-e-FEFO.md](02-Lotes-e-FEFO.md)):

```text
alerta se expires_at <= targetDate + expiryWindowDays
     e quantity > 0
```

Cada entrada:

- `lotId`, `stockItemId`, nome do item, `quantity`, `expiresAt`, `daysUntilExpiry`

---

## 7. Sugestão de promoção (sem texto de marketing)

**Não** geramos copy (“Compre já!”). Só **lista ordenada de produtos**.

Para cada lote na janela de validade:

1. Encontrar produtos ativos que usam aquele `stock_item` na composição.
2. Ordenar por `predictedQuantity` **da loja** (desc) — empurra o que a unidade mais vende.
3. Desempate: preço do produto (desc) ou id.

Exemplo:

- Lote de **Queijo Prato** vence em 2 dias.  
- Produtos: Classic Burger (prev 15), Bacon Supreme (prev 8).  
- Lista: Classic Burger primeiro, depois Bacon Supreme.

```json
{
  "stockItemId": 3,
  "lotId": 42,
  "expiresAt": "2026-04-02",
  "quantityInLot": 30,
  "suggestedProducts": [
    { "productId": 1, "productName": "Classic Burger", "predictedQuantityToday": 15 },
    { "productId": 2, "productName": "Bacon Supreme", "predictedQuantityToday": 8 }
  ]
}
```

---

## 8. Resposta agregada (esboço completo)

```json
{
  "storeId": 1,
  "storeName": "Orderly Centro",
  "targetDate": "2026-03-31",
  "weekday": "TUESDAY",
  "horizonDays": 1,
  "expiryWindowDays": 3,
  "assumptions": {
    "stockScope": "GLOBAL_SHARED",
    "demandScope": "STORE",
    "forecastMethod": "WEEKDAY_AVERAGE",
    "forecastWeeks": 8,
    "note": "Saldo de insumos é compartilhado entre lojas; demanda calculada só desta loja."
  },
  "productDemand": [ /* ... */ ],
  "purchaseSuggestions": [ /* ... */ ],
  "criticalStock": [ /* ... */ ],
  "expiringLots": [ /* ... */ ],
  "promotionSuggestions": [ /* ... */ ]
}
```

O bloco `assumptions` é importante para **você** e para a **banca** não interpretarem o sistema como multi-estoque por loja antes da evolução de modelo.

---

## 9. Sub-rotas opcionais

Se o front preferir carregar em partes (mesma lógica, mesmo service):

| Rota | Conteúdo |
|---|---|
| `GET .../insights/demand` | só `productDemand` |
| `GET .../insights/purchases` | só `purchaseSuggestions` + `criticalStock` |
| `GET .../insights/expiry` | `expiringLots` + `promotionSuggestions` |

Não é obrigatório na primeira entrega se o agregado único bastar.

---

## 10. Erros HTTP

| Situação | Código |
|---|---|
| Loja inexistente | 404 |
| `date` inválida | 400 |
| Não autenticado | 401 |
| Sem `ROLE_ADMIN` (quando roles forem implementadas) | 403 — ver [Autorizacao-por-Roles.md](../Autorizacao-por-Roles.md) |

---

## 11. O que não fazemos (de propósito)

- Previsão com clima, feriado ou ML.
- Texto gerado por IA.
- Alterar preço ou criar promoção no sistema — só **sugestão** em JSON.

Próximo: [04 — Plano de implementação](04-Plano-de-Implementacao.md).
