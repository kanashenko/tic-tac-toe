# CLAUDE.md

Working notes for this repo (what to know before touching code). See
`README.md` for the project overview

## Tech / decisions

- Java 25, Spring Boot 4, Gradle multi-module.
- **Spring WebFlux** (reactive) in session-service; Spring MVC in game-engine.
- **WebSocket** for pushing live game updates to the UI (SSE would also fit —
  see README; WebSocket chosen as the assignment lists it).
- **H2** in-memory for session/move persistence; ConcurrentHashMap for game state.
- Service discovery via **Eureka**, load-balanced `WebClient`.

## Conventions & gotchas

- session-service is reactive: never block the event loop. Blocking JPA calls go
  through `SessionPersistenceService` and are shifted onto
  `Schedulers.boundedElastic()`.
- The Game Engine game is created **lazily** — only when a session's simulation
  starts, not at session creation.
- Prefer constructor injection (Lombok `@RequiredArgsConstructor`), not field
  injection.
- Domain model and DTOs are **immutable Java records**; a move returns a new
  `Game`/`Board` rather than mutating shared state.
- JPA entities expose setters only for fields actually mutated (e.g. `status`,
  `winner`) — not `id` or timestamps.
- Map between layers with **MapStruct** (`@Mapper(componentModel = "spring")`);
  don't hand-roll mappers.
- Validate at the DTO boundary with Jakarta validation (`@Valid`, `@Pattern`,
  `@Min`/`@Max`) so bad input is a 400, not a 500.
- Surface errors as RFC 7807 `ProblemDetail` from `@RestControllerAdvice`, not
  ad-hoc error bodies.
- Concurrent game state stays consistent via `ConcurrentHashMap.compute`, not
  read-then-write.
- Tests: `StepVerifier` for reactive flows, `@WebMvcTest` slices for controllers,
  and the Testcontainers e2e for the full path; name test methods after the
  behavior they assert.

## Commands worth knowing

- `./gradlew test` runs unit + slice tests (no Docker); the end-to-end test is a
  separate module and needs Docker: `./gradlew :e2e-tests:test`.
