# File-by-file guide — Smart Ticket Router

Every source file in `src/main`, grouped by package, in 2–3 lines each.

## Root (`com.example.ticket_router`)

- **`TicketRouterApplication.java`** — the `@SpringBootApplication` entry point. Also enables `OpenAiProperties` and `QdrantProperties` as bound configuration classes so they're available for injection everywhere.
- **`TestRunner.java`** — a `CommandLineRunner` that runs once on every startup: generates an embedding for a hardcoded sample ticket, searches Qdrant for similar ones, and runs the full routing pipeline, printing results to the console. Debug/demo scaffolding, safe to delete.
- **`QdrantTestRunner.java`** — another `CommandLineRunner` that calls `QdrantClient.createCollection()` on startup, so the Qdrant collection exists before anything tries to search or write to it. Not safe to delete — `DataInitializer` depends on the collection existing.

## `client/` — outbound API clients

- **`OpenAiEmbeddingClient.java`** — calls OpenAI's `/embeddings` endpoint with the `text-embedding-3-small` model and returns a 1536-float vector for a piece of text. Annotated `@Retry` so transient failures retry automatically.
- **`OpenAiTicketRoutingClient.java`** — sends the ticket message plus `TicketRoutingPrompt.SYSTEM_PROMPT` to OpenAI's chat completions API (`gpt-4o-mini`, JSON mode, `temperature: 0`) and returns the raw JSON classification. Also `@Retry`.
- **`QdrantClient.java`** — small interface abstracting "create the Qdrant collection if needed," implemented by `QdrantVectorClient`.
- **`QdrantVectorClient.java`** — the real Qdrant HTTP client: creates the 1536-dim cosine-distance collection, upserts a ticket's text + vector as a point, and searches for the top-3 most similar points to a given vector.
- **`TicketRoutingLlmClient.java`** — interface over "send a ticket message, get back raw JSON," implemented by `OpenAiTicketRoutingClient`. Exists so the routing service doesn't depend on OpenAI directly.

## `config/` — Spring bean and property wiring

- **`SecurityConfig.java`** — the `SecurityFilterChain`: which URLs need which `hasAuthority(...)`, the login/logout handlers, and the role-based post-login redirect (admin → `/admin`, department staff → `/department`, everyone else → `/`).
- **`SecurityBeans.java`** — defines the single `PasswordEncoder` bean (`BCryptPasswordEncoder`) used everywhere passwords are hashed or checked.
- **`DataInitializer.java`** — runs on startup: seeds the three default accounts (`admin`/`agent`/`user`) if the `users` table is empty, and seeds 5 example tickets into Qdrant so RAG has context immediately.
- **`OpenAiConfig.java`** — builds the `WebClient` bean used for all OpenAI calls, pre-configured with the base URL and `Authorization: Bearer <key>` header.
- **`OpenAiProperties.java`** — a `@ConfigurationProperties` record binding `openai.api-key` / `openai.base-url` / `openai.model` from `application.properties`.
- **`QdrantConfig.java`** — builds the `WebClient` bean used for all Qdrant calls, pointed at `qdrant.base-url`.
- **`QdrantProperties.java`** — binds `qdrant.base-url` / `qdrant.collection` from `application.properties`.
- **`JacksonConfig.java`** — exposes a plain `ObjectMapper` bean, used to parse the LLM's JSON response into `TicketRoutingResult`.

## `controller/` — HTTP layer

- **`PageController.java`** — serves `/`, the ticket submission page. Redirects anonymous users to `/login`; otherwise sets `userName`/`isAdmin`/`isDepartmentStaff` on the model for the shared navbar.
- **`TicketPageController.java`** — serves `/my-tickets`, a logged-in user's own ticket history, loaded via `TicketService.getTicketsForUser`.
- **`TicketController.java`** — `@RestController` REST endpoint, `POST /api/tickets/route`. Runs the AI classification via `TicketRoutingService` and, only if the caller is authenticated, saves the ticket via `TicketService`.
- **`AdminController.java`** — serves `/admin`: lists every ticket with priority filter/sort, gated on the `VIEW_ALL_TICKETS` permission, plus the `POST /admin/tickets/{id}/delete` endpoint that only allows deleting LOW priority tickets.
- **`AdminUserController.java`** — serves `/admin/users`: lets an admin create new staff/department accounts with a specific `UserType`, and lists/filters existing users by type.
- **`DepartmentController.java`** — serves `/department`: each department's own dashboard, scoped so staff only ever see tickets matching their `UserType`'s `departmentTeamName`; also handles status updates and rejected-ticket deletion.
- **`SignupController.java`** — public self-service signup at `/signup`, always creating a `CUSTOMER` account (never staff/admin — that's `AdminUserController`'s job).
- **`LoginController.java`** — just renders the `/login` page; the actual authentication is handled entirely by Spring Security's form-login filter.
- **`HealthController.java`** — `GET /health`, returns the literal string `"OK"`, for uptime checks.

## `service/` — business logic

- **`TicketRoutingService.java`** — orchestrates the whole RAG pipeline: validate → embed → search Qdrant → build enriched prompt → call the LLM → parse JSON into a `TicketRoutingResult`. Wraps any failure in `RoutingException`.
- **`EmbeddingService.java`** — thin validation/error-translation layer over `OpenAiEmbeddingClient`: rejects blank text, and turns a null/empty vector or client failure into a `RoutingException`.
- **`QdrantService.java`** — same pattern over `QdrantVectorClient`: validates inputs, returns a friendly fallback message when no similar tickets are found, and wraps failures in `RoutingException`.
- **`TicketService.java`** — persistence and business rules for tickets: save a routed ticket, get a user's or department's tickets, update status, and the two conditional delete rules (REJECTED-only for departments, LOW-only for admin).
- **`CustomUserDetailsService.java`** — Spring Security's bridge to the `User` entity: loads a user by username and builds their `GrantedAuthority` list (one `ROLE_<type>` plus one per granted permission).

