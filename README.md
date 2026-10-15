# NimmDa Backend

Java 21 / Spring Boot 3.2.5. **DDD + Clean Architecture + Hexagonal.**

`nimmda-web` only maps HTTP. Driven adapters live in `nimmda-infrastructure`.
Public site: [https://www.nimmda.org](https://www.nimmda.org) and [https://nimmda.org](https://nimmda.org). Production variables are set on Railway (see `.env.example`).

## Modules

```
nimmda-web              driving adapters (REST, security)
        │
        ▼
nimmda-application      use cases (inbound ports)
        │
        ▼
nimmda-domain           aggregates, VOs, repository ports
        ▲
        │
nimmda-infrastructure   driven adapters: MySQL/JPA, MongoDB, mail, geocoder, Flyway
```

| Module | May depend on | Must not contain |
| --- | --- | --- |
| `nimmda-domain` | nothing | Spring, JPA, Mongo, HTTP |
| `nimmda-application` | domain | JPA entities, Mongo documents, controllers |
| `nimmda-infrastructure` | domain + application | REST controllers |
| `nimmda-web` | application + infrastructure (composition root) | business rules, persistence |

Persistence:

- **MySQL** — users, auth tokens, listings, favorites, locations (Flyway, `ddl-auto: validate`)
- **MongoDB** — conversations / inquiries
- **Nominatim** — place search (`GET /api/locations`), user-agent `NimmDa/1.0 (https://nimmda.org)`

Mail: if `RESEND_API_KEY` is set, messages go through Resend; otherwise through SMTP when `MAIL_USERNAME` / `MAIL_PASSWORD` are set. `MAIL_ENABLED` defaults to `false`. A successful Top or Profi payment emails a Zahlungsbeleg to the account address. A failed payment does not start the service. The badge ends on its own when `promotedUntil` or `activeUntil` is in the past.

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

Copy `.env.example` → `.env`. Do not commit secrets.

Health: [http://localhost:8080/api/health](http://localhost:8080/api/health)

Listings: [http://localhost:8080/api/listings](http://localhost:8080/api/listings)

## Configuration

Defaults are in `nimmda-web/src/main/resources/application.yml`. Override with environment variables.

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port (`PORT` on Railway) |
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DB` | `127.0.0.1` / `3307` / `nimmda` | MySQL |
| `MYSQL_USER` / `MYSQL_PASSWORD` | `root` / `root` | MySQL credentials |
| `MONGODB_URI` | `mongodb://127.0.0.1:27017/nimmda` | MongoDB |
| `FRONTEND_BASE_URL` | `http://localhost:4200` | Links in auth emails. Production: `https://www.nimmda.org` |
| `CORS_ALLOWED_ORIGINS` | localhost, `nimmda.org`, `www.nimmda.org`, Capacitor origins | Browser and app origins |
| `JWT_SECRET` / `JWT_TTL_MS` | dev secret / 24h | JWT. Set a long random secret in production |
| `SUPER_ADMIN_EMAIL` / `SUPER_ADMIN_PASSWORD` | empty | Super Admin is not stored in MySQL or Git |
| `RESEND_API_KEY` | empty | Resend (preferred mail path) |
| `MAIL_FROM` | `NimmDa <noreply@nimmda.org>` | From address |
| `MAIL_ENABLED` | `false` | Turn sending on |
| `AUTH_EXPOSE_DEV_LINKS` | `false` (compose default `true`) | Show verify/reset links in API responses when mail is off |
| `GEOCODER_ENABLED` | `true` | Nominatim |
| `STRIPE_SECRET_KEY` | empty | Stripe Checkout für Top-Anzeigen (4,99 €, 7 Tage) |
| `STRIPE_WEBHOOK_SECRET` | empty | Signatur von `POST /api/payments/stripe/webhook` |

## API

JWT is a Bearer token. Anything under `/api/**` that is not listed as public requires authentication. `/api/admin/**` also requires role `ADMIN`.

| Method | Path | Auth | Store |
| --- | --- | --- | --- |
| `GET` | `/api/health` | public | — |
| `POST` | `/api/auth/register` | public | MySQL + mail |
| `POST` | `/api/auth/login` | public | MySQL (JWT only if the email is verified) |
| `POST` | `/api/auth/verify` | public | MySQL (activate, no JWT) |
| `GET` | `/api/auth/verify-email` | public | MySQL (activate, no JWT) |
| `POST` | `/api/auth/forgot-password` | public | MySQL + mail |
| `POST` | `/api/auth/reset-password` | public | MySQL |
| `POST` | `/api/auth/resend-verification` | public | MySQL + mail |
| `GET` | `/api/listings` | public | MySQL. Query: `q`, `category`, `location`, `minPrice`, `maxPrice`, `sort`, `free`, `km`, `page`, `size` |
| `GET` | `/api/listings/{id}` | public | MySQL |
| `GET` | `/api/listings/mine` | JWT | MySQL |
| `GET` | `/api/my/listings` | JWT | MySQL (same seller list) |
| `POST` | `/api/listings` | JWT | MySQL |
| `PUT` | `/api/listings/{id}` | JWT | MySQL |
| `PATCH` | `/api/listings/{id}` | JWT | MySQL (status) |
| `DELETE` | `/api/listings/{id}` | JWT | MySQL |
| `POST` | `/api/listings/{id}/promotion-checkout` | JWT | Stripe Checkout, 4,99 € / 7 Tage Top |
| `POST` | `/api/promotions/checkout-session` | JWT | bestätigt die Rückkehr von Stripe und setzt `promotedUntil` |
| `POST` | `/api/payments/stripe/webhook` | Stripe-Signatur | aktiviert Top oder Profi-Profil nach `checkout.session.completed` |
| `GET` | `/api/business-profiles` | public | aktive Firmen, optional `location` |
| `GET` | `/api/business-profiles/{userId}` | public | Profi-Profil und aktive Anzeigen, auch nach Ablauf |
| `GET` | `/api/business-profiles/mine` | JWT | eigenes Profi-Profil |
| `PUT` | `/api/business-profiles/mine` | JWT | Firma, Branche, Ort, Telefon |
| `POST` | `/api/business-profiles/checkout` | JWT | Stripe Checkout, 19,99 € / 30 Tage |
| `POST` | `/api/business-profiles/checkout-session` | JWT | bestätigt die Rückkehr von Stripe |
| `GET` | `/api/homepage-banner` | public | ein Startseiten-Platz, nur innerhalb von `startsOn`–`endsOn` (Europe/Vienna) |
| `GET` | `/api/admin/homepage-banner` | admin | derselbe Platz mit beiden Daten |
| `PUT` | `/api/admin/homepage-banner` | admin | Firma, Bild, Link, sichtbar ab, sichtbar bis. 99 € / 30 Tage werden außerhalb abgerechnet |
| `GET` | `/api/locations?q=` | public | MySQL + Nominatim |
| `GET` | `/api/favorites` | JWT | MySQL |
| `POST` | `/api/favorites/{listingId}` | JWT | MySQL |
| `DELETE` | `/api/favorites/{listingId}` | JWT | MySQL |
| `POST` | `/api/listings/{listingId}/inquiries` | JWT | MongoDB |
| `GET` | `/api/conversations` | JWT | MongoDB |
| `GET` | `/api/conversations/{id}` | JWT | MongoDB |
| `POST` | `/api/conversations/{id}/messages` | JWT | MongoDB |
| `GET` | `/api/admin/overview` | admin | MySQL |
| `GET` | `/api/admin/users` | admin | MySQL |
| `GET` | `/api/admin/payments` | admin | empty |
| `GET` | `/api/admin/advertisers` | admin | bezahlte Top-Anzeigen: E-Mail, Anzeige, Betrag, bezahlt am, Top bis |
| `GET` | `/api/admin/subscriptions` | admin | bezahlte Profi-Profile: E-Mail, Firma, Betrag, bezahlt am, aktiv bis |
