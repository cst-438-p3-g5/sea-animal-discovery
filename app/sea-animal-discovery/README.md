# Expo client

The Expo client lives here. For the API, database, and full onboarding instructions, start at the [repository README](../../README.md) and [onboarding runbook](../../docs/runbook.md).

## Run locally

```bash
cp .env.example .env
npm ci
npx expo start
```

Press `w` for web, `a` for Android, or `i` for the iOS simulator on macOS. The `EXPO_PUBLIC_*` settings in `.env` are public client configuration; never put a provider secret, database password, or JWT signing key here.

GitHub sign-in setup is documented in [docs/github-sign-in.md](../../docs/github-sign-in.md). Google sign-in is not configured yet.

## Checks

```bash
npm test
npx expo lint
npx tsc --noEmit
```
