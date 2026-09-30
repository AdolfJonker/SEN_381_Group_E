# CivicConnect backend (demo)

Spring Boot API for the CivicConnect UI.

## What was wrong before

1. `pom.xml` targeted **Java 25**, but this machine has **JDK 21** → Maven failed with `release version 25 not supported`.
2. There was **no web starter**, entities, migrations, or controllers — only an empty `DemoApplication`.
3. Postgres is installed, but the `postgres` password is unknown here, so the default **demo** profile uses **in-memory H2**.

## Run the demo

From this folder:

```powershell
.\mvnw.cmd spring-boot:run
```

API base URL: `http://localhost:8081/api`

Then in another terminal:

```powershell
cd ..\..\Frontend
npm run dev
```

Open the Vite URL and pick a demo role. The login screen loads users from the API.

### Demo users

| Role | Name | Id |
|---|---|---|
| Requester | Aisha Ndlovu | `u-req-1` |
| Staff | Johan Botha | `u-staff-1` |
| Management | Thandi Mokoena | `u-mgmt-1` |

Authenticated calls send header `X-User-Id: <id>` (demo auth only).

### Main endpoints

- `GET /api/users`
- `GET /api/requests`
- `GET /api/requests/{id}`
- `POST /api/requests`
- `POST /api/requests/{id}/assign`
- `POST /api/requests/{id}/status`
- `POST /api/requests/{id}/comments`
- `GET /api/summary`

## Optional Postgres profile

Create a database, set credentials, then:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/civicconnect"
$env:DB_USER="postgres"
$env:DB_PASSWORD="your-password"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```
