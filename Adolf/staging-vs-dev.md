# Staging vs development (Adolf M3)

| Concern | Development | Staging (Render free tier) |
|---|---|---|
| Database | Local PostgreSQL or H2 (`demo` profile) | Managed Postgres free tier; may sleep / expire |
| App process | `mvnw spring-boot:run` + Vite `npm run dev` | Single Docker image; Spring serves the Vite build from `/static` |
| JVM memory | Host machine defaults | Constrained free-tier RAM; jar starts with `-XX:MaxRAMPercentage=75.0` |
| Networking | localhost; Vite proxies `/api/v1` | Public HTTPS URL; API and UI same origin |
| Secrets | Root `.env` (gitignored) | Render environment variables only |
| Data durability | Local; disposable demo DB acceptable | Free DB can wipe on plan expiry — do not treat as production archive |
| Profile | `demo` or `postgres` | `postgres` with Flyway V1 + postgres V2 (append-only triggers) |

## What staging does not prove

- Production-scale concurrent municipal load
- Multi-instance session affinity / sticky sessions
- Long-term retention or backup restore drills beyond the free tier
