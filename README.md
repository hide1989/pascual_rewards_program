# Rewards Program API

A REST API that implements a customer rewards program: every purchase earns points, points accumulate across purchases, and customers can redeem their accumulated points for a monetary value.

## Business rules

- **Earning points**: for every **$1,000** spent, the customer earns **1 point**.
- **Carry-over**: the remainder of a purchase that does not complete a full $1,000 is **not lost**. It is added to the customer's running total and counted the next time the customer buys something, until it completes another $1,000. Example: a $700 purchase earns 0 points; a following $500 purchase completes $1,200 total, so it earns 1 point (the $700 "carried over" into it).
- **Redeeming points**: **1 point = $100**. A customer can redeem any number of points up to their currently available balance (`points earned - points already redeemed`).
- **Insufficient balance**: trying to redeem more points than are currently available is rejected with a clear error (`409 Conflict`) instead of silently failing.

There is no customer registration endpoint: a `customerId` is any string the caller chooses, and the customer's points account is created automatically the first time that `customerId` is used in a purchase.

## Tech stack

- **Java 25**
- **Spring Boot 4.0.8** (Spring Framework 7)
- **Gradle** (Groovy DSL) as the build tool
- **H2** in-memory database, accessed through **Spring Data JPA** repositories
- Layered architecture with an explicit **command/handler** (CQRS-style) separation for the application layer
- **JUnit 5 + Mockito + AssertJ** for unit tests, plus **RestTestClient**-based functional (end-to-end) tests
- **JaCoCo** for test coverage measurement, enforced at a minimum of **80% instruction coverage**

## Project structure

```
src/main/java/com/rewardsprogram/
  controller/        REST controllers (HTTP <-> Command/Query translation)
  application/
    command/          Write use-case inputs (RegisterPurchaseCommand, RedeemPointsCommand)
    query/             Read use-case input (GetCustomerPointsBalanceQuery)
    handler/           One class per use case, orchestrates domain + repositories
    dto/request/       Request DTOs with Bean Validation annotations
    dto/response/      Response DTOs
  domain/
    model/             JPA entities
    service/           PointsCalculator (pure points arithmetic, no Spring dependency)
  repository/         Spring Data JPA repositories
  exception/          Business exceptions + global exception handler + error DTO
src/test/java/com/rewardsprogram/
  domain/service/               Unit tests for PointsCalculator (pure logic, no mocks needed)
  application/handler/          Unit tests for the command/query handlers (Mockito mocks for repositories)
  controller/                   Unit tests for the REST controllers (Mockito mocks for handlers)
  exception/                    Unit tests for GlobalExceptionHandler (all exception -> status code mappings)
  functional/                   Functional (end-to-end) tests, hitting the real HTTP API + real H2 database
postman/                                        Postman collection (see below)
```

## Prerequisites

- **JDK 25** installed and available on your `PATH` (or via a tool like SDKMAN).
- No local Gradle installation is required — the project ships with the Gradle Wrapper (`./gradlew`), which will download the correct Gradle version automatically on first run.

> **Note:** Gradle 8.x has a known bug parsing the JDK 25 version string, which breaks build script compilation. This project's wrapper (`gradle/wrapper/gradle-wrapper.properties`) is pinned to **Gradle 9.7.1**, which supports JDK 25 correctly — you do not need to do anything for this, just use `./gradlew`.

## How to run the application

From the project root:

```bash
./gradlew bootRun
```

On Windows, use `gradlew.bat bootRun`.

The application starts on **http://localhost:8080**. You should see Spring Boot's startup log ending with a line like `Started RewardsProgramApplication in ... seconds`.

To stop it, press `Ctrl+C` in the terminal.

### H2 in-memory database

The database is **in-memory** (`jdbc:h2:mem:rewardsdb`) and its schema is created fresh on every application startup and dropped on shutdown (`ddl-auto: create-drop`) — there is no data left over between runs. While the application is running, you can inspect the database through the H2 web console at:

```
http://localhost:8080/h2-console
```

Use JDBC URL `jdbc:h2:mem:rewardsdb`, user `sa`, and an empty password.

## How to run the tests

```bash
./gradlew test
```

