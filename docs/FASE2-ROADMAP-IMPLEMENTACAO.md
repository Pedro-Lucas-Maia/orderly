# ROADMAP DE IMPLEMENTAÇÃO — ORDERLY FRAMEWORK (FASE 2)

> **Documento de Engenharia e Plano de Execução**  
> **Disciplina:** DIM0138 — Projeto Detalhado de Software (DIMAp / UFRN)  
> **Docente:** Prof. Dr. Uirá Kulesza  
> **Autores:** Glauco Sales Paiva Virginio, Pedro Lucas Maia de Paiva, Yuri Gabriel de Macedo Machado  
> **Escopo:** Transformação do sistema Orderly (Fase 1) em um Framework Orientado a Domínio (*Make-to-Order*) com suporte a 3 instâncias de aplicação.

---

## 1. VISÃO GERAL DA ARQUITETURA ALVO

O projeto é mantido em um **repositório único**, estruturado como um **projeto Multi-Módulos Maven (`Maven Multi-Module`)**.

```text
orderly/                              <-- Raiz do projeto (POM Pai agregador)
│
├── orderly-framework/                 <-- MÓDULO CORE: O FRAMEWORK (JAR de Biblioteca)
│   ├── src/main/java/                 <-- Pontos Fixos (Entidades, Template Methods, DTOs, Repositórios)
│   │   └── bti/pds/dinner/
│   │       ├── stock/                 <-- Domínio de estoque + Hot Spot LotAllocationStrategy
│   │       ├── sales/                 <-- Domínio de vendas + Hot Spots OrderPricingStrategy & SaleConsumptionStrategy
│   │       ├── insights/              <-- Domínio de insights + Hot Spots DemandForecastStrategy & CriticalLotActionStrategy
│   │       └── store/                 <-- Domínio de loja + Hot Spot StoreCapacityPolicy
│   └── src/main/resources/
│       ├── orderly-defaults.properties <-- Configurações padrão compartilhadas
│       └── db/migration/              <-- Migrations Flyway estruturais (comuns a todas as instâncias)
│
├── instances/                         <-- Diretório agrupador das aplicações-alvo
│   │
│   ├── instance-burguer/              <-- INSTÂNCIA 1: Hamburgueria Delivery (Fase 1 adaptada)
│   │   ├── src/main/java/             <-- Implementação dos Hot Spots para Hamburgueria
│   │   ├── src/main/resources/
│   │   │   ├── application.properties <-- server.port=8080 | db: orderly_burger
│   │   │   └── db/migration/          <-- V100__seed_burger_menu.sql (lanches, fichas técnicas)
│   │   └── pom.xml                    <-- Depende de 'orderly-framework'
│   │
│   ├── instance-pharma/               <-- INSTÂNCIA 2: Farmácia de Manipulação
│   │   ├── src/main/java/             <-- Implementação dos Hot Spots para Farmácia
│   │   ├── src/main/resources/
│   │   │   ├── application.properties <-- server.port=8081 | db: orderly_pharma
│   │   │   └── db/migration/          <-- Seed de cápsulas, princípios ativos e fórmulas
│   │   └── pom.xml                    <-- Depende de 'orderly-framework'
│   │
│   └── instance-pcbuilder/            <-- INSTÂNCIA 3: Montagem de PCs Customizados
│       ├── src/main/java/             <-- Implementação dos Hot Spots para PCs
│       ├── src/main/resources/
│       │   ├── application.properties <-- server.port=8082 | db: orderly_pc
│       │   └── db/migration/          <-- Seed de processadores, placas de vídeo e kits
│       └── pom.xml                    <-- Depende de 'orderly-framework'
│
├── compose.yaml                       <-- 1 Postgres (3 databases lógicas) + 1 Redis
└── pom.xml                            <-- POM raiz gerindo os módulos
```

---

## 2. CRONOGRAMA POR SPRINT

