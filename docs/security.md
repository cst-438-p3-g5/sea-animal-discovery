# Security

This page records the main security threats for Tidepool and the controls that
reduce them. Automated checks and tests provide evidence for the controls; they
do not replace secure repository settings or careful review.

## Threats and mitigations

1. **OAuth credentials or signing keys are committed or exposed to the app.**
   Keep provider credentials and `JWT_SECRET` on the API, outside the mobile
   client and source control. Local development uses an untracked `.env`, and
   API configuration rejects JWT signing keys shorter than 32 bytes. The
   [GitHub sign-in guide](github-sign-in.md) documents secret handling and the
   [Gitleaks workflow](../.github/workflows/gitleaks.yml) scans repository
   history on every pull request. An administrator should also enable
   [secret-scanning push protection](https://github.com/cst-438-p3-g5/sea-animal-discovery/settings/security_analysis)
   in **Settings → Code security and analysis → Secret Protection → Push
   protection**; this repository setting is not changed by this project file.

2. **An attacker forges or reuses an invalid app token, or intercepts the
   OAuth sign-in flow.** The API validates OAuth authorization codes with a
   PKCE verifier, signs short-lived HS256 JWTs, and validates token signature,
   issuer, and expiry. The client keeps tokens in memory. See [authentication
   implementation](../api/src/main/java/com/example/project03/auth/AuthService.java),
   [authentication API tests](../api/src/test/java/com/example/project03/auth/AuthApiIntegrationTests.java),
   and [client auth helper tests](../app/sea-animal-discovery/tests/api.test.mjs).

3. **Unauthenticated or cross-origin requests access protected API data.**
   Protected routes require stateless bearer authentication, and CORS permits
   only the configured web origin. The [security configuration](../api/src/main/java/com/example/project03/auth/AuthConfig.java)
   defines these rules. [API integration tests](../api/src/test/java/com/example/project03/auth/AuthApiIntegrationTests.java)
   cover missing credentials and an unapproved preflight origin.

4. **Malformed input or SQL injection corrupts records or exposes another
   user's sightings.** Validate request fields and dates before writing, use
   parameterized `JdbcClient` statements, and derive the sighting owner from
   the authenticated JWT subject. See the [sighting endpoint](../api/src/main/java/com/example/project03/sighting/SightingController.java),
   [sighting repository](../api/src/main/java/com/example/project03/sighting/SightingRepository.java),
   and [sighting API integration tests](../api/src/test/java/com/example/project03/sighting/SightingApiIntegrationTests.java).

5. **A vulnerable third-party dependency introduces a known exploit.**
   Dependabot checks the Gradle project under `api/` and npm project under
   `app/sea-animal-discovery/` weekly using [Dependabot configuration](../.github/dependabot.yml).
   The [CI workflow](../.github/workflows/ci.yml) runs API tests, an API image
   build, and Expo checks on pull requests. Review and merge Dependabot update
   pull requests through the team's normal review process; configuration alone
   does not demonstrate that an update has been reviewed or merged.

## GitHub settings and follow-up

- Enable secret-scanning push protection in the repository's **Settings → Code
  security and analysis → Secret Protection** page. This requires an
  administrator with access to repository security settings. The repository
  setting could not be verified from this local change, so push protection is
  not claimed as enabled.
- Review and merge at least one Dependabot pull request to satisfy the issue's
  remaining operational acceptance criterion.
