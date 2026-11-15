# Guia de Observabilidade, Logging e Tratamento de Erros

Este documento descreve a arquitetura de **Observabilidade** (Logs, Métricas e Traces) e o novo padrão centralizado de **Tratamento de Exceções** implementados no `orderly-framework`.

---

## 1. Visão Geral da Stack (OTel + LGTM)

A aplicação foi instrumentada utilizando **Micrometer** e **OpenTelemetry (OTel)**, exportando telemetria via protocolo **OTLP (HTTP)** para um contêiner unificado `grafana/otel-lgtm` configurado no `compose.yaml`.

| Pilar | Tecnologia no Spring Boot | Destino (Container `otel-lgtm`) | Função |
| :--- | :--- | :--- | :--- |
| **Logs** | SLF4J + Logback (`OpenTelemetryAppender`) | **Grafana Loki** | Registro de eventos de negócio, auditoria e erros com correlação automática de `traceId` e `spanId`. |
| **Traces** | Micrometer Tracing + OpenTelemetry Bridge | **Grafana Tempo** | Rastreamento distribuído do ciclo de vida das requisições HTTP, chamadas externas e eventos `@Async`. |
| **Métricas** | Spring Boot Actuator + Micrometer OTLP | **Prometheus / Mimir** | Métricas de JVM, HTTP, pool de conexões e tempos de execução dos métodos anotados com `@Observed`. |
| **Visualização** | - | **Grafana** (`http://localhost:3001`) | Interface web unificada para consultar logs (Loki), traces (Tempo) e dashboards de métricas. |

### Como acessar localmente
Ao subir a aplicação (ou rodar `docker compose up -d`), a stack sobe automaticamente:
* **URL do Grafana:** `http://localhost:3001` *(porta alterada para `3001` para evitar conflito com o frontend na `3000`)*
* **Credenciais padrão:** `admin` / `admin`
* **Receptor OTLP:** `http://localhost:4318` (HTTP) e `localhost:4317` (gRPC)

---

## 2. Como a Instrumentação Funciona no Código

### 2.1. Correlação de Logs e Traces (`logback-spring.xml` e `OpenTelemetryLogBackConfig`)
* Todos os logs emitidos via `@Slf4j` passam tanto pelo console quanto pelo `OpenTelemetryAppender`.
* Cada linha de log recebe automaticamente o `traceId` e o `spanId` da requisição atual (configurado em `orderly-defaults.properties` via `logging.pattern.correlation`).
* Isso permite, dentro do Grafana, clicar em um log de erro no **Loki** e abrir diretamente o trace completo daquela requisição no **Tempo** (e vice-versa).

### 2.2. Monitoramento de Casos de Uso e Integrações (`@Observed`)
Os principais métodos de escrita e processamento analítico nos `Services` (além de integrações externas como `ResendEmailSender` e `CloudinaryImageRepository`) foram anotados com `@Observed`.

**Padrão de nomenclatura adotado:** `<modulo>.<ator>.<acao>` (em *kebab-case*)
* Exemplos:
  * `auth.user.login`, `auth.user.register`
  * `sales.user.checkout`, `sales.admin.confirm-sale`
  * `stock-item.admin.movement`
  * `insights.admin.daily`, `reports.admin.sales`
  * `infrastructure.mail.resend`, `infrastructure.image.cloudinary.upload`

O uso de `@Observed` gera simultaneamente:
1. Um **Span** filho no trace da requisição (permitindo ver quanto tempo levou especificamente o método ou a chamada ao Cloudinary/Resend).
2. **Métricas de timer/contador** exportadas para o Prometheus/Grafana.

### 2.3. Propagação de Contexto em Processos Assíncronos (`AsyncObservabilityConfig`)
Para métodos executados em background com `@Async` (como o envio de e-mails pós-commit em `UserRegistrationListener` e `PasswordResetListener`), foi configurado o `ContextPropagatingTaskDecorator`.
* **Como funciona:** Ele copia o `traceId` da thread principal (HTTP) para a Virtual Thread assíncrona. Assim, os logs e spans do envio de e-mail permanecem vinculados ao mesmo trace da requisição original de cadastro ou recuperação de senha.