## `entity/` — JPA-mapped tables

- **`User.java`** — the `users` table: username, hashed password, full name, `enabled` flag, and a `@ManyToOne` to `UserType`.
- **`UserType.java`** — the `user_type` table: a named type (`ADMIN`, `CUSTOMER`, `ENGINEERING_STAFF`, etc.), an optional `departmentTeamName`, and a `@ManyToMany` set of granted `Permission`s.
- **`Permission.java`** — the `permission` table: a named permission (e.g. `VIEW_ALL_TICKETS`) plus a description, granted to one or more `UserType`s.
- **`Ticket.java`** — the `ticket` table: message, category, priority, assigned team, reasoning, status, timestamp, and a `@ManyToOne` to the submitting `User`.

## `repository/` — Spring Data interfaces

- **`UserRepository.java`** — `findByUsername`, plus `findByUserType_Name` for the admin "filter users by type" screen.
- **`UserTypeRepository.java`** — `findByName` (used everywhere a `UserType` needs to be looked up by string) and `findByDepartmentTeamNameIsNotNull`.
- **`TicketRepository.java`** — every ticket query needed: by user, by priority, by assigned team (with and without a priority filter), all derived from method names.

## `dto/` — plain data carriers (not persisted)

- **`Priority.java`** — enum: `LOW`, `MEDIUM`, `HIGH`.
- **`TicketStatus.java`** — enum: `PENDING`, `ACCEPTED`, `REJECTED`, `IN_PROGRESS`, `COMPLETED`.
- **`TicketRequest.java`** — record wrapping the incoming `{ "message": "..." }` request body for `POST /api/tickets/route`.
- **`TicketRoutingResult.java`** — record for the AI's decision: `category`, `priority`, `assignedTeam`, `reasoning`. Also what the LLM's JSON reply gets parsed into.
- **`EmbeddingResponse.java`** — plain POJO matching OpenAI's `/embeddings` response shape, used only to deserialize that one API call.
- **`ErrorResponse.java`** — record for every error the API returns: timestamp, HTTP status, a short error label, the message, and the request path.

## `exception/` — custom exceptions + centralized handling

- **`InvalidTicketException.java`** — thrown for bad input (blank/too-long message, incomplete routing result, wrong ticket state for a delete).
- **`TicketNotFoundException.java`** — thrown when a ticket ID doesn't exist.
- **`UserNotFoundException.java`** — thrown when a username/ID doesn't resolve to a `User`.
- **`RoutingException.java`** — thrown when the embedding call, Qdrant call, LLM call, or JSON parsing fails.
- **`GlobalExceptionHandler.java`** — `@RestControllerAdvice` mapping each of the above to the right HTTP status (404/400/503) plus a catch-all 500, so nothing ever leaks a raw stack trace to the client.

## `prompt/`

- **`TicketRoutingPrompt.java`** — the entire system prompt sent to the LLM: the 5 categories and their teams, explicit HIGH/MEDIUM/LOW precedence rules, instructions for compound and vague/short messages, the strict JSON response format, and 5 worked examples.

## `resources/` — config, migrations, templates

- **`application.properties`** — all environment-dependent config: OpenAI key/model, Qdrant URL/collection, Postgres connection, Resilience4j retry/circuit-breaker tuning.
- **`logback-spring.xml`** — logging config: console output plus a rolling file appender (`logs/ticket-router.log`), rolling daily or at 10MB, 30 days retained.
- **`db/migration/V1__create_tables.sql`** — original schema: `user_profile` and `ticket` tables (superseded by later migrations, kept as history — never edit an applied migration).
- **`db/migration/V2__create_users_table.sql`** — adds the original `users` table with a fixed `role` column.
- **`db/migration/V3__merge_user_profile_into_users.sql`** — merges `user_profile` into `users`, auto-creating placeholder accounts for orphaned profile names, and repoints `ticket` at `users` directly.
- **`db/migration/V4__add_user_types_permissions_and_ticket_status.sql`** — the RBAC rework: adds `user_type`, `permission`, `user_type_permissions`; migrates `users.role` to `users.user_type_id`; adds `ticket.status`.
- **`templates/index.html`** — the ticket submission page and its JS `fetch` call to `/api/tickets/route`.
- **`templates/my-tickets.html`** — a user's own ticket history, including status badges.
- **`templates/admin.html`** — the admin dashboard: priority filter/sort pills, ticket list, LOW-priority delete button.
- **`templates/admin-users.html`** — the "Manage Users" screen: create-user form plus the existing-users list, filterable by type.
- **`templates/department.html`** — a department's own dashboard: priority filter, per-ticket status dropdown, and the rejected-only delete button.
- **`templates/login.html`** / **`templates/signup.html`** — the authentication pages, posting to Spring Security's form login and to `SignupController` respectively.
- **`templates/fragments/navbar.html`** — the single shared navbar fragment every page includes, showing different links depending on `isAdmin`/`isDepartmentStaff`.
- **`static/css/style.css`** — the entire shared design system: colors, cards, buttons, badges, filter pills, forms.

## `src/test` (not itemized file-by-file here)

16 test classes mirror this same package structure one-for-one (e.g. `TicketServiceTest` tests `TicketService`, `AdminControllerTest` tests `AdminController`). They're Mockito-based unit tests — no real database, Qdrant, or OpenAI call — covering validation rules, permission checks, and delete/update business logic. Ask if you'd like this same file-by-file treatment for the test package.
