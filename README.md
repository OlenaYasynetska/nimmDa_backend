# NimmDa Backend

Java 21 / Spring Boot 3.2, Clean Architecture (как `onlineSchool_backend`).

## Modules

| Module | Responsibility |
| --- | --- |
| `nimmda-domain` | Entities, value objects, repository ports |
| `nimmda-application` | Use cases |
| `nimmda-infrastructure` | JPA, MySQL, adapters |
| `nimmda-web` | REST API, Spring Boot entrypoint |

Роли и доступ добавим отдельно. Сейчас БД отключена: приложение стартует без MySQL.

## Run

```powershell
mvn -pl nimmda-web -am spring-boot:run
```

Health: [http://localhost:8080/api/health](http://localhost:8080/api/health)
