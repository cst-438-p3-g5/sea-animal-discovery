# Google web and Android sign-in

## Configuration

The web and Android clients use the same API contract: both send an OAuth
authorization code, its PKCE verifier, and the exact redirect URI to
`POST /auth/google`. The API exchanges the code with Google, reads the
OpenID Connect user-info response, requires `email_verified: true`, and
returns the same app JWT response as GitHub sign-in.

1. Create a Google OAuth **Web application** client for the web app. Add the
   local web redirect URI `http://localhost:8081` and the production web
   origin's exact redirect URI.
2. Create a Google OAuth **Android** client for the package and signing
   certificate used by the development build. Configure the native redirect
   URI `seaanimaldiscovery:/oauthredirect` when the selected Google client
   supports it. Build the app as a development build; Expo Go cannot receive
   a custom app-scheme callback.
3. Set these API variables in the server environment. `GOOGLE_CLIENT_SECRET`
   and `GOOGLE_ANDROID_CLIENT_SECRET` never belong in the app environment.

   ```text
   GOOGLE_CLIENT_ID=<web-client-id>
   GOOGLE_CLIENT_SECRET=<web-client-secret>
   GOOGLE_ANDROID_CLIENT_ID=<android-client-id>
   GOOGLE_ANDROID_CLIENT_SECRET=
   GOOGLE_REDIRECT_URIS=http://localhost:8081
   GOOGLE_NATIVE_REDIRECT_URI=seaanimaldiscovery:/oauthredirect
   ```

   `GOOGLE_REDIRECT_URIS` may contain comma-separated web redirect URIs when
   more than one web deployment is active. The native URI is configured
   separately so the API can select the Android client without exposing a
   secret in the app.
4. Set the public client variables in
   `app/sea-animal-discovery/.env`:

   ```text
   EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID=<web-client-id>
   EXPO_PUBLIC_GOOGLE_WEB_REDIRECT_URI=http://localhost:8081
   EXPO_PUBLIC_GOOGLE_ANDROID_CLIENT_ID=<android-client-id>
   EXPO_PUBLIC_GOOGLE_NATIVE_REDIRECT_URI=seaanimaldiscovery:/oauthredirect
   ```

   Public client IDs are safe to bundle. Do not put a Google client secret,
   database credential, or JWT secret in an `EXPO_PUBLIC_*` variable.

## Flow and verification

The client uses `expo-auth-session` with a random state and S256 PKCE
challenge. Google returns a one-time authorization code to the web callback
or Android app scheme. The API sends the code and verifier to Google's token
endpoint, then calls the OpenID Connect user-info endpoint with the returned
access token. Provider tokens are never returned to the app or stored in the
database.

Run the API tests with `cd api && ./gradlew test`. Run the client checks with
`npm test`, `npm run lint`, and `npx tsc --noEmit` from
`app/sea-animal-discovery`. Manual verification requires a configured Google
client and an Expo development build for Android.
