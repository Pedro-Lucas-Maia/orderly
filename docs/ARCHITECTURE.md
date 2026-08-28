# Guia de Arquitetura do Módulo de Autenticação

Este documento tem como objetivo explicar a arquitetura adotada no módulo de autenticação (`auth`) após a refatoração. O sistema foi redesenhado aplicando conceitos de **Domain-Driven Design (DDD)** e **Clean Architecture**, visando um código mais manutenível, testável e focado nas regras de negócio.

Como nossa equipe está acostumada com uma arquitetura em camadas tradicional, optamos por um meio-termo: as **Camadas de Serviço** foram mantidas (em vez de usar `UseCases`), mas as responsabilidades de negócio e persistência foram rigidamente separadas.

---

## Estrutura de Diretórios

O módulo está dividido em três grandes camadas: **Domain**, **Application** e **Infrastructure**.

```text
src/main/java/bti/pds/dinner/auth/
├── domain/            # 1. O coração do sistema (Regras de Negócio)
├── application/       # 2. Casos de Uso / Orquestração
└── infrastructure/    # 3. Detalhes técnicos (Banco, Web, Framework)
```

A regra de ouro (Regra de Dependência): **As dependências sempre apontam para dentro**. 
- O **Domain** não conhece nada além dele mesmo (não possui dependências do Spring, JPA ou Web).
- O **Application** conhece o Domain, mas não conhece a Infrastructure.
- A **Infrastructure** conhece as duas camadas acima.

---

## 1. Camada de Domínio (`domain`)

É a camada mais importante. Aqui vivem os modelos que representam a realidade do negócio. Em uma arquitetura tradicional (anêmica), nossas entidades (ex: `User`) eram apenas pacotões de `getters` e `setters` misturados com anotações do banco de dados (`@Entity`, `@Table`). 

No DDD, passamos a usar **Domínios Ricos**. 
- **Entidades Ricas:** Classes como `User` e `PasswordResetToken` não têm `setters` vazios. Qualquer mudança de estado acontece por meio de métodos de negócio (ex: `user.updatePassword(...)`, `token.validate()`).
- **Exceções de Domínio:** Se uma regra é quebrada, o domínio lança exceções focadas no negócio, como `InvalidPasswordException` ou `UserLockedException` (nada de vazar `ResponseStatusException` do Spring Web aqui!).
- **Repositórios (Interfaces):** As interfaces dos repositórios (`UserRepository`) também vivem aqui. O domínio dita *quais* métodos ele precisa para buscar dados, mas *não implementa* como eles são buscados no banco de dados.

## 2. Camada de Aplicação (`application`)

Esta camada é responsável por orquestrar os processos do sistema (como fazer login, atualizar perfil ou registrar usuário). Ela gerencia o fluxo delegando as regras de negócio para o Domínio.

- **`service/`:** Nossas famosas classes de `Service` (ex: `LoginService`, `ProfileService`) residem aqui. Elas não possuem nenhuma anotação de banco ou de web.
- **`input/` e `output/`:** Os serviços se comunicam usando estritamente objetos `Input` (o que entra) e `Output` (o que sai).
  - *Por que não usar as Requests do Controller direto?* Para desacoplar a web da aplicação. Se no futuro o sistema expor uma API GraphQL, ou um evento no RabbitMQ em vez de um Controller REST, o `Service` e o `Input` não precisarão mudar.

## 3. Camada de Infraestrutura (`infrastructure`)

Tudo que é detalhe técnico e que interage com o mundo externo vive aqui. Se envolver Spring, Banco de Dados, Bibliotecas de Email ou Configurações, está na infra.

- **`http/`:** 
  - **`controller/`:** Recebem as requisições HTTP e chamam os serviços.
  - **`request/` e `response/`:** Objetos de transferência da web. O Controller mapeia uma `Request` para um `Input` (geralmente usando métodos estáticos como `LoginRequest.toInput()`) para ser consumido pelo serviço. Depois, mapeia o `Output` retornado pelo serviço para uma `Response`.
- **`persistence/`:** 
  - **`entity/`:** Modelos anêmicos específicos para o Hibernate mapear no banco (`@Entity`, `@Id`, `UserEntity`).
  - **`repository/`:** Contém as interfaces do Spring Data (`JpaRepository`) e também a **implementação dos repositórios do domínio** (ex: `JpaUserRepository`). Essas implementações injetam o Spring Data, buscam os `Entity` do banco, convertem para o objeto de `Domain` (ex: `UserEntity.toDomain()`), e retornam para o `Service`.
- **`mail/` & `config/`:** Integrações externas (Resend, configs JWT, etc).

---

## Exemplo de Fluxo (Reset de Senha)

1. O cliente faz um POST no endpoint. O `AuthController` recebe um `ForgotPasswordRequest`.
2. O Controller converte isso para um `InitiatePasswordResetInput` e passa para o `PasswordResetService`.
3. O Service pede ao `UserRepository` (interface do domínio) para buscar o usuário.
4. O `JpaUserRepository` (na infra) usa o Spring Data para ir ao banco, pega o `UserEntity`, converte para o modelo de domínio `User`, e retorna para o Service.
5. O Service aplica as regras de negócio (criando um token, enviando evento) e salva o estado através do `PasswordResetTokenRepository`.
