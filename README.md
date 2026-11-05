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

```bash
docker compose up --build
```

This starts the API at <http://localhost:8080> and PostgreSQL on port `5432`.
Compose waits for PostgreSQL to pass its health check before starting the API.
The API health endpoint is <http://localhost:8080/health> and returns
`{"status":"UP"}` when the HTTP server is responding.
The database defaults are for local development and can be overridden with
`POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` environment variables.
Use `docker compose down` to stop the services. Add `-v` only when you also
want to remove the local PostgreSQL data volume.

### Run API tests

```bash
cd api
./gradlew test
```

On Windows, use `gradlew.bat test`.

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
