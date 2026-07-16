# Sprint 1: Order Service Foundation

## Objective

Turn the current `order-service` skeleton into a minimal production-style REST service for creating and reading orders.

Sprint duration: 5 to 7 focused practice days.

Theory/practice ratio: 40% theory, 60% implementation.

Teaching approach: start from the problem, discuss trade-offs, then use the smallest suitable framework feature or pattern.

## Current Baseline

The project already has:

- root Maven project;
- `order-service` module;
- Spring Boot application entry point;
- initial `Order` and `OrderItem` JPA entities;
- empty controller, DTOs, handler, and event publisher classes;
- PostgreSQL configuration;
- PlantUML sketches for Kafka topics and happy-path order flow.

The project currently builds with system Maven using:

```bash
mvn -q test
```

The module Maven wrapper is incomplete because `.mvn/wrapper/maven-wrapper.properties` is missing.

## Backlog

### Task 1: Define the Order API Contract

Problem:

- external clients need a stable way to create orders;
- invalid payloads must be rejected before reaching business logic;
- the API must not expose persistence internals.

Minimum theory:

- REST resource modeling;
- command DTO versus read DTO;
- validation as API boundary protection.

Practice:

- define `CreateOrderRequest`;
- define nested item request;
- define `CreateOrderResponse`;
- define `OrderDto`;
- choose response status codes.

Definition of done:

- request contains `customerId` and at least one item;
- item contains `productId`, `name`, `productPrice`, and `quantity`;
- invalid input maps to `400 Bad Request`;
- response contains created order id and status.

Interview questions:

- Why should entities not be exposed directly from controllers?
- Where should validation live: controller, service, or domain?
- What is the difference between command DTO and query DTO?

### Task 2: Implement Domain Invariants

Problem:

- an order with no customer, no items, negative price, or invalid quantity must not exist;
- relying only on controller validation is not enough because domain code can be called from tests, jobs, or event consumers.

Minimum theory:

- aggregate root;
- invariants;
- entity factory methods;
- anemic domain model versus rich domain model.

Practice:

- require non-null `customerId`;
- reject empty orders;
- reject non-positive quantity;
- reject negative or zero product price;
- maintain `totalPrice` inside the aggregate;
- add timestamps.

Definition of done:

- invalid domain state cannot be created through public factory methods;
- total price is recalculated consistently;
- domain behavior is covered by unit tests.

Interview questions:

- What is an aggregate root?
- Why is `BigDecimal` preferred for money?
- What are the risks of public setters on entities?

### Task 3: Implement Application Use Case

Problem:

- creating an order is one business operation;
- saving partially valid state must be impossible;
- persistence should be committed or rolled back as one unit.

Minimum theory:

- application service versus domain entity;
- transaction boundary;
- repository abstraction;
- command handling.

Practice:

- implement create-order use case;
- save order in PostgreSQL through `OrderRepository`;
- return created order response;
- mark the service method transactional.

Definition of done:

- use case has a clear input and output;
- controller does not contain business logic;
- transaction starts at application service boundary;
- repository is used only by application layer.

Interview questions:

- Where should `@Transactional` be placed and why?
- What happens if a transaction commits but event publishing fails?
- What is the difference between application service and domain service?

### Task 4: Add Read Endpoint

Problem:

- clients need to read created orders;
- missing resources must be represented clearly;
- JPA internals must not leak through JSON serialization.

Minimum theory:

- query model;
- lazy loading;
- transaction scope for reads;
- HTTP status semantics.

Practice:

- implement `GET /orders/{id}`;
- map missing order to `404 Not Found`;
- map entity to DTO;
- avoid leaking Hibernate proxies.

Definition of done:

- existing order returns full order DTO;
- missing order returns 404;
- mapping is explicit;
- behavior is tested.

Interview questions:

- What causes `LazyInitializationException`?
- How can N+1 appear in read endpoints?
- When is `@Transactional(readOnly = true)` useful?

### Task 5: Add Error Handling and Tests

Problem:

- failures should be predictable for API clients;
- regressions should be caught before manual testing;
- tests should prove behavior, not only framework startup.

Minimum theory:

- exception taxonomy;
- global exception handling;
- test pyramid;
- controller slice tests versus integration tests.

Practice:

- add global error handler;
- add validation error response;
- add domain tests;
- add service tests;
- add controller tests.

Definition of done:

- validation failures are predictable;
- not-found failures are predictable;
- core domain behavior is tested without Spring context;
- API behavior is tested with Spring MVC test support.

Interview questions:

- What should be tested with unit tests versus integration tests?
- Why are context-loading tests weak?
- How should API errors be represented?

## Sprint 1 Review Checklist

- Controller delegates to application layer.
- DTOs do not expose JPA entities.
- Domain prevents invalid state.
- `@Transactional` is not placed on controller methods.
- Money is represented with `BigDecimal`.
- `Order` owns `OrderItem` lifecycle.
- Error responses are consistent.
- Tests prove business behavior, not only Spring context startup.

## Out of Scope

These topics are intentionally deferred:

- Kafka publishing;
- Transactional Outbox;
- Saga implementation;
- Redis;
- Docker Compose;
- query performance tuning.

Deferring them keeps Sprint 1 focused and prevents architecture from outrunning the working domain model.