| Sprint | Prazo Oficial | Foco Principal | Entregáveis |
| :---: | :---: | :--- | :--- |
| **Sprint 0** | **05 de Dezembro** | Definição de Domínio, Pontos Fixos, Pontos Variáveis e 3 Aplicações | Slides de apresentação (`docs/FASE2-SPRINT0-SLIDES.md`). |
| **Sprint 1** | **17 e 19 de Dezembro** | Modelagem Detalhada e Diagrama de Classes UML | Diagrama de Classes UML do Framework destacando Núcleo vs. Hot Spots vs. Instâncias. |
| **Sprint 2** | **07 e 09 de Janeiro** | Modularização Maven, Extração do Core e Adaptação da Instância 1 | `orderly-framework` compilando e `instance-burguer` rodando 100% sobre o framework. |
| **Sprint 3** | **21 e 23 de Janeiro** | Implementação das Instâncias 2 e 3 + Validação Prática | `instance-pharma` e `instance-pcbuilder` implementadas com seeds e testes comparativos. |

---

## 3. PASSO A PASSO DETALHADO DA IMPLEMENTAÇÃO

---

### ETAPA 1: Modelagem e Diagrama de Classes (Sprint 1)

Formalizar a estrutura nos diagramas de classes por módulo (`stock`, `sales`, `insights`, `store`), respeitando o isolamento de *Bounded Contexts* entre os pacotes.

- [x] **Passo 1.1:** Mapear nos diagramas UML os pacotes do **Núcleo do Framework (`orderly-framework`)**:
  - Entidades fixas: `Product`, `StockItem`, `StockLot`, `StockMovement`, `Sale`, `Store`.
  - Serviços de fluxo fixo (*Template Methods*): `StockItemService`, `SaleService`, `CheckoutService`, `SaleStatusService`, `RecipeConsumptionService`, `DailyInsightsService`, `StoreService`.
- [x] **Passo 1.2:** Mapear as **Interfaces dos Hot Spots** respeitando o escopo de domínio de cada módulo (sem acoplamento cruzado entre pacotes):
  - `stock.domain.LotAllocationStrategy` (estratégia exclusiva de seleção/alocação de lotes no estoque).
  - `sales.domain.SaleConsumptionStrategy` (política de cancelamento por status, estorno de insumos ao estoque e fator de perda técnica na produção).
  - `sales.domain.OrderPricingStrategy` (formação do preço total do pedido e cálculo de frete).
  - `insights.domain.DemandForecastStrategy` & `insights.domain.CriticalLotActionStrategy` (previsão de demanda e avaliação de lotes críticos).
  - `store.domain.StoreCapacityPolicy` (regras de capacidade operacional e pausa automática da loja).
- [x] **Passo 1.3:** Mapear a especialização dos Hot Spots nas **3 instâncias** (implementações de interface com nomes claros para a banca).

---

### ETAPA 2: Modularização Maven e Infraestrutura de Banco (Sprint 2 — Início)

Transformar o repositório em um projeto multi-módulo sem quebrar o que já existe.

- [x] **Passo 2.1 — Criar o POM Raiz (`pom.xml` pai):**
  - Configurar `<packaging>pom</packaging>`.
  - Declarar os módulos: `<modules><module>orderly-framework</module><module>instances/instance-burguer</module>...</modules>`.
  - Centralizar versões de dependências em `<dependencyManagement>` (Java 25, Spring Boot 4, Flyway, Postgres, Lombok, etc.).
- [x] **Passo 2.2 — Criar o módulo `orderly-framework`:**
  - Configurar seu `pom.xml` com `<packaging>jar</packaging>`.
  - Mover o código de domínio, aplicação e infraestrutura base para dentro de `orderly-framework`.
  - Mover os scripts Flyway estruturais e `orderly-defaults.properties` para `orderly-framework`.
- [x] **Passo 2.3 — Atualizar o `compose.yaml` para Multi-Database:**
  - Configurar a inicialização do container PostgreSQL para criar automaticamente os 3 bancos lógicos via script `scripts/init-databases.sql`:
    ```sql
    CREATE DATABASE orderly_burger;
    CREATE DATABASE orderly_pharma;
    CREATE DATABASE orderly_pc;
    ```
- [x] **Passo 2.4 — Compilar o Core isoladamente:**
  - Executar `./mvnw clean test -pl orderly-framework` para garantir que o framework não possua referências cruzadas ou imports inválidos.

---

