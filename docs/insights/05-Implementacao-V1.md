# Insights V1 — implementação no código

Este arquivo descreve **o que foi implementado** no backend: casos de uso, decisões, camadas, cálculos (em português), contrato HTTP, exceções, seed e limitações.  
Complementa os docs 01–04 (visão, lotes, fórmulas, plano).

---

## 1. O que é

O módulo `insights` monta um **briefing do dia** para o dono: números e listas, sem texto de marketing e sem LLM.

Ele **não** altera estoque, preço nem vendas. Só lê:

- vendas `ENTREGUE` (por loja ou todas);
- produtos ativos e composição (receita);
- saldo atual dos insumos;
- lotes abertos com validade.

Pré-requisito no `stock`: **lotes + FEFO** (migration `V17`), senão não existe alerta honesto de validade.

---

## 2. Decisões que guiaram o código

| Decisão | O que fizemos | Por quê |
|---------|----------------|---------|
| Um endpoint agregado | `GET .../insights` devolve demanda + compra + crítico + validade + promoção | É um relatório do dia, não quatro CRUDs. As seções dependem da mesma previsão. |
| Quebra interna | Quatro calculadoras estáticas + service orquestrador | Testável sem banco; HTTP continua único. |
| Portas + adapters | Insights não importa entidades JPA de product/sales/stock no service | Se o SQL mudar, a regra de média não muda. |
| Demanda por loja | Path `/api/stores/{storeId}/insights` | Padrão de venda pode diferir entre unidades. |
| Visão global provisória | `GET /api/insights` (TODO no controller) | Mesmo espírito de `GET /api/stock-items`. Remover quando o front usar sempre `storeId`. |
| Estoque compartilhado | Compra/crítico/lotes usam o pool único | Modelo atual não tem depósito por loja. O JSON avisa em `assumptions`. |
| Autenticação | Qualquer JWT válido (cookie `token`) | Igual ao stock na V1. `ROLE_ADMIN` fica para depois (`docs/Autorizacao-por-Roles.md`). |
| Previsão | Média do mesmo weekday, 8 semanas, **incluindo zero** | Explicável na banca; evita inflar a média só com dias que venderam. |
| Seed | Script no terminal, **não** Flyway | Demo reexecutável; histórico de venda não baixa estoque (projeto acadêmico). |

---

## 3. Casos de uso

### UC1 — Briefing da loja (principal)

**Ator:** usuário autenticado.  
**Fluxo:** `GET /api/stores/{storeId}/insights`  
**Resultado:** JSON com previsão **só das vendas daquela loja** e estoque **global**.

Usar na tela do dono da unidade. Query params são opcionais (ver §6).

### UC2 — Briefing global (provisório)

**Ator:** usuário autenticado.  
**Fluxo:** `GET /api/insights`  
**Resultado:** mesma forma, `storeId = null`, nome `"Todas as lojas"`, demanda = soma das vendas confirmadas de **todas** as lojas. Estoque continua o mesmo pool.

Útil enquanto o front lista “tudo” como no stock. Comentário TODO no `InsightsController`.

### UC3 — Simular outro dia / outras janelas (demo)

Mesmos GETs com:

- `date=YYYY-MM-DD` — briefing “como se hoje fosse X” (o seed dummy vende terça e sexta);
- `horizonDays` — para quantos dias a **compra** deve cobrir (não muda a previsão do prato);
- `expiryWindowDays` — até quantos dias à frente listar lotes a vencer.

### UC4 — Entrada de estoque com validade (stock)

**Ator:** usuário autenticado.  
**Fluxo:** movimentação `ENTRADA` com `expiresAt` obrigatório.  
**Resultado:** cria `stock_lot`; saldo do item sobe.

Sem validade → `MissingLotExpiryException` (handler de stock: 400 **se** o advice do stock ganhar; ver §8).

### UC5 — Saída / venda baixa FEFO (stock)

Baixa primeiro o lote que **vence antes**; empate pelo `id` do lote. Sem quantidade suficiente nos lotes → `InsufficientStockException`.

### UC6 — Consultar lotes de um insumo

`GET /api/stock-items/{id}/lots` — lista lotes do item (validade, quantidade).

### UC7 — Popular demo acadêmica

```bash
./scripts/seed-insights-demo.sh
```

