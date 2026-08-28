# Guia de Configuração e Execução (Setup)

Este guia descreve os passos necessários para configurar o ambiente local e rodar o projeto `dinner`.

---

## 1. Variáveis de Ambiente e Configurações Externas

O projeto utiliza serviços e configurações externas de segurança que precisam ser configuradas na sua máquina antes da execução.

### Resend (Envio de Emails)

O sistema de autenticação utiliza a API do [Resend](https://resend.com/) para o envio de e-mails de verificação e recuperação de senha.
Você precisará de uma chave de API válida para o serviço funcionar.

1. Crie uma conta no [Resend](https://resend.com/) e gere uma **API Key**.
2. No seu arquivo `application.properties` (ou `.env`, dependendo de como você injeta), defina a propriedade:
   ```properties
   resend.api.key=re_sua_chave_gerada_aqui
   ```

### Chaves Criptográficas (JWT)

A autenticação é feita via Tokens JWT que são assinados digitalmente usando um par de chaves RSA (Pública e Privada). Por questões de segurança, **essas chaves não são enviadas para o repositório** e precisam ser geradas localmente.

O projeto espera que as chaves existam dentro do diretório `src/main/resources/env/`. Siga os passos abaixo para gerar as suas chaves usando o `openssl`:

1. Abra o terminal na raiz do projeto.
2. Crie a pasta `env` caso ela não exista:
   ```bash
   mkdir -p src/main/resources/env
   ```
3. Gere a **chave privada** (formato PKCS#8) chamada `app.key`:
   ```bash
   openssl genpkey -algorithm RSA -out src/main/resources/env/app.key -pkeyopt rsa_keygen_bits:2048
   ```
4. A partir da chave privada, extraia a **chave pública** chamada `app.pub`:
   ```bash
   openssl rsa -pubout -in src/main/resources/env/app.key -out src/main/resources/env/app.pub
   ```

Pronto! As chaves estarão no local correto que o `JwtConfig` e o `application.properties` esperam.

> **Importante:** A pasta `src/main/resources/env/` está no `.gitignore`. **Nunca** faça o commit de chaves de produção ou desenvolvimento reais no repositório.

---

## 2. Banco de Dados

O projeto está configurado com Spring Boot, JPA, Flyway (para migrações) e PostgreSQL.
Certifique-se de ter um banco PostgreSQL rodando de acordo com as credenciais padrões do projeto (geralmente localhost porta 5432) ou inicialize-o via **Docker Compose**, caso disponível.

Os testes de integração utilizam o **Testcontainers**, o que significa que durante a rotina de testes (`mvn test`), o banco de dados de teste será levantado automaticamente num contêiner Docker (você precisa ter o Docker rodando na sua máquina).

---

## 3. Rodando o Projeto

Após gerar as chaves e configurar a API Key, você pode rodar o projeto de duas formas:

### Via IDE (IntelliJ, Eclipse, VS Code)
Basta abrir a classe principal `DinnerApplication.java` e executá-la.

### Via Maven (Terminal)
Na raiz do projeto, execute:

```bash
./mvnw spring-boot:run
```

Para rodar a suíte completa de testes:
```bash
./mvnw clean test
```