### ETAPA 3: Refatoração dos Hot Spots no Framework (Sprint 2 — Meio)

Extrair as regras de negócio engessadas da Fase 1 para interfaces polimórficas (Padrão *Strategy*), mantendo baixo acoplamento entre os módulos.

#### 🔥 Hot Spot 1: Alocação de Lotes (`stock`) e Consumo/Cancelamento de Produção (`sales`)

> **Decisão Arquitetural (Desacoplamento `stock` $\leftrightarrow$ `sales`):**  
> Para evitar que o módulo `stock` importe `SaleStatus` (de `sales`), este ponto variável foi segregado em duas interfaces coesas, cada uma dentro do seu próprio *Bounded Context*:

- [x] **1A. No módulo `stock` (`bti.pds.dinner.stock.domain`)** — Criar a interface `LotAllocationStrategy`:
  ```java
  public interface LotAllocationStrategy {
      List<LotAllocation> allocate(List<StockLot> availableLots, int requiredQuantity);
  }
  ```
- [x] Refatorar `StockItemService.consumeLots` no framework para receber `LotAllocationStrategy` via injeção de dependência e remover a classe estática `LotLedger` do núcleo.
- [x] Refatorar `StockItemServiceLotTest` para usar uma estratégia fake agnóstica de aplicação.
- [x] **1B. No módulo `sales` (`bti.pds.dinner.sales.domain`)** — Criar a interface `SaleConsumptionStrategy`:
  ```java
  public interface SaleConsumptionStrategy {
      boolean canBeCancelled(SaleStatus saleStatus);
      boolean shouldRestoreOnCancellation(SaleStatus saleStatus);

      default double getProductionLossFactor() {
          return 0.0; // Ex: 0.0 para lanches/PCs, 0.05 (+5%) para farmácia
      }
  }
  ```
- [x] Refatorar `Sale.cancel(SaleConsumptionStrategy strategy)` para delegar a validação de quais status permitem cancelamento sem precisar tornar a entidade `Sale` abstrata.
- [x] Refatorar `SaleStatusService.cancelSale` (*Template Method*) para validar o cancelamento via `sale.cancel(saleConsumptionStrategy)` e consultar `shouldRestoreOnCancellation(statusBeforeCancel)` antes de estornar insumos ao estoque.
- [x] Refatorar `RecipeConsumptionService.accumulateConsumption` para aplicar o `getProductionLossFactor()` da estratégia injetada.
- [x] Refatorar `SaleServiceTest` para testar de forma agnóstica todos os cenários da política de consumo e cancelamento.

#### 🔥 Hot Spot 2: Precificação do Pedido e Cálculo de Frete/Serviço (`sales`)
- [x] Criar a interface de domínio `OrderPricingStrategy` em `bti.pds.dinner.sales.domain`:
  ```java
  public interface OrderPricingStrategy {
      BigDecimal calculateOrderTotal(Sale sale, List<SaleItem> items, BigDecimal baseDeliveryFee);
      BigDecimal calculateDeliveryFee(Address address);
  }
  ```
- [x] Refatorar `Sale.createNew`, `SaleService` e `CheckoutService.simulateFreight`: remover o `switch` fixo de bairros de Natal do framework e delegar o cálculo para a estratégia injetada.

#### 🔥 Hot Spot 3: Previsão de Demanda e Ações para Lotes Críticos (`insights`)

> **Atenção Arquitetural para evitar acoplamento e quebra de fluxos internos:**  
> 1. Hoje, `DailyInsightsService` calcula as datas de amostragem (`sampleWeekdays`) por conta própria antes de buscar o histórico. Na Farmácia (média móvel de 30 dias) e na Loja de PCs (*Lead Time*), a janela de consulta ao histórico muda! Portanto, `DemandForecastStrategy` deve informar qual janela histórica precisa consultar (ex.: `int getHistoryWindowDays()`), ou receber o histórico do período solicitado, mantendo `ProductDemand` e `ForecastConfidence` no pacote `insights.domain` para que `PurchaseSuggestionCalculator` continue funcionando sem alterações.
> 2. `CriticalLotActionStrategy` deve receber os snapshots já existentes em `insights.domain` (`StockLotSnapshot`, `StockItemSnapshot`, `RecipeLine`, `ProductDemand`, `LocalDate targetDate`, `int windowDays`) sem importar entidades de `stock` ou `product`.

