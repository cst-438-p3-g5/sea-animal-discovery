# Project onboarding runbook

Use this runbook to clone the repository, start the local API and database, run the tests, and record whether onboarding worked on a clean machine. The check-in uses the local PostgreSQL database in Docker; no Supabase or OAuth account is needed.

Before the check-in, a repository maintainer should create the GitHub issue label named `onboarding` (repository **Issues → Labels → New label**). Visitors use it on blocker reports and on the clean-run report.

## Requirements

- Git
- Docker Desktop or Docker Engine with the Compose v2 plugin, running
- Node.js 22 and npm (needed to generate the local JWT secret and run the app checks)
- An internet connection on the first run so Docker, Gradle, and npm can download dependencies

The Gradle wrapper manages the API's Java version. API integration tests use Testcontainers, so Docker must be available even when the Compose stack is stopped.

## Clone and configure

Run these commands from a terminal:

```bash
git clone https://github.com/cst-438-p3-g5/sea-animal-discovery.git
cd sea-animal-discovery
cp .env.example .env
```

In Windows PowerShell, use `Copy-Item .env.example .env` for the last command.

Generate a local signing key:

```bash
node -e "console.log(require('crypto').randomBytes(48).toString('base64'))"
```

Replace the `# JWT_SECRET=` line in the root `.env` with `JWT_SECRET=` followed by the generated value. Keep `.env` private and untracked. The local Compose database defaults are already configured in `compose.yml`.

## Start and check the API

Start PostgreSQL and the API from the repository root:

```bash
docker compose up --build
```

On API startup, Spring runs `api/src/main/resources/schema.sql` and then `api/src/main/resources/data.sql`. The schema creates or updates the tables, and the seed file inserts the 10 sample species (`sp-001` through `sp-010`). You do not need to paste seed SQL into PostgreSQL or run a separate seed command.

The seed inserts check whether each species ID already exists, so restarting the API is safe and adds newly defined IDs without duplicating existing rows. If you edit an existing species entry in `data.sql`, the startup seed will not overwrite that database row. To inspect the seed records in the local Compose database, open another terminal at the repository root and run:

```bash
docker compose exec db psql -U project03 -d project03 -c "SELECT id, common_name FROM species ORDER BY id;"
```

This command uses the default local database name and user from `compose.yml`; adjust them if you changed the `POSTGRES_DB` or `POSTGRES_USER` values in `.env`. A fresh database should show 10 rows. To add a sample species, add an insert to `api/src/main/resources/data.sql` with a new unique ID and restart the API. Existing database data persists across `docker compose down`; do not use `docker compose down -v` just to rerun seeds, because `-v` deletes the local database volume, including user and sighting data.

Wait for the API container to start. In another terminal, run these checks:

The commands below use `curl`; in Windows PowerShell, use `curl.exe`.

```bash
docker compose ps
curl -i http://localhost:8080/health
curl -i http://localhost:8080/actuator/health
curl -i http://localhost:8080/species
curl -i http://localhost:8080/species/sp-001
curl -i http://localhost:8080/users/me
```

Expected results:

- `/health` returns HTTP 200 and `{"status":"UP"}`.
- `/actuator/health` returns HTTP 200 with status `UP`; its `db` component confirms the API can reach PostgreSQL.
- `/species` returns HTTP 200 with the seeded catalog (10 species).
- `/species/sp-001` returns the Ochre sea star profile.
- `/users/me` returns HTTP 401 without a bearer token. This is expected and confirms the profile route requires authentication.

The API currently provides `GET /health`, `GET /species`, `GET /species/{id}`, `POST /auth/github`, `GET /users/me`, and authenticated `POST /sightings`. For this onboarding check, health and species endpoints are enough to confirm the local stack is working. Sign-in credentials are not needed to run the tests.

If a check fails, inspect the service state and logs:

```bash
docker compose ps
docker compose logs --tail=100 api db
```

## Run the test suite

Run API tests and the service coverage check:

```bash
cd api
./gradlew check --no-daemon
```

On Windows PowerShell, use `.\gradlew.bat check --no-daemon` instead of `./gradlew` while in the `api` directory. The API tests use Testcontainers and mock GitHub responses; live OAuth credentials are not required. A successful run ends with `BUILD SUCCESSFUL`.

Return to the repository root before the Expo checks:

```bash
cd ..
```

Run the Expo app checks from the repository root:

```bash
cd app/sea-animal-discovery
npm ci
npm test
npx expo lint
npx tsc --noEmit
```

`npm test` uses Node's built-in test runner and does not need the API to be running. For an optional browser smoke check, from `app/sea-animal-discovery`, copy `.env.example` to `.env`, run `npm run web -- --port 8081`, and open <http://localhost:8081>. Sign-in needs OAuth configuration; it is not part of this check-in.

## Record the check-in result

Keep a text log of each command and its result. If a step blocks you, file a separate GitHub issue for that problem and apply the `onboarding` label. Include the runbook step, exact command and error text, expected result, your operating system, and Docker version (`docker --version` and `docker compose version`).

If everything works, file one issue labeled `onboarding` saying the run was successful. Include the commands you ran, test results, operating system, and Docker version. If you host, acknowledge each issue filed against this repository before class ends by assigning an owner or replying with a plan.

## Stop the stack

Stop the foreground `docker compose up` with Ctrl+C, then run:

```bash
docker compose down
```

Do not add `-v` unless you intentionally want to delete the local PostgreSQL data volume.

