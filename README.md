# Computer Parts Inventory System

## A Java inventory system with three interchangeable storage backends and a REST API built entirely from scratch with no Spring boot, ORM or mocking framework.
This project includes thorough JUnit testing with 57 tests in total that run automatically when a branch is merged or there is a push to main.
A Multi-stage Docker build which can be run on any device without worrying about dependencies. 

![Screenshot 2026-07-17 121245.png](images/Screenshot%202026-07-17%20121245.png)
## Getting Started

**Prerequisites:** JDK 26, Maven 3.9+ (Docker optional). JDK 26 is required specifically because the entry points use instance `main()` methods (JEP 512), a language feature not available on older JDKs.

```bash
git clone https://github.com/ravigillwork-dot/InventorySystem.git
cd InventorySystem
mvn clean package
```

**Run the REST API:**
```bash
java -jar target/ComputerPartInventory-1.0-SNAPSHOT.jar
```
Server starts at `http://localhost:8080/products`.

**Run the CLI:** the packaged jar's manifest defaults to `MainApi`, so the CLI is launched by naming it explicitly:
```bash
java -cp target/ComputerPartInventory-1.0-SNAPSHOT.jar MainCLI
```

## How to run docker (Must have docker set up): 

```bash
docker -build -t inventory


docker run -it -p 8080:8080 inventory
```
If you would like the data to persist and not disappear when the docker container is shut down use the following to start the container.
This will create a folder named dataInventory in the directory with a SQLite database file. 

```bash
docker run -it -p 8080:8080 -v ${PWD}\dataInventory:/app/data inventory
```
Java 26 · Maven · REST API · JDBC · Repository Pattern · Dependency Injection · 57 Tests · Docker · GitHub Actions CI


## Highlights


#### 3 storage backends, 1 interface
in-memory, JSON file, and SQLite are fully swappable with zero changes to calling code

#### Hand-rolled REST API
Routing, query parsing, and JSON wiring built directly on com.sun.net.httpserver, no framework hiding the mechanics

#### 57 tests
1:1 test-to-code ratio — enforced by CI on every push and pull request

#### Multi-stage Docker build
Compiles in one stage, ships a small self-contained image in the next
Java 26, using instance main() entry points (JEP 512) instead of the traditional boilerplate.



## Features

#### CLI (MainCLI)


Choose a storage backend at startup: in-memory (session-only), JSON file, or SQLite
View all products, add new products, adjust quantity up/down, delete products
Input validation with retry-on-invalid-input for every numeric/text prompt (no crashes on bad input)


#### REST API (MainApi + ProductHandler)


Full CRUD over HTTP, backed by SQLite
Manual routing by HTTP method, manual query-string parsing, JSON (de)serialization via Jackson
Consistent JSON error responses with appropriate HTTP status codes across every endpoint.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 26 |
| Build | Maven, `maven-shade-plugin` 3.5.3 (fat-jar packaging) |
| HTTP server | `com.sun.net.httpserver` (JDK built-in — no framework) |
| JSON | Jackson `jackson-databind` 2.17.1 |
| Database | SQLite via `org.xerial:sqlite-jdbc` 3.53.2.0 |
| Testing | JUnit 5 (`junit-jupiter` 5.10.2) |
| CI | GitHub Actions |
| Containerization | Docker (multi-stage build) |

## REST API Reference

Base URL: `http://localhost:8080/products`

| Method | Endpoint | Body | Success | Failure cases |
|---|---|---|---|---|
| `GET` | `/products` | — | `200` + JSON array of all products | — |
| `GET` | `/products?productId={id}` | — | `200` + JSON product | `404` not found · `400` invalid id |
| `POST` | `/products` | JSON product | `201` + JSON of saved product | `400` invalid JSON · `500` save failed (e.g. duplicate id) |
| `PUT` | `/products?productId={id}&add={n}` | — | `200` | `400` invalid params · `500` on failure |
| `PUT` | `/products?productId={id}&remove={n}` | — | `200` | `400` invalid params · `500` on failure |
| `DELETE` | `/products?productId={id}` | — | `200` | `404` not found · `400` invalid id |
| any other method | any | — | `405` Method Not Allowed | — |

## Testing

57 JUnit 5 tests across 6 test classes, roughly a 1:1 line ratio with the application code itself (824 test lines vs. 899 main lines). Two deliberate testing choices worth calling out:

- **Repository tests are isolated from each other on purpose.** Test fixtures are inserted via raw SQL rather than through `save()`, so a regression in one repository method can only ever fail its own test — not cascade into unrelated failures that obscure the real cause.
- **`ProductHandlerTest` runs real HTTP requests against a real server**, using `com.sun.net.httpserver.HttpServer` and `java.net.http.HttpClient` — no mocked `HttpExchange`. Every test spins up an actual server bound to a free port and an actual in-memory SQLite database, and verifies the real response a client would receive: status code, headers, and JSON body.
  Coverage includes standard happy paths as well as edge cases: negative quantity adjustments, removing more stock than exists, missing/invalid ids, malformed JSON, duplicate primary keys, and unsupported HTTP verbs.

Run the full suite:
```bash
mvn clean test
```

## CI/CD

GitHub Actions (`.github/workflows/ci.yml`) runs `mvn clean verify` — full compile plus the entire test suite — on every push and pull request to `main`. The git history reflects this being used in practice: feature work (the REST API, Docker support) was developed on branches and merged via pull request rather than pushed directly to `main`.

## Docker

The `Dockerfile` uses a **multi-stage build**:
1. **Build stage** (`maven:3.9-eclipse-temurin-26`) compiles the project and packages it via the Shade plugin into a single self-contained "fat jar" — all dependencies (Jackson, sqlite-jdbc) bundled in, so nothing needs to be resolved at runtime.
2. **Run stage** (`eclipse-temurin:26-jdk`) copies out only the finished jar, leaving the full build toolchain and source code behind — keeping the final image lean.
```bash
docker build -t inventory-api .
docker run -p 8080:8080 inventory-api
```

## Project Structure

```
src/
├── main/java/
│   ├── MainApi.java                # REST API entry point
│   ├── MainCLI.java                 # CLI entry point
│   ├── Model/
│   │   └── Product.java
│   ├── Repository/
│   │   ├── ProductRepository.java   # storage-agnostic interface
│   │   ├── InMemoryRepository.java
│   │   ├── FileRepository.java      # JSON-backed
│   │   └── SQLiteRepository.java
│   └── Service/
│       ├── SQLiteconnector.java     # opens JDBC connections
│       ├── SQLiteinit.java          # schema creation
│       ├── ProductHandler.java      # HTTP ↔ repository translator
│       ├── RepositoryException.java # storage-agnostic error type
│       └── InputHelper.java         # CLI input validation
└── test/java/                       # mirrors main/, 57 tests total
```