- [ ] Criar as interfaces de domínio em `bti.pds.dinner.insights.domain`:
  ```java
  public interface DemandForecastStrategy {
      String getMethodName();
      int getHistoryLookbackDays();
      List<ProductDemand> forecast(
              List<CatalogProduct> products,
              List<DailySoldQuantity> history,
              LocalDate targetDate
      );
  }

  public interface CriticalLotActionStrategy {
      CriticalLotEvaluation evaluate(
              LocalDate targetDate,
              int windowDays,
              List<StockLotSnapshot> lots,
              List<StockItemSnapshot> items,
              List<RecipeLine> recipes,
              List<ProductDemand> demand
      );
  }
  ```
- [ ] Refatorar `DailyInsightsService` para orquestrar o fluxo (*Template Method*): consultar o histórico usando a janela da `DemandForecastStrategy`, executar `demandForecastStrategy.forecast(...)`, calcular sugestões de compra e estoque crítico, e delegar a análise de lotes para `criticalLotActionStrategy.evaluate(...)`.
- [ ] Mover `DemandForecastCalculator` (média por dia da semana) e `ExpiryPromotionCalculator` (sugestão de promoção de vencimento) para `instances/instance-burguer` como `BurgerDemandForecastStrategy` e `BurgerPromotionActionStrategy`.

#### 🔥 Hot Spot 4: Capacidade Produtiva e Aceite Operacional (`store`)

> **Atenção Arquitetural (Desacoplamento `store` $\leftrightarrow$ `sales`):**  
> 1. No roadmap antigo, `StoreCapacityPolicy.canAcceptOrder` recebia `CreateSaleInput` (que pertence a `sales.application.input`) e dizia para alterar `SaleService.ensureStoreIsAvailable`. Isso acoplaria `store` a `sales` e duplicaria a responsabilidade que já existe na porta `StoreAvailability` (implementada por `JpaStoreAvailability` no módulo `store`).
> 2. No código atual, tanto `StoreService` quanto `JpaStoreAvailability` chamam `store.refreshAutomaticStatus(currentTime, ordersInProgress)`, que internamente decide se a loja continua `ABERTA` ou vai para `PAUSADA`.
> 3. Portanto, `StoreCapacityPolicy` deve viver 100% dentro de `bti.pds.dinner.store.domain`, sem importar nada de `sales`, sendo passada para `store.refreshAutomaticStatus(currentTime, ordersInProgress, storeCapacityPolicy)`!

- [ ] Criar a interface de domínio `StoreCapacityPolicy` em `bti.pds.dinner.store.domain`:
  ```java
  public interface StoreCapacityPolicy {
      boolean canAcceptOrder(Store store, int currentOrdersInProgress);
  }
  ```
- [ ] Refatorar `Store.refreshAutomaticStatus` para receber `StoreCapacityPolicy` por parâmetro (mesmo padrão usado em `Sale.cancel` e `Sale.createNew`), pausando a loja (`StoreStatus.PAUSADA`) quando `!capacityPolicy.canAcceptOrder(this, ordersInProgress)`.
- [ ] Injetar `StoreCapacityPolicy` em `StoreService` e `JpaStoreAvailability` para repassá-la ao método `store.refreshAutomaticStatus(...)`.

---

### ETAPA 4: Implementação da Instância 1 — Hamburgueria (Sprint 2 — Fim)

Adaptar o sistema atual da Fase 1 para se comportar como a primeira aplicação consumidora do framework.

- [x] **Passo 4.1 — Criar o módulo `instances/instance-burguer`:**
  - Configurar `pom.xml` importando `orderly-framework`.
  - Criar a classe principal `@SpringBootApplication public class BurgerApplication { ... }`.
  - Configurar `application.properties`:
    - `server.port=8080`
    - `spring.datasource.url=jdbc:postgresql://localhost:5432/orderly_burger`
