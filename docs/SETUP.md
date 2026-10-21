# Guia de Configuração e Execução (Setup)

Este guia descreve os passos necessários para configurar o ambiente local e rodar o projeto `dinner`.

---

## 1. Variáveis de Ambiente e Configurações Externas

O projeto utiliza serviços e configurações externas de segurança que precisam ser configuradas na sua máquina antes da execução.

### Resend (Envio de Emails)

O sistema de autenticação utiliza a API do [Resend](https://resend.com/) para o envio de e-mails de verificação e recuperação de senha.
Você precisará de uma chave de API válida para o serviço funcionar.

1. Crie uma conta no [Resend](https://resend.com/) e gere uma **API Key**.
2. Defina a variável de ambiente `RESEND_API_KEY` ou configure diretamente em `application.properties`:
   ```properties
   resend.api.key=re_sua_chave_gerada_aqui
   ```
   > **Nota:** Caso não seja informada, o valor padrão é `default_api_key`, mas os envios reais falharão.

### Cloudinary (Armazenamento de Imagens)

O módulo de produtos utiliza o [Cloudinary](https://cloudinary.com/) para upload, hospedagem e remoção das imagens do catálogo.

> **Atenção:** As configurações do Cloudinary são **obrigatórias** para o startup da aplicação. Se não forem fornecidas, o Spring Boot não conseguirá inicializar o bean `CloudinaryConfig` e a aplicação falhará ao subir.

1. Crie uma conta no [Cloudinary](https://cloudinary.com/).
2. No seu Dashboard / Console (em *API Keys*), obtenha as seguintes credenciais:
   - **Cloud Name**
   - **API Key**
   - **API Secret**
3. Configure as variáveis de ambiente ou atualize seu `application.properties`:
   ```properties
   cloudinary.cloud-name=seu_cloud_name
   cloudinary.api-key=sua_api_key
   cloudinary.api-secret=seu_api_secret
   ```
   Ou exporte via terminal / configuração de execução da IDE:
   ```bash
   export CLOUDINARY_CLOUD_NAME=seu_cloud_name
   export CLOUDINARY_API_KEY=sua_api_key
   export CLOUDINARY_API_SECRET=seu_api_secret
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

### Resumo das Variáveis de Ambiente

| Variável | Descrição | Obrigatória? | Valor Padrão |
| :--- | :--- | :---: | :--- |
| `CLOUDINARY_CLOUD_NAME` | Nome da conta/cloud no Cloudinary | **Sim** | - |
| `CLOUDINARY_API_KEY` | Chave de API do Cloudinary | **Sim** | - |
| `CLOUDINARY_API_SECRET` | Segredo de API do Cloudinary | **Sim** | - |
| `RESEND_API_KEY` | Chave de API do Resend | Não* | `default_api_key` (*necessária para envios reais) |
| `DB_USERNAME` | Usuário do PostgreSQL | Não | `myuser` |
| `DB_PASSWORD` | Senha do PostgreSQL | Não | `secret` |
| `DB_HOST` | Host do PostgreSQL | Não | `localhost` |
| `DB_PORT` | Porta do PostgreSQL | Não | `5432` |
| `DB_NAME` | Nome do banco de dados | Não | `mydatabase` |
| `JWT_EXPIRATION` | Tempo de expiração do JWT (em segundos) | Não | `300` |

---

## 2. Banco de Dados e Serviços (Docker Compose)

O projeto está configurado com Spring Boot, JPA, Flyway (para migrações automáticas), PostgreSQL e Redis.

### Via Docker Compose Integrado (Recomendado)
O projeto conta com o `compose.yaml` e a dependência do Spring Boot Docker Compose habilitada (`spring.docker.compose.lifecycle-management=start_only`).
Se você tiver o **Docker** rodando na máquina:
- Ao executar a aplicação (seja via IDE ou Maven), o Spring Boot iniciará os contêineres de **PostgreSQL** e **Redis** automaticamente.
- Caso prefira subir manualmente antes da aplicação:
  ```bash
  docker compose up -d
  ```

Os testes de integração utilizam o **Testcontainers**, levantando um contêiner descartável do banco durante o ciclo `mvn test`.

---

## 3. Rodando o Projeto

Após gerar o par de chaves RSA e definir as credenciais do Cloudinary e Resend:

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
