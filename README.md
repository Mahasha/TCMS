Traditional Court Management System (TCMS)

A Spring Boot–based backend for managing traditional court dispute cases, land stands, organizations, roles, and users. The project follows a layered architecture with domain entities, repositories, and services.

Features
- Manage dispute cases with lifecycle status tracking (CaseStatus)
- Manage land stands with types (StandType)
- Manage organizations, roles, and users
- JPA-based persistence (PostgreSQL)
- Java Bean Validation ready

Tech Stack
- Java 17
- Spring Boot 4.x (Web MVC, Data JPA, Validation)
- PostgreSQL
- Lombok
- Maven

Project Structure
/ (project root)
├─ pom.xml
├─ src
│  ├─ main
│  │  ├─ java/com/tbf/tcms
│  │  │  ├─ TcmsApplication.java              (Spring Boot entry point)
│  │  │  ├─ domain/                           (JPA entities)
│  │  │  │  ├─ DisputeCase.java
│  │  │  │  ├─ LandStand.java
│  │  │  │  ├─ Organization.java
│  │  │  │  ├─ Role.java
│  │  │  │  ├─ User.java
│  │  │  │  └─ enums/ (CaseStatus, StandType)
│  │  │  ├─ repository/                       (Spring Data repositories)
│  │  │  ├─ service/                          (Service interfaces)
│  │  │  └─ service/impl/                     (Service implementations)
│  │  └─ resources/
│  │     └─ application.properties            (App configuration)
│  └─ test/java/com/tbf/tcms/                 (Sample tests)
└─ HELP.md

Prerequisites
- Java 17 (set JAVA_HOME accordingly)
- Maven 3.9+ (or use the included mvnw/mvnw.cmd wrapper)
- PostgreSQL 13+ running locally (or a reachable instance)

Configuration
Default configuration is in src/main/resources/application.properties:
spring.application.name=tcms
spring.datasource.url=jdbc:postgresql://localhost:5432/tbf_db
spring.datasource.username=postgres
spring.datasource.password=CHANGE_ME
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

Important
- For security, do not commit real passwords. Prefer environment variables or externalized config.
- You may override properties via environment variables, e.g. on Windows PowerShell:
  - setx SPRING_DATASOURCE_URL "jdbc:postgresql://localhost:5432/tbf_db"
  - setx SPRING_DATASOURCE_USERNAME "postgres"
  - setx SPRING_DATASOURCE_PASSWORD "yourStrongPassword"
- Ensure the database defined in spring.datasource.url exists.

Database
- JPA/Hibernate is configured with ddl-auto=update for convenience in development. For production, consider using migrations (e.g., Flyway or Liquibase) and set ddl-auto=validate.

Build and Run
Using Maven Wrapper (recommended):
- Run the application:
  - Windows: mvnw.cmd spring-boot:run
  - Linux/macOS: ./mvnw spring-boot:run
- Package (creates a fat JAR under target/):
  - Windows: mvnw.cmd clean package
  - Linux/macOS: ./mvnw clean package
- Run the packaged JAR:
  - java -jar target/tcms-0.0.1-SNAPSHOT.jar

Testing
- Run tests:
  - Windows: mvnw.cmd test
  - Linux/macOS: ./mvnw test

API Endpoints
This repository includes domain, repository, and service layers. To expose REST endpoints, add controllers under com.tbf.tcms.web or com.tbf.tcms.controller and use request/response DTOs as needed.

Development Notes
- Lombok is enabled; ensure your IDE has Lombok plugin installed and annotation processing turned on.
- Java version is pinned to 17 in pom.xml.
- Spring Boot version is managed via parent spring-boot-starter-parent 4.0.0.

Troubleshooting
- Cannot connect to DB: verify PostgreSQL is running, credentials are correct, and the DB exists.
- Lombok errors in IDE: install Lombok plugin and enable annotation processing.
- Port conflicts: set server.port in application.properties or via env var SERVER_PORT.

Contributing
1. Fork the repository
2. Create a feature branch: git checkout -b feature/your-feature
3. Commit your changes: git commit -m "feat: add your feature"
4. Push to the branch: git push origin feature/your-feature
5. Open a Pull Request

License
This project currently has no explicit license. If you intend to make it open-source, consider adding a LICENSE file (e.g., MIT, Apache 2.0) and updating pom.xml metadata.

—
© 2025 The Bright Fusion — TCMS