- [ ] **Passo 4.2 — Implementar as Classes Concretas dos Hot Spots:**
  - [x] `BurgerLotAllocationStrategy` (`stock.domain`): implementa FEFO tradicional ordenando por `expiresAt` e `id`.
  - [x] `BurgerSaleConsumptionStrategy` (`sales.domain`): só permite cancelamento e estorno quando `PENDENTE` (bloqueia cancelamento quando `EM_PREPARO` na chapa) e aplica fator de perda `0.0`.
  - [x] `BurgerPricingStrategy` (`sales.domain`): preço de cardápio + tabela de entrega por bairros locais de Natal.
  - [ ] `BurgerDemandForecastStrategy` (`insights.domain`): algoritmo `WEEKDAY_AVERAGE` (média das últimas 8 semanas do mesmo dia da semana).
  - [ ] `BurgerPromotionActionStrategy` (`insights.domain`): sugere promoções no cardápio para ingredientes vencendo em até 3 dias.
  - [ ] `BurgerCapacityPolicy` (`store.domain`): pausa a loja se `automaticPause` estiver ativo e `currentOrdersInProgress >= maxOrdersInProgress`.
- [ ] **Passo 4.3 — Validar com Seed da Hamburgueria:**
  - Rodar aplicação na porta `8080` e aplicar o seed de demonstração (`scripts/seed-insights-demo.sql`).
  - Executar testes ponta a ponta: fluxo de venda $\rightarrow$ dedução de lotes FEFO $\rightarrow$ painel de insights diário.

---

### ETAPA 5: Implementação das Instâncias 2 e 3 (Sprint 3)

Com o framework validado na Hamburgueria, a implementação das outras duas aplicações consiste em criar os módulos e escrever as classes dos Hot Spots e seus respectivos seeds.

#### Aplicação 2: Farmácia de Manipulação (`instances/instance-pharma`)
- [ ] **Passo 5.1 — Configuração do Módulo:**
  - `application.properties`: `server.port=8081` | `jdbc:postgresql://localhost:5432/orderly_pharma`.
- [ ] **Passo 5.2 — Implementação dos Hot Spots Farmacêuticos:**
  - `PharmaLotAllocationStrategy` (`stock.domain`):
    - **FEFO com Margem de Tratamento:** só aceita lote com validade maior que `hoje + 30 dias`.
    - **Lote Único:** bloqueia alocação se o insumo tiver que ser dividido entre lotes diferentes (rastreabilidade sanitária ANVISA).
  - `PharmaSaleConsumptionStrategy` (`sales.domain`):
    - Só permite cancelar e estornar se `PENDENTE` (fórmula em manipulação no laboratório não pode ser cancelada).
    - Aplica **fator de perda técnica de manipulação (`0.05` / +5%)** na pesagem/encapsulamento.
  - `PharmaPricingStrategy` (`sales.domain`): custo dos itens + taxa técnica de manipulação/encapsulamento + frete especial térmico.
  - `PharmaDemandForecastStrategy` (`insights.domain`): **Média Móvel Contínua (últimos 30 dias)**.
  - `PharmaQuarantineActionStrategy` (`insights.domain`): lotes próximos do vencimento geram **Alerta de Quarentena e Descarte Sanitário** (sem sugerir promoção de medicamentos).
  - `PharmaCapacityPolicy` (`store.domain`): valida limite operacional simultâneo da bancada laboratorial.
- [ ] **Passo 5.3 — Seed Farmacêutico:**
  - Criar massa com *Amoxicilina, Paracetamol, Cápsulas Gelatinosas, Frascos Âmbar*.

#### Aplicação 3: Loja de Montagem de PCs (`instances/instance-pcbuilder`)
- [ ] **Passo 6.1 — Configuração do Módulo:**
  - `application.properties`: `server.port=8082` | `jdbc:postgresql://localhost:5432/orderly_pc`.