Cria/atualiza loja 2, vendas `INSIGHTS_DEMO`, estoque baixo e lotes curtos (pão, queijo, bacon, sorvete). Pode rodar de novo.

---

## 4. Como o código está organizado

```text
HTTP          InsightsController  →  InsightsResponse
Caso de uso   DailyInsightsService → DailyInsightsOutput
Regras        DemandForecastCalculator
              PurchaseSuggestionCalculator
              CriticalStockCalculator
              ExpiryPromotionCalculator
Portas        ProductCatalog, RecipeCatalog, StockSnapshot,
              ConfirmedSalesHistory, StoreCatalog
Infra         *Adapter + InsightsSaleQueryRepository (SQL nativo)
```

Analogia: o **controller** é a view; o **service** é o roteiro; as **portas** são “o que preciso buscar”; os **adapters** traduzem JPA/SQL para records simples; as **calculadoras** são funções puras.

`Clock` (`TimeConfig`, fuso `America/Sao_Paulo`) define “hoje” quando `date` não vem.

Configuração (`application.properties`):

- `app.insights.forecast-weeks=8`
- `app.insights.min-samples-high=6` / `min-samples-medium=3`
- `app.insights.expiry-window-days=3`
- `app.insights.horizon-days=1`

---

## 5. Cálculos (em português)

### Demanda (`productDemand`)

1. Pega o dia de referência (`date` ou hoje).
2. Lista as N semanas anteriores **no mesmo dia da semana** (ex.: 8 terças). O próprio dia alvo **não entra**.
3. Para cada produto **ativo**, soma o que vendeu nessas datas (venda `ENTREGUE`). Dia sem venda conta **zero**.
4. Divide pelo número de semanas e arredonda para inteiro → `predictedQuantity`.
5. Confiança: HIGH se há ≥ 6 semanas com sinal; MEDIUM se ≥ 3; senão LOW. Se **não vendeu nada** na janela, previsão 0, `sampleSize` 0, LOW.

Por loja: só `store_id` da URL. Global: todas as lojas no mesmo agrupamento dia+produto.

### Compra (`purchaseSuggestions`)

1. Para cada linha da receita (produto → insumo × quantidade por unidade), multiplica a previsão do prato × quantidade da receita × `horizonDays`.
2. Soma o consumo de todos os pratos que usam o mesmo insumo.
3. `quantityToBuy` = o que falta para cobrir esse consumo (nunca negativo).
4. `belowMinimum` = saldo atual ≤ mínimo cadastrado (independente da previsão).
5. `affectedProductIds` = pratos do **cardápio** que usam o insumo (não só os que a loja vendeu no seed).

A lista inclui **todos** os insumos ativos; na UI filtre `quantityToBuy > 0` se quiser só “o que comprar”.

### Crítico (`criticalStock`)

Insumo entra se saldo ≤ mínimo. **Não** usa previsão. Por isso pode aparecer crítico com `quantityToBuy = 0` (e o contrário).

### Lotes e promoção

Lote entra se quantidade > 0 e a validade é até `targetDate + expiryWindowDays` (inclui já vencido; `daysUntilExpiry` pode ser negativo).

Ordenação: vence primeiro, depois `lotId`.

Promoção: para cada lote na janela, lista produtos da receita que usam aquele insumo, do que **mais vende nesta visão** para o de menor demanda. Sem copy de campanha.

---

## 6. Contrato HTTP

Autenticação: cookie **`token`** (JWT). Sem cookie → **401**.

### Requests

Tela normal:

```http
GET /api/stores/1/insights
Cookie: token=<jwt>
```

Demo (terça do seed):

```http
GET /api/stores/1/insights?date=2026-10-06
GET /api/stores/2/insights?date=2026-10-06
GET /api/insights?date=2026-10-06
GET /api/stores/1/insights?date=2026-10-06&horizonDays=2&expiryWindowDays=7
```

### Campos da response

| Campo | Significado |
|-------|-------------|
| `storeId` / `storeName` | Loja; no global, `null` / `"Todas as lojas"` |
| `targetDate` / `weekday` | Dia usado no cálculo |
| `horizonDays` / `expiryWindowDays` | Valores efetivos (query ou default) |
| `assumptions` | Estoque `GLOBAL_SHARED`; demanda `STORE` ou `ALL_STORES`; método `WEEKDAY_AVERAGE`; semanas; nota |
| `productDemand` | Previsão por produto ativo |
| `purchaseSuggestions` | Consumo previsto e quanto comprar por insumo |
| `criticalStock` | Abaixo ou no mínimo |
| `expiringLots` | Lotes na janela |
| `promotionSuggestions` | Pratos a empurrar por lote |

