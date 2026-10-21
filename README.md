# Orderly 🍽️

> Plataforma de Gestão Operacional e Inteligência para Restaurantes e Food Service.

---

### Identificação do Projeto

- **Instituição:** Universidade Federal do Rio Grande do Norte (UFRN)
- **Departamento:** Departamento de Informática e Matemática Aplicada (DIMAp)
- **Curso:** Bacharelado em Tecnologia da Informação (BTI)
- **Disciplina:** Projeto Detalhado de Software (DIM0138)
- **Docente Responsável:** Prof. Dr. Uirá Kulesza
- **Discentes:**
  - Glauco Sales Paiva Virginio
  - Pedro Lucas Maia de Paiva
  - Yuri Gabriel de Macedo Machado
- **Interface Web (Frontend):** [orderly-frontend](https://github.com/nzsyuu/orderly-frontend)

---

## 📌 Visão Geral do Sistema

O **Orderly** é uma solução backend completa desenvolvida para suprir as necessidades operacionais e estratégicas do segmento de alimentação comercial (restaurantes, cozinhas industriais e delivery). 

O sistema integra a gestão de múltiplas lojas físicas, catálogo de produtos com ficha técnica de insumos, controle de estoque rastreado por lotes e política de validade, processamento de pedidos/vendas e um motor analítico que gera briefings diários e relatórios financeiros/operacionais de alta performance.

---

## 🎯 Destaques de Design e Arquitetura de Software

O foco central da disciplina de **Projeto Detalhado de Software** foi exercitar padrões avançados de design, manutenibilidade, separação estrita de responsabilidades e desacoplamento. Os principais destaques técnicos do projeto incluem:

### 1. Módulo de Insights: Previsão Determinística e Calculadoras Puras
Em vez de depender de modelos externos ou abordagens probabilísticas opacas, o módulo de inteligência do Orderly implementa um **motor determinístico de tomada de decisão operacional**:
- **Previsão de Demanda Semanal:** Analisa um histórico de 8 semanas, calculando médias do mesmo dia da semana e considerando explicitamente dias sem vendas (evitando inflar projeções).
- **Consumo e Reposição com FEFO (*First Expired, First Out*):** Identifica lotes próximos do vencimento, sugerindo itens para promoções ativas e gerando listas de compras prioritárias de insumos.
- **Isolamento de Domínio:** As regras residem em calculadoras puras (`DemandForecastCalculator`, `PurchaseSuggestionCalculator`, `ExpiryPromotionCalculator`, `CriticalStockCalculator`), desacopladas de frameworks e 100% testáveis sem necessidade de banco de dados.
- Para detalhes matemáticos e de contrato, consulte os documentos em [`docs/insights/`](file:///home/pedro/IdeaProjects/orderly/docs/insights/).

### 2. Módulo de Autenticação: Domínio Rico e Arquitetura Orientada a Eventos
- **Arquitetura Orientada a Eventos:** Processos secundários (como envio de e-mails transacionais de boas-vindas e tokens de recuperação de senha) são disparados por meio de Eventos de Domínio (`OnUserRegisteredEvent`, `OnPasswordResetedEvent`). Listeners na camada de infraestrutura (`UserRegistrationListener`, `PasswordResetListener`) interceptam os eventos e orquestram a comunicação com o serviço externo ([Resend API](https://resend.com/)), mantendo o fluxo de negócio desacoplado do meio de envio.
- **Domínio Rico vs. Anêmico:** Entidades de domínio encapsulam suas próprias invariantes e regras de validação, sem exposição indiscriminada de *setters*.
- **Segurança Criptográfica Assimétrica:** Autenticação stateless via JWT assinada com par de chaves RSA de 2048 bits (PKCS#8), garantindo isolamento criptográfico entre a emissão e a validação de credenciais.

### 3. Módulo de Relatórios: Padrão *Ports & Adapters* e Pragmatismo com Spring JDBC
- **Desacoplamento Tecnológico:** Para relatórios gerenciais e consultas analíticas de grande volume (agregações diárias, movimentações de estoque, curva de produtos), a persistência via ORM (JPA) daria lugar a sobrecarga desnecessária. Optou-se pelo uso de consultas otimizadas com **Spring JDBC** (`JdbcTemplate`).
- **Inversão de Dependência:** O domínio define as portas de dados (`SalesReportDataPort`, `StockReportDataPort`), enquanto a infraestrutura implementa os adaptadores (`SalesReportDataAdapter`, `StockReportDataAdapter`) que encapsulam o JDBC. 
- **Integridade Arquitetural:** Nem uma única linha do Domínio ou da Camada de Aplicação precisou ser alterada para acomodar a transição entre JPA e JDBC, demonstrando a eficácia do padrão *Ports and Adapters* (Arquitetura Hexagonal / Clean Architecture).

---

## 🏛️ Organização dos Bounded Contexts (Módulos)

O sistema segue uma organização modular onde cada pacote representa um contexto bem delimitado:

| Módulo | Responsabilidade |
| :--- | :--- |
| `auth` | Cadastro, perfis, tokens, autenticação JWT com chaves RSA e eventos transacionais. |
| `store` | Gestão de lojas/unidades físicas, endereços e status de funcionamento. |
| `product` | Cardápio de produtos, fichas técnicas (receitas/composição) e upload de imagens via Cloudinary. |
| `stock` | Controle de insumos, movimentações, rastreabilidade de lotes com validade e política FEFO. |
| `shoppingCart` | Gestão de itens em carrinho e cálculo de subtotal por cliente. |
| `sales` | Registro de pedidos, endereços de entrega e ciclo de vida das vendas. |
| `insights` | Briefing operacional do dia, previsão de demanda e sugestão de compras. |
| `reports` | Geração de relatórios consolidados de vendas e estoque via JDBC analítico. |
| `address` | Gestão de endereços e vinculação territorial de usuários e lojas. |
| `common` | Tratamento global de exceções, utilitários de resposta HTTP e classes base de domínio. |

> Detalhes sobre o fluxo de dependências e a divisão entre `domain`, `application` e `infrastructure` estão descritos em [`docs/ARCHITECTURE.md`](file:///home/pedro/IdeaProjects/orderly/docs/ARCHITECTURE.md).

---

## ⚖️ Trade-offs e Decisões de Escopo

O Orderly foi projetado com nível de robustez voltado para produção (migrações com Flyway, testes com containers reais, autenticação com chaves assimétricas, resiliência de cache com Redis). 

A principal decisão de escopo consciente tomada pelo grupo para o escopo da disciplina foi:
- **Ausência de Gateway de Pagamento Integrado:** Não foram integradas soluções externas reais de checkout (ex.: Stripe, MercadoPago ou Pagar.me). O ciclo de vida do pedido simula os status de transação para que o foco da equipe permanecesse integralmente voltado à modelagem rica do domínio, integridade arquitetural (DDD/Clean Architecture), gestão de insumos/lotes e motor analítico.

---

## 🛠️ Stack Tecnológica

- **Linguagem:** Java 25
- **Framework:** Spring Boot 4
- **Segurança:** Spring Security, OAuth2 Resource Server, JWT (Assinatura RSA 2048-bit)
- **Persistência & Banco de Dados:** PostgreSQL, Spring Data JPA, Spring JDBC (`JdbcTemplate`), Flyway Migrations
- **Cache & Sessão:** Redis, Spring Data Redis
- **Infraestrutura & Containers:** Docker, Docker Compose (`compose.yaml`), Testcontainers
- **Integrações Externas:** Cloudinary API (Imagens do Cardápio), Resend API (Envio de E-mails)
- **Testes:** JUnit 5, Mockito, Testcontainers

---

## 🚀 Guia Rápido de Execução (Quick Start)

### Pré-requisitos
- **Java 25** instalado
- **Docker e Docker Compose** ativos
- **OpenSSL** (para geração do par de chaves RSA do JWT)

### Passo a Passo

1. **Clonar o Repositório:**
   ```bash
   git clone https://github.com/Pedro-Lucas-Maia/orderly.git
   cd orderly
   ```

2. **Gerar as Chaves Criptográficas (RSA):**
   ```bash
   mkdir -p src/main/resources/env
   openssl genpkey -algorithm RSA -out src/main/resources/env/app.key -pkeyopt rsa_keygen_bits:2048
   openssl rsa -pubout -in src/main/resources/env/app.key -out src/main/resources/env/app.pub
   ```

3. **Configurar Credenciais Obrigatórias:**
   Configure suas credenciais do Cloudinary e Resend como variáveis de ambiente ou diretamente em `src/main/resources/application.properties`:
   ```properties
   cloudinary.cloud-name=seu_cloud_name
   cloudinary.api-key=sua_api_key
   cloudinary.api-secret=seu_api_secret
   resend.api.key=sua_resend_api_key
   ```

4. **Executar a Aplicação:**
   O projeto conta com o Spring Boot Docker Compose integrado. Ao iniciar, os containers de **PostgreSQL** e **Redis** sobem automaticamente:
   ```bash
   ./mvnw spring-boot:run
   ```

> 📖 Para instruções detalhadas de configuração e tabela completa de variáveis de ambiente, consulte o guia em [`docs/SETUP.md`](file:///home/pedro/IdeaProjects/orderly/docs/SETUP.md).

---

## 🌱 Dados de Demonstração (Seed) & Usuário de Teste

Para facilitar a avaliação da banca e permitir que os relatórios analíticos e a previsão preditiva sejam testados com massa de dados verossímil, disponibilizamos uma carga simulando **aproximadamente 3 meses de operação real** de uma loja.

### O que o Seed popula:
- Usuários e perfis de acesso;
- Cardápio de produtos com fichas técnicas e insumos cadastrados;
- Histórico contínuo de movimentações de estoque e controle de lotes com validade (FEFO);
- Histórico distribuído de vendas/pedidos em múltiplos status ao longo de ~90 dias (permitindo que o algoritmo de 8 semanas de `insights` e os relatórios analíticos de `reports` calculem tendências e métricas reais).

### Como aplicar o Seed:
Com a aplicação e os contêineres do banco em execução, execute o script utilitário no terminal:
```bash
./scripts/seed-insights-demo.sh
```
*(Ou importe diretamente o script SQL [`scripts/seed-insights-demo.sql`](file:///home/pedro/IdeaProjects/orderly/scripts/seed-insights-demo.sql) no seu cliente PostgreSQL)*.

### Credenciais do Usuário de Teste (Admin):
Para autenticar na API ou efetuar login na [interface web](https://github.com/nzsyuu/orderly-frontend) com permissões administrativas completas:

| Campo | Valor |
| :--- | :--- |
| **E-mail** | `admin@orderly.com` |
| **Senha** | `admin123` |

---

## 🧪 Qualidade de Código e Testes

O projeto conta com suíte de testes unitários isolados para as regras de negócio de domínio e testes de integração utilizando **Testcontainers** para subir instâncias reais do PostgreSQL e Redis durante a validação.

Para executar todos os testes automatizados:
```bash
./mvnw test
```

---

## 📚 Documentações Complementares

- 📐 [Guia de Arquitetura e Decisões de DDD/Clean Arch](file:///home/pedro/IdeaProjects/orderly/docs/ARCHITECTURE.md)
- ⚙️ [Guia de Configuração e Execução Completo](file:///home/pedro/IdeaProjects/orderly/docs/SETUP.md)
- 📊 [Documentação da Lógica de Cálculos e APIs do Módulo de Insights](file:///home/pedro/IdeaProjects/orderly/docs/insights/03-Calculos-e-API.md)
- 📝 [Implementação e Detalhamento da V1 do Módulo de Insights](file:///home/pedro/IdeaProjects/orderly/docs/insights/05-Implementacao-V1.md)
- 🌐 [Repositório da Interface Web (Frontend)](https://github.com/nzsyuu/orderly-frontend)
