# mobi-bank-server

`mobi-bank-server` is a Kotlin/JVM banking backend built with Ktor. It exposes JSON HTTP APIs for authentication and user profiles, accounts, deposits, loans, and transactions, persisting application data in PostgreSQL. The repository is a server-side application, not an Android client; it has one Gradle application module and organizes code by feature within that module.

## Screenshots

This repository provides an HTTP API rather than a user interface. The images below are placeholders for API tooling or future client screenshots.

<table>
  <tr>
    <td align="center"><img src="/docs/images/screen1.png" alt="API or client screenshot placeholder" width="320"><br>API overview</td>
    <td align="center"><img src="/docs/images/screen2.png" alt="API or client screenshot placeholder" width="320"><br>Example client screen</td>
  </tr>
</table>

## Architecture

### Application shape

The server uses a feature-oriented, layered structure inside a single Kotlin/JVM Gradle module. Ktor routing functions handle HTTP requests and responses, feature services apply application rules and map persistence results, and repositories perform database operations through Exposed. Koin provides the application services and repositories at the composition root. This is not a multi-module Clean Architecture project: there are no separate `:domain`, `:data`, or Android `:app` Gradle modules, and the repository does not establish compile-time module boundaries.

Typical dependency direction within a feature is:

```text
Ktor route -> feature service -> feature repository -> DatabaseFactory / Exposed -> PostgreSQL
                         ^
                 Koin composition root
```

Routes and service/repository classes are grouped by feature, while server-wide concerns are installed through Ktor plugins. `Application.kt` starts the CIO server on `0.0.0.0:8081` and configures dependency injection, database connectivity, JWT authentication, routing, and JSON content negotiation. `application.yaml` also declares the Ktor module setup and port.

### Source structure

| Path | Responsibility |
| --- | --- |
| `src/main/kotlin/Application.kt` | JVM entry point and application plugin setup. |
| `src/main/kotlin/plugins/` | Ktor integration for routing, JWT authentication, database initialization, Koin, and JSON serialization. |
| `src/main/kotlin/di/AppModule.kt` | Koin bindings for feature repositories and services. |
| `src/main/kotlin/database/DatabaseFactory.kt` | HikariCP data source, Exposed connection, schema initialization, and coroutine-aware database query helper. |
| `src/main/kotlin/features/auth/` | Registration/login, JWT-backed profile access, profile updates, and password changes. |
| `src/main/kotlin/features/accounts/` | Account/card creation and listing. |
| `src/main/kotlin/features/deposits/` | Deposit operations and associated request/response/table models. |
| `src/main/kotlin/features/loans/` | Loan application, repayment, and associated models. |
| `src/main/kotlin/features/transactions/` | Transfers, top-ups, transaction history, and associated models. |
| `src/main/resources/application.yaml` | Ktor server port and application module configuration. |
| `src/main/resources/logback.xml` | Console logging configuration. |
| `src/test/kotlin/` | Feature service unit tests using mocked repositories. |
| `gradle/libs.versions.toml` | Version catalog for Kotlin/JVM dependencies. |

Feature packages contain route, service, repository, and model/table code as applicable. These are source-package boundaries in one application, not independently buildable modules. At a high level, HTTP concerns belong in routes, business validation and response mapping in services, and SQL/Exposed operations in repositories; dependencies are assembled in `di/AppModule.kt`.

![Architecture Diagram](docs/images/architecture_diagram.png)

## Tech Stack

Versions are taken from the Gradle build and version catalogs; Ktor dependencies use the Ktor catalog `3.5.0` declared in `settings.gradle.kts`.

| Category | Technologies |
| --- | --- |
| Language and runtime | Kotlin/JVM `2.3.21`, Java toolchain 21, Gradle wrapper `9.1.0`. |
| HTTP server | Ktor server `3.5.0`, CIO engine, YAML configuration, and server test host. |
| Serialization | Ktor ContentNegotiation with `kotlinx.serialization` JSON. |
| Architecture / dependency injection | Feature-level route/service/repository separation; Koin Ktor plugin and SLF4J logger (`3.5.3`). |
| Remote data / persistence | PostgreSQL JDBC driver (`42.7.7`), JetBrains Exposed core/JDBC/DAO/Java Time (`1.3.0`), and HikariCP (`5.1.0`). |
| Authentication / security | Ktor JWT authentication and Auth0 Java JWT (used by the security plugin); jBCrypt (`0.4`) for password hashing. |
| Asynchrony | Kotlin coroutines through Ktor and Exposed suspended transactions; `kotlinx-coroutines-test` for tests. |
| Logging | SLF4J integration through Koin and Logback Classic (`1.5.21`). |
| Testing | Kotlin test, JUnit 4, MockK (`1.13.10`), Ktor test host, Koin test support, H2 (`2.2.224`), and coroutines-test (`1.8.0`). |

No Android UI toolkit, Android Gradle Plugin, or Android-specific modules are configured in this repository.

## Setup & Build

### Prerequisites

- JDK 21. The Gradle build configures `jvmToolchain(21)`.
- Git and a terminal. Android Studio is not required; it can be used as a Kotlin/JVM IDE, but this project does not build an Android app.
- PostgreSQL reachable by the server, with a database named `mobibank_db` created before startup. The application initializes its Exposed tables at startup; it does not create the PostgreSQL database itself.
- Network access on the first Gradle run to download the Gradle distribution and dependencies.

The Gradle wrapper uses Gradle `9.1.0`; installing Gradle globally is unnecessary. The exact compatible IDE version is not specified by the repository.

### Clone and configure

```bash
git clone https://github.com/WantonOoze23/mobi-bank-server.git
cd mobi-bank-server
```

Configure PostgreSQL connection settings in the server process environment:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/mobibank_db"
export DB_USER="<your-postgres-user>"
export DB_PASSWORD="<your-postgres-password>"
```

`DB_URL` is optional in the current code and defaults to `jdbc:postgresql://localhost:5432/mobibank_db`; the database username and password also have source-code defaults. Set explicit values for your local environment. Although the repository contains a `.env` file, the build does not include a dotenv loader, so Gradle does not automatically load it; export variables in the process environment or use your own environment-management tool.

### Build and run

On macOS/Linux:

```bash
./gradlew build
./gradlew run
```

On Windows PowerShell or Command Prompt:

```bat
gradlew.bat build
gradlew.bat run
```

The server listens on port `8081` and binds to `0.0.0.0`. The root health response is available at `http://localhost:8081/`. API request examples are provided in `src/main/kotlin/requests.http` and `src/main/kotlin/TopUpAccount.http`.

### Configuration and security notes

- The checked-in `src/main/resources/application.yaml` configures port `8081`. The executable entry point also specifies that port directly.
- `DatabaseFactory` reads `DB_URL`, `DB_USER`, and `DB_PASSWORD` from the process environment and uses a PostgreSQL connection pool.
- JWT issuer, audience, and signing secret are currently defined as constants in `src/main/kotlin/plugins/Secutity.kt`. The values in `.env` are not wired into that JWT configuration. Do not deploy with the committed JWT secret or database credentials: rotate exposed credentials and move secrets into an appropriate secret-management mechanism before use outside a local development environment.

## Testing

The test source set contains service unit tests for authentication, accounts, deposits, loans, and transactions. Tests use MockK to isolate repositories and `runTest` for suspend functions. No Android or instrumented test source set is configured.

Run the unit tests from the repository root:

```bash
./gradlew test
```

For a complete build, including tests:

```bash
./gradlew build
```