This runs the **full test suite** (44 tests): 23 unit tests plus 21 functional (end-to-end) tests. A combined coverage report is generated automatically after `test` finishes (see [Test coverage (JaCoCo)](#test-coverage-jacoco) below).

To run the whole build (compile + test + enforce the coverage threshold + package the jar):

```bash
./gradlew build
```

### Testing strategy

The test suite is split into two complementary layers:

- **Unit tests** (`src/test/java/com/rewardsprogram/{domain,application,controller,exception}/...`) — isolate a single class at a time using **Mockito** to mock its collaborators (repositories, handlers), following the **Arrange-Act-Assert (AAA)** pattern in every test method. Each layer with meaningful logic has its own unit tests:
  - `PointsCalculatorTest` — the points arithmetic itself (no mocks needed, it is a pure function class): carry-over, exact multiples, redemption value conversion.
  - `RegisterPurchaseHandlerTest`, `RedeemPointsHandlerTest`, `GetCustomerPointsBalanceHandlerTest` — the use-case orchestration logic, with the JPA repositories and `PointsCalculator` mocked via Mockito (`@Mock`/`@ExtendWith(MockitoExtension.class)`), verifying both the **happy path** (correct points/balance calculations, correct entities passed to `save(...)` via `ArgumentCaptor`) and the **expected exception** (`InsufficientPointsException` thrown when a redemption exceeds the available balance, including the edge case of a customer with no account at all).
  - `PurchaseControllerTest`, `RedemptionControllerTest`, `CustomerPointsControllerTest` — the HTTP-to-command translation, with the handler mocked, verifying the returned status code and that exceptions thrown by a handler propagate unmodified (they are handled by `GlobalExceptionHandler`, not by the controller).
  - `GlobalExceptionHandlerTest` — every exception-to-HTTP-status mapping (`400` for validation and malformed JSON, `409` for insufficient points, `500` for unexpected errors), using Mockito to simulate `MethodArgumentNotValidException`, `ConstraintViolationException`, etc.

- **Functional tests** (`src/test/java/com/rewardsprogram/functional/...`) — exercise the application as a black box: real HTTP calls (via `RestTestClient`) against a running Spring context and a real in-memory H2 database, with no mocks at all. These cover the same business scenarios end-to-end, including the carry-over rule and every validation/error case, as documented in detail in the [Postman collection](#postman-collection) section below.

Both layers use **JUnit 5** as the test runner and **AssertJ** (`assertThat`) for assertions.

## Test coverage (JaCoCo)

Test coverage is measured with the **JaCoCo** Gradle plugin. Running `./gradlew test` automatically generates a coverage report afterwards (the `test` task is `finalizedBy jacocoTestReport`). To generate the report explicitly:

```bash
./gradlew jacocoTestReport
```

Reports are written to:
- HTML (human-readable): `build/reports/jacoco/test/html/index.html` — open this file in a browser to see line-by-line coverage per class.
- XML (machine-readable, e.g. for CI): `build/reports/jacoco/test/jacocoTestReport.xml`

### Coverage threshold

The build enforces a **minimum of 80% instruction coverage** through the `jacocoTestCoverageVerification` task, which is wired into Gradle's standard `check` task — meaning it runs automatically as part of `./gradlew build` and fails the build if coverage drops below 80%. To run just the verification:

```bash
./gradlew jacocoTestCoverageVerification
```

As of this test suite, overall instruction coverage is **~95%**. The small remainder of uncovered instructions comes from code that is not meaningful to unit-test directly: the `main` method of `RewardsProgramApplication` (the Spring Boot bootstrap entry point, which would require starting a full separate server process to exercise) and the protected no-argument constructors that JPA entities (`CustomerPointsAccount`, `Purchase`, `Redemption`) are required to expose for Hibernate's internal reflection-based instantiation. Both are well above the required 80% threshold either way.

## API endpoints

All request/response bodies are JSON. All error responses share this shape:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "human-readable summary",
  "errors": [{ "field": "amount", "message": "amount debe ser mayor que 0" }],
  "timestamp": "2026-08-22T20:14:53.016037Z",
  "path": "/api/purchases"
}
```

### 1. Register a purchase

```
POST /api/purchases
```

Registers a purchase for a customer and returns how many points it earned (applying the carry-over rule against the customer's accumulated purchase total).

**Request body**

| Field        | Type   | Rules                        |
|--------------|--------|-------------------------------|
| `customerId` | string | required, not blank           |
| `amount`     | number | required, strictly greater than 0 |

```json
{ "customerId": "cust-123", "amount": 1500 }
```

**Success response — `201 Created`**

```json
{
  "customerId": "cust-123",
  "purchaseId": 42,
  "amount": 1500,
  "pointsEarned": 1,
  "totalAvailablePoints": 3
}
```

**Error responses**: `400 Bad Request` if `amount` is missing, zero, negative, or `customerId` is blank; `400 Bad Request` if the JSON body is malformed.

### 2. Get a customer's points balance

```
GET /api/customers/{customerId}/points
```

Returns the customer's total points earned, total points redeemed, and currently available balance. If the customer has never made a purchase, this still returns `200 OK` with everything at 0 — it never returns `404`.

**Success response — `200 OK`**

```json
{
  "customerId": "cust-123",
  "totalPointsEarned": 5,
  "totalPointsRedeemed": 2,
  "availablePoints": 3
}
```

**Error response**: `400 Bad Request` if `customerId` is blank.

### 3. Redeem points

```
POST /api/redemptions
```

Redeems a number of points from a customer's available balance and returns their monetary value (`points * $100`).

**Request body**

| Field        | Type    | Rules                          |
|--------------|---------|----------------------------------|
| `customerId` | string  | required, not blank              |
| `points`     | integer | required, strictly greater than 0 |

```json
{ "customerId": "cust-123", "points": 2 }
```

**Success response — `201 Created`**

```json
{
  "customerId": "cust-123",
  "redemptionId": 17,
  "pointsRedeemed": 2,
  "amountValue": 200.00,
  "remainingPoints": 1
}
```

**Error responses**:
- `400 Bad Request` if `points` is missing, zero, negative, or not a whole number, or if `customerId` is blank.
- `409 Conflict` if the customer does not have enough available points to redeem the requested amount (this includes a customer that has never purchased anything, since their balance is 0).

## Postman collection

A ready-to-import Postman collection is included at [`postman/Rewards-Program.postman_collection.json`](postman/Rewards-Program.postman_collection.json).

**How to import it**: open Postman → `Import` → select the file (or drag it into the Postman window). It uses the Postman Collection v2.1 schema, so it works with any recent version of Postman or Postman-compatible clients (e.g. Insomnia's Postman importer).

**Collection variables** (editable from the collection's "Variables" tab):

| Variable     | Default value          | Purpose                                              |
|--------------|-------------------------|--------------------------------------------------------|
| `baseUrl`    | `http://localhost:8080` | Base URL of the running application. Change it if you run the app on a different port. |
| `customerId` | `cust-demo-001`         | Sample customer id shared by every request in the collection. |

**What's in the collection**: the requests are grouped into folders and numbered so that running the collection from top to bottom tells one coherent story for a single demo customer (`{{customerId}}`):

- **Purchases**
  1. *Register Purchase (below $1,000 - starts carry-over)* — registers a $700 purchase. Earns 0 points, but the $700 is kept for the next purchase.
  2. *Register Purchase (completes the carry-over)* — registers a $500 purchase. Combined with the previous $700, the customer's total reaches $1,200, earning 1 point. Demonstrates the carry-over rule end to end.
  3. *Register Purchase (validation error - negative amount)* — sends `amount: -50` to show the `400 Bad Request` validation response.
- **Points Balance**
  - *Get Customer Points Balance* — run after the two purchases above to see `availablePoints: 1`.
- **Redemptions**
  1. *Redeem Points (valid)* — redeems the 1 available point for $100, bringing the balance back to 0.
  2. *Redeem Points (insufficient balance)* — tries to redeem 5 points right after the balance was emptied, to show the `409 Conflict` "not enough points" error.

Each request includes a description explaining what it does and why, plus a saved example response so you can see the expected result without even running the app first. Make sure the application is running (`./gradlew bootRun`) before executing the requests for real.
