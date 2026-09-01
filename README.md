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

- **MySQL** — listings (relational, Flyway)
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
| `POST` | `/api/auth/mail` | SMTP adapter |
| `GET` | `/api/listings` | MySQL |
| `GET` | `/api/listings/{id}` | MySQL |
| `POST` | `/api/listings` | MySQL |
| `POST` | `/api/listings/{id}/inquiries` | MongoDB |
| `GET` | `/api/conversations?sellerId=` | MongoDB |
