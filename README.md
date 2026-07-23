# Smart Ticket Router

An AI-powered support ticket triage app. Users submit a plain-language description of an issue; the app uses Retrieval-Augmented Generation (RAG) — semantic search over previously seen tickets plus an LLM call — to classify the ticket's category, priority, assigned team, and reasoning. A permission-based RBAC system then routes each ticket to the right department dashboard and controls who can see, update, or delete it.

## Tech stack

- **Java 21** / **Spring Boot 3.4.13**
- **Spring Security** — form login, self-service signup, permission-based access control (see [Role-based access control](#role-based-access-control) below)
- **Spring Data JPA** + **PostgreSQL** — persistence for users, user types/permissions, and tickets
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
3. The parsed result is returned to the browser and, if the caller is authenticated, the ticket is saved against their `User` account in PostgreSQL with an initial status of `PENDING`.

## Project structure

```
com.example.ticket_router
├─ client/       External API clients (OpenAI embeddings/completions, Qdrant)
├─ config/       Security, OpenAI/Qdrant config, Jackson, data seeding
├─ controller/   Web pages (login/signup/index/my-tickets/admin/admin-users/department) + REST API
├─ dto/          Request/response records and enums (Priority, TicketStatus)
├─ entity/       JPA entities: User, UserType, Permission, Ticket
├─ exception/    Custom exceptions + centralized @RestControllerAdvice handler
├─ prompt/       LLM system prompt for ticket classification
├─ repository/   Spring Data repositories
└─ service/      Business logic: embeddings, Qdrant search, routing, tickets, users
```

## Data model

- **users** — `username` (unique), BCrypt `password`, `full_name`, `enabled`, FK `user_type_id` → `user_type`. Used for authentication.
- **user_type** — a named account type (e.g. `ADMIN`, `CUSTOMER`, `ENGINEERING_STAFF`); optional `department_team_name` for staff types, matching the exact `assignedTeam` string the AI router produces. Replaces the old fixed `role` enum.
- **permission** — a named permission (e.g. `VIEW_ALL_TICKETS`) plus a description.
- **user_type_permissions** — many-to-many mapping between `user_type` and `permission`.
- **ticket** — `message`, `category`, `priority` (`LOW`/`MEDIUM`/`HIGH`), `assigned_team`, `reasoning`, `status` (`PENDING`/`ACCEPTED`/`REJECTED`/`IN_PROGRESS`/`COMPLETED`), `created_at`, FK `user_id` → `users`.

Schema is managed entirely through Flyway migrations in `src/main/resources/db/migration`; `spring.jpa.hibernate.ddl-auto` is set to `validate`, so entity changes must be paired with a new migration file, not left to Hibernate to auto-generate.

Migration history: `V1` created the original `user_profile`/`ticket` tables, `V2` added `users` with a fixed `role` column, `V3` merged `user_profile` into `users` (auto-creating placeholder accounts for any orphaned pre-login ticket data), and `V4` introduced the current `user_type`/`permission`/`user_type_permissions` RBAC schema plus the `ticket.status` workflow column.

## Role-based access control

Access is driven entirely by data, not a hardcoded role check. Each `User` has one `UserType`, and each `UserType` is granted zero or more `Permission`s. Spring Security authorizes requests against permission names directly (`hasAuthority(...)`), so adding a new department or capability is a matter of inserting rows, not changing security code.

Seeded user types:

| User type | Department team name | Notes |
|---|---|---|
| `ADMIN` | — | Full visibility, user management |
| `CUSTOMER` | — | Submits tickets, sees only their own |
| `ACCOUNTS_STAFF` | Accounts Department | |
| `ENGINEERING_STAFF` | Engineering Department | |
| `IAM_STAFF` | IAM Team | |
| `PRODUCT_STAFF` | Product Development Team | |
| `SUPPORT_STAFF` | Customer Service Team | |

Seeded permissions: `SUBMIT_TICKET`, `VIEW_OWN_TICKETS`, `VIEW_ALL_TICKETS`, `VIEW_DEPARTMENT_TICKETS`, `UPDATE_TICKET_STATUS`, `DELETE_REJECTED_TICKET`, `DELETE_LOW_PRIORITY_TICKET`, `MANAGE_USERS`. `ADMIN` gets `VIEW_ALL_TICKETS`/`MANAGE_USERS`/`DELETE_LOW_PRIORITY_TICKET`; `CUSTOMER` gets `SUBMIT_TICKET`/`VIEW_OWN_TICKETS`; every staff type gets `VIEW_DEPARTMENT_TICKETS`/`UPDATE_TICKET_STATUS`/`DELETE_REJECTED_TICKET`, scoped to their own department via `department_team_name`.

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

| Username | Password | User type |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `agent` | `agent123` | `SUPPORT_STAFF` |
| `user` | `user123` | `CUSTOMER` |

Change or remove these before using the app anywhere beyond local development. `DataInitializer` also seeds five example historical tickets into Qdrant so RAG has context to compare against right away — note that this seeding only happens once, at startup; tickets submitted afterward are used to *search* Qdrant but aren't written back into it, so the historical corpus doesn't grow from real usage.

## Using the app

- Go to `/login` to sign in, or `/signup` to self-register (public signup always creates a `CUSTOMER` account — staff/admin accounts can only be created by an admin).
- Customers land on the ticket submission page, describe an issue, and get an AI-generated classification back.
- **My Tickets** (`/my-tickets`) shows a customer's own submission history.
- Admins are redirected to `/admin` after login, see every ticket across all departments, can filter/sort by priority, and can delete `LOW` priority tickets.
- Admins manage accounts at `/admin/users` — create staff/department/admin accounts and filter the existing user list by type.
- Department staff (`ACCOUNTS_STAFF`, `ENGINEERING_STAFF`, `IAM_STAFF`, `PRODUCT_STAFF`, `SUPPORT_STAFF`) are redirected to `/department` after login, a shared dashboard scoped to only their own department's tickets, where they can update ticket status or delete tickets already marked `REJECTED`.

## Access control

| Route | Access |
|---|---|
| `/login`, `/signup`, `/css/**` | Public |
| `/admin/users/**` | `MANAGE_USERS` permission |
| `/admin`, `/admin/**` | `VIEW_ALL_TICKETS` permission |
| `/department/**` | `VIEW_DEPARTMENT_TICKETS` permission |
| Everything else (including `/`, `/my-tickets`, `/api/tickets/route`, `/health`) | Any authenticated user |

Note: `/health` currently falls under the authenticated-by-default rule rather than being explicitly public. If it's meant to back an uptime check that runs without credentials, it needs its own `permitAll()` entry in `SecurityConfig` — worth revisiting if external monitoring hits this endpoint.

## Logging

Application events (logins, logouts, signups, ticket submissions/routing results, admin/department dashboard access, and every handled exception) are logged via SLF4J/Logback to both the console and `logs/ticket-router.log`. The log file rolls daily or once it exceeds 10MB, keeping up to 30 days of history capped at 500MB total. Note: `TicketService`, `CustomUserDetailsService`, and `QdrantVectorClient` currently have little or no logging of their own, so some persistence and vector-store activity won't show up in the log file.

## Notes

- `QdrantTestRunner` is a `CommandLineRunner` bean that runs on every startup to ensure the Qdrant collection exists before anything reads/writes to it. `DataInitializer` depends on it — don't delete it.
- Passwords are hashed with BCrypt (`SecurityBeans`); never store or log raw passwords.
- Test suite: 78 Mockito-based unit tests covering controllers, services, and exception handling — no integration tests exercising the real Spring Security filter chain or database yet.
