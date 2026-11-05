# Architecture: Tidepool

## Architecture and hosting

```mermaid
flowchart LR
    subgraph providers["OAuth providers"]
        google["Google OAuth"]
        github["GitHub OAuth"]
    end

    subgraph clients["Tidepool clients"]
        web["Expo web client<br/>Hosted on Render Static Site"]
        android["Expo Android development build<br/>Run locally"]
    end

    api["Spring Boot REST API<br/>Hosted on Render"]
    db[("PostgreSQL database<br/>Hosted on Supabase")]

    web & android -- "Start sign-in: PKCE" --> google & github
    google & github -- "Authorization code" --> web & android

    web -- "POST /auth/github or /auth/google" --> api
    android -- "POST /auth/github or /auth/google" --> api
    api -- "Exchange code and verify identity" --> google & github
    api -- "Return app JWT" --> web & android

    web -- "Browse species and manage sightings" --> api
    android -- "Same API and features" --> api

    api -- "Read and write data via session pooler" --> db
```

| Component | Technology | Hosting |
|---|---|---|
| Web client | Expo (web) | Render Static Site |
| Android client | Expo Android development build | Run locally |
| API | Spring Boot REST API | Render |
| Database | PostgreSQL | Supabase (connected through the session pooler) |
| Sign-in | GitHub OAuth and Google OAuth, Authorization Code + PKCE | GitHub and Google |

Both clients call the same API. The clients never talk to the database directly, and the provider secrets stay on the API.

## Data model and API flow

This is the sign-in flow followed by the vertical slice (PRD-3): browse species, open a profile, record a sighting, and see it in "My Sightings." Endpoints are defined in [`openapi.yaml`](openapi.yaml), and tables in [`erd.md`](erd.md).

```mermaid
sequenceDiagram
    actor User
    participant Client as Expo Web or Android
    participant API as Spring Boot API
    participant OAuth as GitHub or Google
    participant DB as PostgreSQL

    User->>Client: Choose sign in
    Client->>OAuth: Authorization Code + PKCE
    OAuth-->>Client: Authorization code
    Client->>API: POST /auth/github or /auth/google
    API->>OAuth: Exchange code and verify identity
    OAuth-->>API: Verified user identity
    API-->>Client: App JWT

    Client->>API: GET /species
    API->>DB: Read species catalog
    DB-->>API: Species list
    API-->>Client: Species list

    Client->>API: GET /species/{id}
    API-->>Client: Species profile

    Client->>API: POST /sightings
    API->>DB: Save sighting for user and species
    DB-->>API: Saved sighting
    API-->>Client: Created sighting

    Client->>API: GET /sightings/me
    API-->>Client: Current user's sightings
```

Errors from any of these calls use the RFC 9457 Problem Details model in [`problem-details.md`](problem-details.md).