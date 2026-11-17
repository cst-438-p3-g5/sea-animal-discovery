# CST 438 Project 03 — Sea Animal Discovery

Sea Animal Discovery is a web and mobile app for exploring sea animals and recording sightings. The repo contains an Expo client, a Spring Boot API, and a PostgreSQL database.

## Clone and start the local API

```bash
git clone https://github.com/cst-438-p3-g5/sea-animal-discovery.git
cd sea-animal-discovery
cp .env.example .env
```

Generate a local JWT signing key with the command below, then replace the commented `# JWT_SECRET=` line in `.env` with `JWT_SECRET=` followed by the generated value:

```bash
node -e "console.log(require('crypto').randomBytes(48).toString('base64'))"
```

Start the API and local database:

```bash
docker compose up --build
```

The API listens on <http://localhost:8080>. Check <http://localhost:8080/health> for API availability and <http://localhost:8080/actuator/health> for API and database health. See the [onboarding runbook](docs/runbook.md) for the full setup, API walkthrough, test commands, and issue-report format.

The clone command checks out the repository's default branch. For the class check-in, use the documented default branch and follow the runbook from top to bottom.

## Tests

The API tests use Testcontainers, so Docker must be running:

```bash
cd api
./gradlew check
cd ..
```

On Windows PowerShell, run `.\gradlew.bat check` from `api`.

The app tests and static checks use Node.js 22:

```bash
cd app/sea-animal-discovery
npm ci
npm test
npx expo lint
npx tsc --noEmit
```

## Run the app

From the repository root, copy the Expo example environment file and start the app:

```bash
cd app/sea-animal-discovery
cp .env.example .env
npm ci
npx expo start
```

Press `w` for web, `a` for Android, or `i` for the iOS simulator on macOS. For real GitHub sign-in, configure the API and Expo OAuth values using the [GitHub sign-in guide](docs/github-sign-in.md). OAuth is not required to start the API or run automated tests.
