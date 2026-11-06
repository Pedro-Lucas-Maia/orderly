# 🏛️ Guia de Evolução Arquitetural — Do Monólito ao Framework (Fase 2)

> **Documento Interno da Equipe:** Glauco, Pedro e Yuri  
> **Objetivo:** Explicar de forma simples e direta o que mudou na estrutura do projeto, o porquê de cada mudança e como trabalhar com o novo código.

---

## 1. O que era a Fase 1 vs. O que é a Fase 2

- **Fase 1 (Monólito da Hamburgueria):** Tínhamos uma aplicação Spring Boot tradicional em uma única pasta `src/`. Nela se misturavam as regras genéricas de estoque e pedidos com dados e regras específicas da Hamburgueria (pão brioche, taxa por bairro de Natal, etc.).
- **Fase 2 (Orderly Framework):** O sistema foi transformado em um **Framework de Aplicação**. Agora temos uma biblioteca central reutilizável (**Núcleo / Pontos Fixos**) e aplicações que consomem esse núcleo (**Instâncias / Pontos Variáveis**).

---

## 2. A Nova Estrutura de Pastas (Maven Multi-Módulo)

Em vez de criar 4 repositórios no Git (o que tornaria o desenvolvimento caótico), mantivemos **um único repositório**, organizado em módulos independentes:

```text
orderly/                              <-- Raiz do projeto
│
├── orderly-framework/                 <-- 📦 MÓDULO 1: O FRAMEWORK (Biblioteca)
│   ├── src/main/java/                 <-- Todo o código base (entidades, services, controllers)
│   ├── src/main/resources/
│   │   ├── orderly-defaults.properties <-- Configurações padrão compartilhadas
│   │   ├── env/ (app.key, app.pub)    <-- Chaves RSA padrão do JWT
│   │   └── db/migration/              <-- Tabelas estruturais (V1..V9, V11..V17, V19)
│   └── pom.xml                        <-- Gera um arquivo .JAR reutilizável
│
├── instances/                         <-- 🚀 APLICAÇÕES FINAIS (Executáveis)
│   │
│   ├── instance-burguer/              <-- INSTÂNCIA 1: Hamburgueria Delivery
│   │   ├── src/main/java/.../BurgerApplication.java  <-- Classe que roda o Spring Boot
│   │   ├── src/main/resources/
│   │   │   ├── application.properties <-- Importa os defaults e define: porta 8080, db: orderly_burger
│   │   │   └── db/migration/V100__seed_burger_menu.sql <-- Cardápio de hambúrguer e fichas técnicas
│   │   └── pom.xml                    <-- Importa 'orderly-framework' como dependência
│   │
│   ├── instance-pharma/               <-- (Sprint 3) Farmácia de Manipulação (porta 8081)
│   └── instance-pcbuilder/            <-- (Sprint 3) Loja de PCs (porta 8082)
│
├── docker-compose.yml                 <-- 1 Postgres (3 bancos) + 1 Redis
└── pom.xml                            <-- POM Pai que gerencia todos os módulos
```

---

## 3. Principais Mudanças e o Porquê de Cada Uma

### A. O Framework não "roda" sozinho, quem roda é a Instância
- **O que mudou:** A classe `DinnerApplication.java` saiu do framework e foi para `instances/instance-burguer/BurgerApplication.java`.
- **Por quê?** O framework é apenas uma biblioteca (`.jar`). Ele não tem servidor embutido ligado. Quem sobe o Tomcat e o Spring Boot é cada aplicação cliente individualmente.

### B. Convenção sobre Configuração (`application.properties`)
- **O que mudou:** Todas as configurações comuns (JWT, Flyway, chaves RSA, timezone, regras de insights) agora moram em `orderly-framework/src/main/resources/orderly-defaults.properties`.
- **Por quê?** Para não precisar copiar e colar 50 linhas de configuração e pastas de chaves em cada aplicação. Na hamburgueria, nós apenas importamos os padrões com uma linha:
  ```properties
  spring.config.import=optional:classpath:orderly-defaults.properties
  server.port=8080
  spring.datasource.url=jdbc:postgresql://localhost:5432/orderly_burger
  spring.data.redis.database=0
  ```

### C. Separação de Migrations (Estrutura vs. Cardápio)
- **O que mudou:** Os dados fictícios de hambúrgueres (antigas `V10` e `V18`) saíram do framework e foram para a hamburgueria como `V100__seed_burger_menu.sql`.
- **Por quê?** O framework só pode criar as tabelas (`users`, `products`, `stock_items`). Se ele inserisse hambúrgueres automaticamente, quando criássemos a Farmácia ou a Loja de PCs, elas teriam lanches no banco de dados.

### D. Banco de Dados e Redis Compartilhados de Forma Leve
- **O que mudou:** O arquivo `compose.yaml` agora executa o script `scripts/init-databases.sql` que cria 3 bancos lógicos no mesmo container Postgres: `orderly_burger`, `orderly_pharma` e `orderly_pc`. E no Redis usamos índices de banco (`database=0` para hamburgueria, `1` para farmácia).
- **Por quê?** Evita subir 3 servidores PostgreSQL e 3 Redis pesados. Gastamos apenas ~150MB de memória RAM no total e cada aplicação tem seus dados 100% isolados.

---

## 4. Como Rodar o Projeto Agora

Tudo continua muito simples pelo terminal da raiz:

1. **Compilar todos os módulos:**
   ```bash
   ./mvnw clean compile
   ```
2. **Subir os bancos no Docker:**
   ```bash
   docker compose up -d
   ```
3. **Executar a Hamburgueria:**
   - Pela IDE: basta rodar o `main` da classe `instances/instance-burguer/.../BurgerApplication.java`.
   - Ou pelo terminal:
     ```bash
     ./mvnw spring-boot:run -pl instances/instance-burguer
     ```
   A aplicação subirá normalmente na porta **8080**!

---

## 5. Próximos Passos (Sprints 1 a 3)

1. **Sprint 1 (Modelagem):** Desenhar o Diagrama de Classes UML mostrando os *Hot Spots* (interfaces como `LotAllocationStrategy`, `OrderPricingStrategy`, `DemandForecastStrategy`).
2. **Sprint 2 (Hot Spots em Código):** Substituir as regras "travadas" (ex.: o switch de bairros de Natal em `SaleService`) por injeção das interfaces do framework.
3. **Sprint 3 (Novas Aplicações):** Criar os módulos `instance-pharma` e `instance-pcbuilder` implementando essas interfaces e demonstrar as 3 rodando juntas para o professor!