---

## 3. Novo Padrão de Exceções e `GlobalExceptionHandler`

Os diversos `ExceptionHandlers` espalhados pelos módulos foram substituídos por uma hierarquia única de exceções de domínio em `common/exception` e um único handler global (`GlobalExceptionHandler`).

### 3.1. Categorias de Erro (`ErrorCategory` e `DomainException`)
Toda exceção de negócio herda de `DomainException` (através de classes base semânticas), que define uma `ErrorCategory` mapeada automaticamente para o status HTTP correspondente:

* `DomainValidationException` (`VALIDATION` $\rightarrow$ **400 Bad Request**)
* `UnauthorizedException` (`UNAUTHORIZED` $\rightarrow$ **401 Unauthorized**)
* `ForbiddenAccessException` (`FORBIDDEN` $\rightarrow$ **403 Forbidden**)
* `ResourceNotFoundException` (`RESOURCE_NOT_FOUND` $\rightarrow$ **404 Not Found**)
* `ConflictException` (`CONFLICT` $\rightarrow$ **409 Conflict**)
* `BusinessRuleException` (`UNPROCESSABLE` $\rightarrow$ **422 Unprocessable Entity**)
* `ExternalServiceException` (`EXTERNAL_FAILURE` $\rightarrow$ **500 Internal Server Error**)
* `InternalErrorException` / `DataIntegrityException` (`INTERNAL_ERROR` $\rightarrow$ **500 Internal Server Error**)

### 3.2. Política de Logs no `GlobalExceptionHandler`
Para evitar poluição de logs e vazamento de detalhes internos para o cliente:
* **Erros `4xx` (Erros do Cliente / Regra de Negócio):** São logados automaticamente como **`WARN`** (apenas uma linha com status, título, path e mensagem, **sem stack trace**).
* **Erros `5xx` (Falhas Internas, `DataIntegrityException`, Serviços Externos ou Bugs inesperados):**
  * São logados automaticamente como **`ERROR`** **com a stack trace completa** e a mensagem real da falha para investigação da equipe.
  * Para o cliente HTTP, a mensagem interna é ocultada e substituída por uma mensagem genérica segura (`"An unexpected internal error occurred"`), evitando expor detalhes de SQL ou infraestrutura.

---

## 4. Boas Práticas para Novos Desenvolvimentos na Equipe

Ao criar novas features ou alterar serviços existentes, siga estas convenções:

1. **Não crie novos `@RestControllerAdvice` por módulo:** Faça sua nova exceção estender uma das classes base do pacote `bti.pds.dinner.common.exception` (ex: `ResourceNotFoundException`, `BusinessRuleException`, `ConflictException`). O `GlobalExceptionHandler` cuidará do retorno HTTP e do log automaticamente.
2. **Não faça `log.error` ou `log.warn` antes de lançar exceções em fluxos síncronos (HTTP):** Como o `GlobalExceptionHandler` já loga todas as exceções que sobem do Controller, logar no Service gera duplicidade.
   * *Exceção à regra:* Métodos `@Async` (como Listeners de eventos), pois rodam fora da thread HTTP e não passam pelo `GlobalExceptionHandler`. Neles, use `try/catch` com `log.error(..., ex)`.
3. **Proteção de Dados Sensíveis (LGPD / PII):** Nunca inclua senhas, tokens brutos, CPFs ou e-mails completos em mensagens de log ou de exceção. Utilize o utilitário `LogMaskUtils.maskEmail(...)` e `LogMaskUtils.maskCpf(...)`.
4. **Formato das Mensagens de Log (`@Slf4j`):**
   * Logue no nível `INFO` apenas eventos relevantes de mudança de estado (após o `.save()` ou `.delete()` no repositório) ou conclusões de relatórios/cálculos.
   * Use pares `chave=valor` claros ao final da frase e passe valores primitivos (ex: `.uuid()` ou `.value()`) em vez do objeto inteiro:
     ```java
     log.info("Sale dispatched. userId={}, saleId={}", savedSale.getUserId().uuid(), savedSale.getId().uuid());
     ```
