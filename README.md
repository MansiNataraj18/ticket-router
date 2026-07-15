# Smart Ticket Router

An AI-powered support ticket triage app. Users submit a plain-language description of an issue; the app uses Retrieval-Augmented Generation (RAG) — semantic search over previously seen tickets plus an LLM call — to classify the ticket's category, priority, assigned team, and reasoning.

## Tech stack

- **Java 21** / **Spring Boot 3.4.13**
- **Spring Security** — form login, self-service signup, role-based access (`ADMIN`, `AGENT`, `USER`)
- **Spring Data JPA** + **PostgreSQL** — persistence for users and tickets
- **Flyway** — versioned database migrations
- **Qdrant** — vector database for similarity search over historical tickets
- **OpenAI API** — text embeddings (`text-embedding-3-small`) and chat completion for ticket classification
- **Resilience4j** — retry and circuit breaker around OpenAI calls
- **Thymeleaf** — server-rendered UI
- **Logback** — structured, rolling application logs
- **Maven** (wrapper included, `./mvnw`)

## How ticket routing works (RAG flow)

1. A user submits a ticket message from the web UI (`POST /api/tickets/route`).
2. `TicketRoutingService` validates the message, then:
   - `EmbeddingService` calls OpenAI to generate a vector embedding of the message.
   - `QdrantService` searches Qdrant for similar historical tickets using that vector.
   - The similar tickets + the new message are combined into an enriched prompt.
   - `TicketRoutingLlmClient` sends the enriched prompt to the LLM, which returns strict JSON: `category`, `priority`, `assignedTeam`, `reasoning`.
3. The parsed result is returned to the browser and, if the user is authenticated, the ticket is saved against their `UserProfile` in PostgreSQL.

## Project structure

```
com.example.ticket_router
├─ client/       External API clients (OpenAI embeddings/completions, Qdrant)
├─ config/       Security, OpenAI/Qdrant config, Jackson, data seeding
├─ controller/   Web pages (login/signup/index/admin/my-tickets) + REST API
├─ dto/          Request/response records
├─ entity/       JPA entities: User, UserProfile, Ticket, Role
├─ exception/    Custom exceptions + centralized @RestControllerAdvice handler
├─ prompt/       LLM system prompt for ticket classification
├─ repository/   Spring Data repositories
└─ service/      Business logic: embeddings, Qdrant search, routing, tickets, users
```

## Data model

- **users** — `username` (unique), BCrypt `password`, `full_name`, `role` (`ADMIN`/`AGENT`/`USER`), `enabled`. Used for authentication.
- **user_profile** — `name` (unique). Used to associate submitted tickets with a person.
- **ticket** — `message`, `category`, `priority` (`LOW`/`MEDIUM`/`HIGH`), `assigned_team`, `reasoning`, `created_at`, FK to `user_profile`.

Schema is managed entirely through Flyway migrations in `src/main/resources/db/migration`; `spring.jpa.hibernate.ddl-auto` is set to `validate`, so entity changes must be paired with a new migration file, not left to Hibernate to auto-generate.

## Getting started

### Prerequisites

- Java 21
- PostgreSQL running locally with a `ticket_db` database created
- Qdrant running locally, e.g. `docker run -p 6333:6333 qdrant/qdrant`
- An OpenAI API key

### Environment variables

| Variable | Purpose |
|---|---|
| `LLM_API_KEY` | OpenAI API key |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |

### Key configuration (`application.properties`)

- `spring.datasource.url` — defaults to `jdbc:postgresql://localhost:5432/ticket_db`
- `qdrant.base-url` — defaults to `http://localhost:6333`
- `qdrant.collection` — defaults to `tickets`
- `openai.model` — defaults to `gpt-4o-mini`
- `resilience4j.*` — retry (3 attempts) and circuit breaker (opens at 50% failure rate over a 10-call window) around OpenAI calls

### Run it

```
export LLM_API_KEY=sk-...
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword

./mvnw spring-boot:run
```

The app starts at `http://localhost:8080`. Flyway migrations and default data seeding run automatically on startup.

### Default seeded accounts

On first run only (skipped if the `users` table already has rows), `DataInitializer` creates:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `agent` | `agent123` | AGENT |
| `user` | `user123` | USER |

Change or remove these before using the app anywhere beyond local development. It also seeds five example historical tickets into Qdrant so RAG has context to compare against right away.

## Using the app

- Go to `/login` to sign in, or `/signup` to self-register (new accounts are always created as `USER`).
- Regular users land on the ticket submission page, describe an issue, and get an AI-generated classification back.
- **My Tickets** (`/my-tickets`) shows a user's own submission history.
- Admins are redirected to `/admin` after login and can see every ticket submitted by every user.

## Access control

| Route | Access |
|---|---|
| `/login`, `/signup`, `/css/**` | Public |
| `/`, `/my-tickets` | Any authenticated user |
| `/admin` | `ROLE_ADMIN` only |
| `/api/tickets/route` (POST) | Any authenticated user |
| `/health` | Public — returns `OK` |

## Logging

Application events (logins, logouts, signups, ticket submissions/routing results, admin dashboard access, and every handled exception) are logged via SLF4J/Logback to both the console and `logs/ticket-router.log`. The log file rolls daily or once it exceeds 10MB, keeping up to 30 days of history capped at 500MB total.

## Notes

- `QdrantTestRunner` and `TestRunner` are `CommandLineRunner` beans that execute automatically on every startup (initializing the Qdrant collection and running a sample routing request for debugging). They're safe to delete once you no longer need them.
- Passwords are hashed with BCrypt (`SecurityBeans`); never store or log raw passwords.
