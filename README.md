# CST 438 Project 03 - Sea Animal Discovery

Sea Animal Discovery is an application that allows users to explore and learn about different sea animals. The project uses Expo to support Web, Android, and iOS from a single codebase.

## Project Structure

```text
sea-animal-discovery/
├── api/          # Backend API
├── app/          # Expo frontend (Web, Android, iOS)
├── docs/         # Project documentation
├── compose.yml   # Docker configuration
└── README.md
```

## Tech Stack

- **Frontend:** React Native, Expo, TypeScript
- **Navigation:** Expo Router
- **Backend:** REST API
- **Platforms:** Web, Android, iOS

## Getting Started

### Local API and database

First copy `.env.example` to `.env` and set a random `JWT_SECRET` (at least
32 bytes). See [GitHub sign-in setup](docs/github-sign-in.md) to configure OAuth.

```bash
docker compose up --build
```

This starts the API at <http://localhost:8080> and PostgreSQL on port `5432`.
Compose waits for PostgreSQL to pass its health check before starting the API.
The API health endpoint is <http://localhost:8080/health> and returns
`{"status":"UP"}` when the HTTP server is responding.
The database health endpoint is <http://localhost:8080/actuator/health>; its
`db` component reports whether the API can connect to PostgreSQL.
The database defaults are for local development and can be overridden with
`POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` environment variables.
Use `docker compose down` to stop the services. Add `-v` only when you also
want to remove the local PostgreSQL data volume.

To use Supabase instead, copy `.env.example` to `.env`, uncomment and fill in
the `SPRING_DATASOURCE_*` values from your Supabase project's **Connect** panel,
then run `docker compose up --build`. Use the JDBC **Session pooler** connection
on port `5432` with `sslmode=require`. Compose still starts its local PostgreSQL
container, but the API uses the Supabase connection when `SPRING_DATASOURCE_URL`
is set. Never commit `.env` or put database credentials in the client app.

### Run API checks

```bash
cd api
./gradlew check
```

On Windows, use `gradlew.bat check`.
This runs the API tests and JaCoCo service-layer coverage verification. Docker
must be running because the integration tests use Testcontainers.

### Frontend

The Expo app lives in `app/sea-animal-discovery`:

```bash
cd app/sea-animal-discovery
npm install
npx expo start
```

Once Expo starts, press:

- `w` to open Web
- `a` to open Android
- `i` to open iOS (macOS)
