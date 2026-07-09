this java project aims to implement senior java developer home task from hiring employeer
see task here docs\task-assignment.md
This is features/to-do list i decided to implement:
**1)Game Engine microservice**

* `ConcurrentHashMap used as a repository`
* Move validation
* Win/draw detection
* `POST /games/{id}`
* `POST /games/{id}/move`
*  `GET /games/{id}`
* `@ControllerAdvice` — exception handling
* Java Doc/comments

**2)Session Service microservice**

* Use websocket for live updates to ui
* Reactive pipeline
* `WebClient` → Game Engine
* `POST /sessions` — create session, init game via Game Engine
* `POST /sessions/{id}/simulate` — 202, start async loop
* `GET /sessions/{id}` — return session \+ move history
* H2 — sessions table \+ moves table
* `@ControllerAdvice` — exception handling
* Java Doc/comments

**3)E2E integration test**

**4)Infrastructure**

* Eureka
* Spring Cloud Gateway
* `Docker compose`

**5)UI & Docs**

* `index.html`
* README — build, run, test \+ architecture overview

Here are coding instructions:
1)don't edit generated files inside build/
2)apply java spring boot app coding best practices