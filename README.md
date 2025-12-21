Traditional Court Management System (TCMS)

A Spring Boot backend for managing traditional court dispute cases, land stands, organizations, roles, users, residents and levies. The project follows a layered architecture with domain entities, repositories, services and REST controllers.

Key Features
- Dispute case lifecycle: open, file, notify, defend, assign adjudicators, close (see `DisputeCaseController`).
- Land stand management: list/search, apply, allocate, assign by council, mark fee paid (see `LandStandController`).
- Organizations hierarchy and case views (see `OrganizationController`).
- Users: list (paged), create, define heir, assign roles, appoint council (see `UserController`).
- Residents: generate proof of residence (see `ResidentController`).
- Levies: record payments and check status with validation (see `LevyController`).
- Pagination-ready endpoints with `PageResponse` wrapper and Spring Data `Pageable`.
- OpenAPI/Swagger UI preconfigured.

Tech Stack
- Java 17
- Spring Boot (parent 4.0.0) — Web MVC, Data JPA, Validation, Security (OAuth2 Resource Server)
- PostgreSQL
- Flyway (database migrations)
- Lombok
- Maven

Project Structure (high level)
/ (project root)
├─ pom.xml
├─ src
│  ├─ main
│  │  ├─ java/com/tbf/tcms
│  │  │  ├─ TcmsApplication.java              (Spring Boot entry point)
│  │  │  ├─ SecurityConfig.java               (JWT resource server + role mapping)
│  │  │  ├─ domain/                           (JPA entities)
│  │  │  ├─ repository/                       (Spring Data repositories)
│  │  │  ├─ service/ and service/impl/        (business logic)
│  │  │  └─ web/                              (REST controllers + handlers)
│  │  └─ resources/
│  │     ├─ application.properties            (App configuration)
│  │     └─ db/migration                      (Flyway SQL migrations)
│  └─ test/java/com/tbf/tcms/                 (Unit and slice tests)
└─ HELP.md

Prerequisites
- Java 17 (set JAVA_HOME accordingly)
- Maven 3.9+ (or use the included mvnw/mvnw.cmd wrapper)
- PostgreSQL 13+ running locally (or a reachable instance)

Configuration
- Default properties: `src/main/resources/application.properties`.
- Database and Flyway:
  - `spring.datasource.url=${SPRING_DATASOURCE_URL}`
  - `spring.jpa.hibernate.ddl-auto=validate` (schema validated against Flyway migrations)
  - Flyway enabled with `baseline-on-migrate=true`
- Security (JWT Resource Server):
  - `spring.security.oauth2.resourceserver.jwt.issuer-uri=${KEYCLOAK_ISSUER_URI}`
  - Exposed (permitAll): `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`
  - All other endpoints require authentication and appropriate roles.

### Local Development Setup (PowerShell)

Set environment variables for the current session:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/tbf_db"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "yourStrongPassword"
$env:KEYCLOAK_ISSUER_URI = "http://localhost:8181/realms/tcms-realm"
# Add others as needed
```

Then start the app:

```powershell
./mvnw spring-boot:run
```

Important: Make sure the database tbf_db exists before starting.

OpenAPI / Swagger
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
All controllers are annotated with `@Tag` and appear in Swagger. Use a Bearer token in the Authorize dialog.

Security & Roles
- The app is an OAuth2 Resource Server expecting JWTs (e.g., from Keycloak).
- Roles are mapped from the JWT claim `realm_access.roles` to Spring authorities `ROLE_<role>`.
- Roles used in the code: `ADMIN`, `CLERK`, `USER`.
  - Examples:
    - Levies: `POST /api/levies/{familyId}/payments` → roles `ADMIN` or `CLERK`.
    - Levies: `GET /api/levies/{familyId}/status` → roles `ADMIN`, `CLERK`, or `USER`.
    - Land stands and user admin operations typically require `ADMIN`.

Quickstart (local)
1) Start PostgreSQL and create the database (e.g., `tbf_db`).
2) Export datasource credentials and security config as env vars (see Local Development Setup section).
3) Run the app with Maven Wrapper:
   - Windows: `./mvnw.cmd spring-boot:run`
   - Linux/macOS: `./mvnw spring-boot:run`
4) Visit Swagger UI at `/swagger-ui.html`.

Build, Package and Test
- Build & package: `mvnw.cmd clean package` (Windows) or `./mvnw clean package` (Linux/macOS)
- Run tests: `mvnw.cmd test` or `./mvnw test`
- Run JAR: `java -jar target/tcms-0.0.1-SNAPSHOT.jar`

Pagination and Sorting
- List endpoints use Spring Data `Pageable` and return a `PageResponse` with total elements and content.
- Query parameters:
  - `page` (0-based), `size`
  - `sort` can appear multiple times. Examples:
    - `?page=0&size=20&sort=createdAt,desc`
    - `?page=0&size=20&sort=createdAt,desc&sort=name,asc`

API Overview (selected)
- Dispute Cases (`/api/cases`): open, file, notice, defense, adjudicators, close.
- Land Stands (`/api/stands` or `/api/land-stands`): list (search), allocate, apply, assign-by-council, mark fee paid.
- Organizations (`/api/organizations`): create, fetch hierarchy, list cases (paged, optional status).
- Users (`/api/users`): list (paged), create, disqualify, assign role, appoint council, define heir.
- Residents (`/api/residents`): generate proof of residence.
- Levies (`/api/levies`): record payment, check status.

Validation & Errors
- Bean Validation is used on request payloads. Example (levy):
  - `amount` is required and must be `> 0`; optional positive `year` defaults to current year.
- Validation and other exceptions are handled by `GlobalExceptionHandler` to return structured error JSON (e.g., `{ "amount": "amount is required" }`).

Example Request (curl)
```
curl -X POST "http://localhost:8080/api/levies/1/payments" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"amount": 25.00, "year": 2025}'
```

Development Notes
- Lombok is enabled; ensure your IDE has Lombok plugin installed and annotation processing turned on.
- Java version is pinned to 17 in `pom.xml`.
- Spring Boot parent version is `4.0.0` in `pom.xml`.

Troubleshooting
- Cannot connect to DB: verify PostgreSQL is running, credentials are correct, and the DB exists. Use env vars to override properties.
- Flyway validation errors: ensure the schema matches the migrations under `src/main/resources/db/migration`.
- Swagger not accessible: confirm the app runs on port 8080 and visit `/swagger-ui.html`.
- 401/403 responses: provide a valid JWT and ensure it carries the required role in `realm_access.roles`.

Contributing
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

License
This project currently has no explicit license. If you intend to make it open-source, consider adding a LICENSE file (e.g., MIT, Apache 2.0) and updating `pom.xml` metadata.

—
© 2025 The Bright Fusion — TCMS