### Exemplo resumido (loja 1, terça, seed atual)

Demanda: Classic 18, Bacon Supreme 10.  
Compra: pão 20, queijo 16, bacon 280.  
Crítico: pão, queijo, bacon, sorvete.  
Lotes: bacon (já vencido no alvo 06/10) e queijo (vence no alvo).  
Promoção: Bacon Supreme e Classic Burger.

---

## 7. Exceções HTTP

### Insights (`InsightsExceptionHandler`)

| Situação | Código | Body |
|----------|--------|------|
| `storeId` inexistente | **404** | `Store not found: {id}` |
| `date` (ou outro param) malformado | **400** | data: `Invalid date. Use ISO format YYYY-MM-DD.` |

O `GlobalExceptionHandler` fica com `@Order(LOWEST_PRECEDENCE)` e os handlers de módulo (`InsightsExceptionHandler`, `StoreExceptionHandler`) com `@Order(HIGHEST_PRECEDENCE)`. Sem isso o catch-all de `RuntimeException` do global era escolhido primeiro e virava 500.

### Stock (lotes)

| Situação | Código |
|----------|--------|
| Item/estoque não encontrado | 404 |
| ENTRADA sem `expiresAt` | 400 (`MissingLotExpiryException`) |
| Lotes insuficientes na baixa FEFO | 409 (`InsufficientStockException`) |
| Quantidade inválida | 400 |

### Segurança

| Situação | Código |
|----------|--------|
| Sem JWT / cookie inválido | **401** |

Não há **403** por role nesta V1.

---

## 8. Seed dummy

Arquivos: `scripts/seed-insights-demo.sql` + `scripts/seed-insights-demo.sh`.

- Vendas: `observation = 'INSIGHTS_DEMO'` (apagadas e reinseridas a cada run). Loja 1: Classic + Bacon Supreme (terça/sexta). Loja 2: Crispy + Milkshake.
- Estoque demo (reescrito a cada run): pão 8, queijo 20, bacon 120, sorvete 800, com mínimos altos o bastante para crítico; lotes de queijo/bacon na janela de 3 dias.
- Vendas **não** disparam baixa de estoque.

Para ver **todas** as seções na UI: chame com `date` numa terça ou sexta coberta pelo histórico (ex. `2026-10-06`). No domingo “hoje”, demanda zera e crítico/validade ainda aparecem.

Login local: o hash do admin na migration antiga **não** traz prefixo `{bcrypt}`; o `DelegatingPasswordEncoder` pode rejeitar. Ajuste pontual no banco se o login falhar (não faz parte do módulo insights).

---

## 9. Testes

Unidade (calculadoras, `DailyInsightsService` com fakes, `LotLedger`, `StockItemService` lot-aware).

A API foi conferida de ponta a ponta: JSON vs vendas do seed vs composição vs saldo — os números fecham.

`DinnerApplicationTests` (contexto Spring + Cloudinary) não faz parte desta entrega.

---

## 10. O que não fazemos de propósito

- ML, clima, feriado, peso maior em semanas recentes.
- Estoque ou cardápio **por loja**.
- Criar promoção ou alterar preço.
- Texto gerado por IA.
- Filtrar `purchaseSuggestions` só com `quantityToBuy > 0` (fica o front ou uma evolução).
- Dimensionar “venda N unidades para zerar o lote”.

---

## 11. Arquivos-chave

| Área | Onde |
|------|------|
| HTTP insights | `insights/infrastructure/http/controller/InsightsController.java` |
| Orquestração | `insights/application/service/DailyInsightsService.java` |
| Calculadoras | `insights/domain/*Calculator.java` |
| SQL de vendas | `InsightsSaleQueryRepository` |
| Lotes | `stock/domain/StockLot.java`, `LotLedger.java`, `V17__create_stock_lots.sql` |
| Seed | `scripts/seed-insights-demo.sh` |

Leitura complementar: [README](README.md), [01](01-Visao-Geral-e-Decisoes.md), [02](02-Lotes-e-FEFO.md), [03](03-Calculos-e-API.md), [modelo loja/estoque](../modelo/Loja-Estoque-e-Cardapio.md).
