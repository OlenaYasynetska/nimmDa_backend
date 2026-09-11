# NimmDa Backend

Java 21 / Spring Boot 3.2. **DDD + Clean Architecture + Hexagonal.**

Unlike a mixed layout (JPA/Mongo/mail inside `web`), every driven adapter lives in `nimmda-infrastructure`. `nimmda-web` only maps HTTP.

## Modules

```
nimmda-web              driving adapters (REST)
        │
        ▼
nimmda-application      use cases (inbound ports)
        │
        ▼
nimmda-domain           aggregates, VOs, repository ports
        ▲
        │
nimmda-infrastructure   driven adapters: MySQL/JPA, MongoDB, SMTP, Flyway
```

| Module | May depend on | Must not contain |
| --- | --- | --- |
| `nimmda-domain` | nothing | Spring, JPA, Mongo, HTTP |
| `nimmda-application` | domain | JPA entities, Mongo documents, controllers |
| `nimmda-infrastructure` | domain + application | REST controllers |
| `nimmda-web` | application + infrastructure (composition root) | business rules, persistence |

Persistence split:

- **MySQL** — users, listings (relational, Flyway)
- **MongoDB** — conversations / inquiries (document threads)

## Docker

Start MySQL + MongoDB + API:

```powershell
docker compose up --build
```

Only databases (then run Spring from the IDE / Maven). MySQL is published on **3307** so it does not clash with a local MySQL on 3306:

```powershell
docker compose up -d mysql mongo
mvn -pl nimmda-web -am spring-boot:run
```

Health: [http://localhost:8080/api/health](http://localhost:8080/api/health)

Listings: [http://localhost:8080/api/listings](http://localhost:8080/api/listings)

## API

| Method | Path | Store |
| --- | --- | --- |
| `GET` | `/api/health` | — |
| `POST` | `/api/auth/register` | MySQL + mail (unverified user) |
| `POST` | `/api/auth/login` | MySQL (JWT, only if verified) |
| `POST` | `/api/auth/verify` | MySQL (activate only, no JWT) |
| `GET` | `/api/auth/verify-email` | MySQL (activate only, no JWT) |
| `POST` | `/api/auth/forgot-password` | MySQL + mail |
| `POST` | `/api/auth/reset-password` | MySQL |
| `POST` | `/api/auth/resend-verification` | MySQL + mail |
| `GET` | `/api/listings` | MySQL |
| `GET` | `/api/listings/mine` | MySQL (JWT) |
| `GET` | `/api/listings/{id}` | MySQL |
| `POST` | `/api/listings` | MySQL (JWT) |
| `PATCH` | `/api/listings/{id}` | MySQL (JWT) |
| `POST` | `/api/listings/{id}/inquiries` | MongoDB (JWT) |
| `GET` | `/api/conversations` | MongoDB (JWT) |
| `POST` | `/api/conversations/{id}/messages` | MongoDB (JWT) |

The Super Admin login is not stored in MySQL or Git. Set `SUPER_ADMIN_EMAIL` and `SUPER_ADMIN_PASSWORD` in Railway Variables (and locally in `.env`).
