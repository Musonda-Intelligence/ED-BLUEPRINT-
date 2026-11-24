# ED Blueprint API contract (Increment 1: authentication)

The Android app talks to the same cPanel-hosted backend as the website, so one
account works on both. The base address is `EDB_API_BASE_URL` in `gradle.properties`.
All requests and responses are JSON over HTTPS. Every request after login sends
`Authorization: Bearer <token>`.

The server is the only place that decides roles and subscriptions. The app just
shows what the server returns.

## POST /auth/register

Request:

```json
{ "fullname": "Jane Banda", "email": "jane@example.com", "password": "at-least-8-chars" }
```

Success (201): the new student is logged in straight away.

```json
{
  "token": "plain-text-api-token",
  "user": {
    "id": 12,
    "fullname": "Jane Banda",
    "email": "jane@example.com",
    "role": "student",
    "subscription_active": false,
    "subscription_expires_at": null
  }
}
```

Failure (422), shown to the student as-is:

```json
{ "message": "The given data was invalid.", "errors": { "email": ["That email is already registered."] } }
```

## POST /auth/login

Request: `{ "email": "...", "password": "..." }`

Success (200): same body as register.
Wrong details (401): `{ "message": "Incorrect email or password." }`
Too many attempts (429): the app tells the student to wait.

## GET /auth/me

Success (200): `{ "user": { ...same user object... } }`
Expired or revoked token (401): the app signs the student out.

## POST /auth/logout

Revokes the token on the server. Success (204 or 200).

## Rules the server must enforce

- `role` is `"student"` or `"admin"`, taken from the database only.
- `subscription_active` is calculated by the server from the subscription's expiry date every time.
- Passwords are stored hashed, never in plain text.
- Login and register are rate limited.


---

# Increment 2: content, viewers, quizzes (MVP)

All endpoints need `Authorization: Bearer <token>`. Lists return only **published** items.

## Materials

`MaterialDto`: `{ id, title, description, type, access, locked, topic_id, published_at }`

- `type`: `video`, `document`, `note`, `paper`, `marking_scheme`
- `access`: `free` or `premium`
- `locked`: server-calculated for this student (premium and no active subscription). Display only.

| Endpoint | Returns |
|---|---|
| `GET /topics` | `{ "topics": [{ id, title, description, material_count, quiz_count }] }` |
| `GET /topics/{id}/materials` | `{ "materials": [MaterialDto] }` |
| `GET /materials/recent` | `{ "materials": [MaterialDto] }` newest first |
| `GET /papers` | `{ "materials": [MaterialDto] }` past papers and marking schemes. Open to any logged-in student. |

## Protected file delivery

`GET /materials/{id}/file` streams the PDF or video.

- 401 if not logged in.
- **403 if the material is premium and the student's subscription is not active (or has expired).**
  This check is made on every request; the file must never be reachable by a public URL.
- Videos: support HTTP `Range` requests so the player can seek.
- Past papers (`access: free`) are served to any logged-in student.

## Quizzes

| Endpoint | Returns |
|---|---|
| `GET /quizzes` | `{ "quizzes": [{ id, title, duration_minutes, question_count, total_marks }] }` |
| `GET /quizzes/{id}` | `{ "quiz": { id, title, duration_minutes, questions: [{ id, text, marks, options: [{ id, text }] }] } }` (never includes correct answers) |
| `POST /quizzes/{id}/submit` | body `{ "answers": { "<question_id>": <option_id> } }` returns `{ "attempt": AttemptDto }` |
| `GET /quiz-attempts` | `{ "attempts": [AttemptDto] }` newest first |

`AttemptDto`: `{ id, quiz_id, quiz_title, score, total, submitted_at, review: [{ question, your_answer, correct_answer, correct, marks }] }`

The server marks the answers, stores the attempt, and rejects a submission that arrives
after `duration_minutes` (plus a few seconds of grace). The app never sends a score.