- [ ] **Passo 6.2 — Implementação dos Hot Spots de Hardware:**
  - `PcBuilderLotAllocationStrategy` (`stock.domain`):
    - **FIFO:** aloca pelo lote mais antigo de entrada (`receivedAt`), independente da validade.
  - `PcBuilderSaleConsumptionStrategy` (`sales.domain`):
    - Permite cancelamento tanto em `PENDENTE` quanto em `EM_PREPARO` (bancada de montagem), **estornando 100% dos componentes intactos ao estoque** em ambos os casos, com perda `0.0`.
  - `PcBuilderPricingStrategy` (`sales.domain`): soma das peças + taxa de mão de obra de montagem e *stress test* + seguro de frete `ad valorem` (1,5% sobre o valor da máquina).
  - `PcBuilderDemandForecastStrategy` (`insights.domain`): projeção baseada em *Lead Time* de reposição.
  - `PcBuilderBundleActionStrategy` (`insights.domain`): análise de *Aging Stock* (> 90 dias parado) sugerindo **Kits Promocionais / Bundles de Upgrade**.
  - `PcBuilderCapacityPolicy` (`store.domain`): controle por limite de máquinas simultâneas nas bancadas técnicas.
- [ ] **Passo 6.3 — Seed de Informática:**
  - Criar massa com *CPUs Ryzen/Intel, GPUs RTX, Memórias DDR5, Fontes 750W*.

---

### ETAPA 6: Bateria de Testes Comparativos e Roteiro de Demonstração (Sprint 3 — Fim)

Preparação do ambiente para a apresentação final ao professor.

- [ ] **Passo 7.1 — Script de Inicialização Único (`scripts/start-all-instances.sh`):**
  - Script bash que sobe os containers do Docker (`docker compose up -d`) e inicia os três `.jar` em paralelo (portas 8080, 8081 e 8082).
- [ ] **Passo 7.2 — Script de Seed Automatizado (`scripts/seed-all-demo.sh`):**
  - Popula os três bancos lógicos com seus respectivos cenários de negócio.
- [ ] **Passo 7.3 — Roteiro de Demonstração ao Vivo:**
  1. Mostrar o terminal compilando o `orderly-framework` sozinho (`./mvnw test -pl orderly-framework`).
  2. Subir a **Hamburgueria (8080)** e demonstrar:
     - Criação de pedido consumindo insumos via FEFO simples.
     - Bloqueio de cancelamento quando o pedido já está `EM_PREPARO`.
     - Painel de insights sugerindo promoção para queijo vencendo em 2 dias.
  3. Subir a **Farmácia (8081)** e demonstrar:
     - Consumo com acréscimo de +5% de perda técnica de manipulação.
     - Tentativa de usar lote que vence em 15 dias ou lote fragmentado sendo **rejeitada pela política sanitária**.
     - Painel de insights sugerindo descarte/quarentena (e não promoção comercial).
  4. Subir a **Loja de PCs (8082)** e demonstrar:
     - Pedido alocando peças via FIFO (`receivedAt`).
     - Cancelamento em `EM_PREPARO` (montagem) sendo permitido e estornando 100% dos componentes intactos.
     - Painel sugerindo montagem de *Bundle* para placas de vídeo paradas.

---

## 4. CHECKLIST DE CONTROLE DE QUALIDADE (FRAMEWORK)

Antes de entregar cada etapa, execute esta checagem de conformidade:

- [x] **Inversão de Dependência Respeitada:** O módulo `orderly-framework` **não tem nenhuma linha de código** importando pacotes de `instance-burguer`, `instance-pharma` ou `instance-pcbuilder`.
- [x] **Isolamento entre Módulos (Bounded Contexts):** Nenhuma interface de *Hot Spot* em `stock`, `sales`, `insights` ou `store` importa classes de domínio ou DTOs de outro módulo vizinho.
- [ ] **Ausência de *Hardcoding* no Núcleo:** Nenhum nome de bairro específico (ex: "Ponta Negra", "Tirol") ou calculadora exclusiva de hambúrguer existe dentro de `orderly-framework`.
- [x] **Hot Spots Exclusivamente de Domínio:** Nenhuma interface de Hot Spot foi criada para alternar banco de dados ou meio de envio de e-mail (infraestrutura mantida transparente conforme exigência do Prof. Uirá).
- [ ] **Testes Unitários de Domínio:** O framework possui testes unitários agnósticos com Fakes e cada implementação de *Strategy* nas instâncias possui testes unitários validando as regras específicas daquele domínio.
