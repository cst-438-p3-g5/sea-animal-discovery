# GitHub web sign-in

## Configuration

1. Register a GitHub **OAuth App** in GitHub Settings → Developer settings.
   For local development, use `http://localhost:8081` for both the homepage and
   authorization callback URL. Use a separate OAuth App for production.
2. Copy the root `.env.example` to `.env`. Set `GITHUB_CLIENT_ID`,
   `GITHUB_CLIENT_SECRET`, and a random `JWT_SECRET` of at least 32 bytes.
   Generate a signing secret with
   `node -e "console.log(require('crypto').randomBytes(48).toString('base64'))"`.
   Keep these secrets on the API server. Compose reads this file; when starting
   Gradle directly, export the same variables into that process's environment.
3. Copy `app/sea-animal-discovery/.env.example` to `.env` in that same directory.
   Set `EXPO_PUBLIC_GITHUB_CLIENT_ID` to the same client ID. This ID is public.
   Never add a client secret or signing secret to any `EXPO_PUBLIC_*` variable.
4. Run `docker compose up --build` at the repository root. The API initializes
   the `app_user` table in PostgreSQL using `schema.sql` (existing rows survive).
5. In `app/sea-animal-discovery`, run `npm ci` then `npm run web -- --port 8081`.
   Open `http://localhost:8081`, allow the sign-in popup, and choose **Sign in with GitHub**.

For production, set `WEB_ORIGIN` to the exact HTTPS web origin (no trailing
slash), `GITHUB_REDIRECT_URI` and `EXPO_PUBLIC_GITHUB_REDIRECT_URI` to the exact
same homepage URL registered with GitHub, and `EXPO_PUBLIC_API_URL` to the
HTTPS API URL. Keep the redirect URL spelling, port, and trailing slash identical
across GitHub, API, and web configuration. Expo embeds public variables at build
time; rebuild after changing them. The callback uses the home route so no
additional static-host rewrite is required. HTTPS or localhost is required for
browser cryptography. Never log authorization codes, provider tokens, or JWTs.

## Flow and API

The web app uses Expo AuthSession to generate a random state and S256 PKCE
challenge. GitHub returns a code to the popup's home route, where
`maybeCompleteAuthSession` completes the session. AuthSession checks state
before the client submits the code and verifier to `POST /auth/github`:

```json
{"code":"github-code","code_verifier":"original-43-to-128-character-verifier","redirect_uri":"http://localhost:8081"}
```

The API only accepts its configured redirect URI. It exchanges the code with
GitHub using the server-held client secret and the supplied verifier, then calls
`/user` and `/user/emails`. At least one verified email is required, preferring
the verified primary email. The profile's public email alone is never trusted.
Provider errors return a sanitized Problem Details response; no provider token
or secret is sent to the web client or persisted.

Users are upserted by GitHub's immutable numeric ID, with an app UUID as the JWT
subject. Signing in again updates the verified email while preserving the app
ID and display name. Accounts are never merged by matching email addresses.

The response matches `docs/openapi.yaml`: `access_token` is an HS256 app JWT,
`token_type` is `Bearer`, and `user` has `id`, `email`, and `display_name`.
JWTs expire after one hour. The API validates signature, issuer, and expiry.
`GET /users/me` demonstrates authentication via `Authorization: Bearer <JWT>`;
the web **Check my profile** button uses the shared authenticated request helper.
Other API routes require authentication unless explicitly public in the security
configuration. Profile editing and deletion remain separate issues.

The web JWT lives only in React memory. Reloading or signing out clears the
local session; sign in again after reload or expiry. Signing out does not revoke
an already issued JWT; it remains valid until expiry. Rotating `JWT_SECRET`
invalidates all issued tokens. Missing or short signing keys fail startup;
missing GitHub credentials return 503 on sign-in. CORS only allows `WEB_ORIGIN`.

## Verification

- API: `cd api && ./gradlew test` (`gradlew.bat test` on Windows). Tests use
  isolated H2 schema initialization, mocked provider HTTP responses, and mocked
  user persistence for controller tests. No live OAuth credentials are needed.
- Web: in `app/sea-animal-discovery`, run `npm run lint`, `npx tsc --noEmit`,
  `npm test`, and `npx expo export --platform web`.
  The request-helper tests use Node 24's built-in TypeScript support.
- Manual with configured credentials: sign in using a verified GitHub email,
  check the profile, sign out, and sign in again. The app user ID should remain
  the same. Cancel/deny sign-in and confirm the retry works. An account without
  verified email must be rejected. Reload clears the session. A missing,
  altered, or expired JWT must receive 401 from `/users/me`.

Provider HTTP tests cover verifier/secret forwarding, verified primary and
secondary emails, unverified-email rejection, code rejection, and outages.
Controller tests cover JWT issuance/use, tampering, expiry, issuer, malformed
requests, redirect validation, and allowed/disallowed CORS origins. Live GitHub
authorization and PostgreSQL persistence require the manual configuration above.

References: [GitHub OAuth web flow](https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/authorizing-oauth-apps),
[Expo SDK 58 AuthSession](https://docs.expo.dev/versions/v58.0.0/sdk/auth-session/).
