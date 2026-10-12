# CivicConnect source layout

```text
src/
  frontend/
    views/                 All user-facing screens
    api/                   Calls to the backend (uses VITE_API_BASE_URL from .env)
    assets/
    styles/

  backend/                 Spring Boot modular monolith
    pom.xml
    src/main/java/civicconnect/
      api/                 REST controllers
      services/            Business actions
      observers/           Status-change reactions
      strategies/          Role-based request filtering
      records/             Database entities and enums
      repositories/        Database access
      security/            Login and role checks
      config/              Loads root .env before startup
    src/main/resources/
      application.yml      Datasource settings from .env
      db/migration/        Flyway schema scripts
```

## Database setup

1. Copy `.env.example` to `.env` at the project root (already created locally).
2. Set `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD`.
3. Create an empty PostgreSQL database with that name.
4. From `src/backend`, run: `./mvnw spring-boot:run` (or `mvn spring-boot:run`).
5. Open `http://localhost:8080/api/v1/health` — `database` should be `UP`.

To run without PostgreSQL, set `SPRING_PROFILES_ACTIVE=demo` in `.env` (in-memory H2).
