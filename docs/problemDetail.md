# Error Model: RFC 9457 Problem Details

Every error from the Tidepool API uses the same shape, defined by [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457). Clients can handle every error the same way instead of guessing at different formats.

- Content type: `application/problem+json`
- Schema: `ProblemDetails` in [`openapi.yaml`](openapi.yaml)

## Fields

| Field | Required | Meaning |
|---|---|---|
| `type` | Yes | URI that identifies the kind of problem, like `/problems/not-found` |
| `title` | Yes | Short summary that is the same every time this type happens |
| `status` | Yes | HTTP status code, repeated in the body |
| `detail` | No | Explanation specific to this request |
| `instance` | No | The request path that caused the problem |
| `errors` | No | List of `{ field, message }`, used only for validation errors |

## Error types

| Status | `type` | `title` | When it is used |
|---|---|---|---|
| 400 | `/problems/validation-error` | Validation failed | Request body is missing a required field or has a bad value (for example, a `sighting_date` in the future) |
| 401 | `/problems/unauthorized` | Unauthorized | No token, an invalid or expired token, or GitHub/Google rejected the sign-in code |
| 403 | `/problems/forbidden` | Forbidden | A user tries to view, edit, or delete another user's sighting (PRD-6) |
| 404 | `/problems/not-found` | Not found | The species or sighting id does not exist |
| 500 | `/problems/internal-error` | Internal server error | Something unexpected failed. `detail` never includes stack traces or SQL. |

## Examples

Validation error, from `POST /sightings`:

```json
{
  "type": "/problems/validation-error",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "instance": "/sightings",
  "errors": [
    { "field": "sighting_date", "message": "must not be in the future" }
  ]
}
```

Another user's sighting, from `PATCH /sightings/s-123`:

```json
{
  "type": "/problems/forbidden",
  "title": "Forbidden",
  "status": 403,
  "detail": "You can only view or change your own sightings.",
  "instance": "/sightings/s-123"
}
```

Unknown species, from `GET /species/sp-999`:

```json
{
  "type": "/problems/not-found",
  "title": "Not found",
  "status": 404,
  "detail": "No species with id sp-999.",
  "instance": "/species/sp-999"
}
```

## Spring Boot

Spring Boot 3 has built-in support through `ProblemDetail`. A `@RestControllerAdvice` class maps exceptions to these types, for example `MethodArgumentNotValidException` to 400 with the `errors` list filled in.