# Java Middle+/Senior Interview Mentorship Roadmap

## Goal

Prepare for Java Middle+/Senior interviews in 6 weeks using this project as the main practice field.

The target stack is Java 21, Spring Boot, Hibernate, PostgreSQL, Kafka, Docker, Redis, and microservice architecture patterns.

The working ratio is 40% theory and 60% practice.

The teaching approach is problem-first: start from a concrete engineering problem, identify the trade-offs, then introduce the minimum required tool or pattern.

## Mentor Role

The mentor acts as:

- technical mentor for theory and interview preparation;
- team lead for scope control and sprint planning;
- code reviewer for design, maintainability, and production readiness;
- interviewer for mock questions and trade-off discussions.

## Core Engineering Themes

- How to keep API contracts explicit and stable: Spring MVC, DTOs, validation.
- How to prevent invalid business state: aggregate roots, invariants, Java 21 records where useful.
- How to persist data safely: Hibernate, JPA lifecycle, transactions, locking, migrations.
- How to understand slow or unsafe SQL: PostgreSQL ACID, indexes, isolation, `EXPLAIN ANALYZE`.
- How to make services communicate reliably: Kafka, retries, DLQ, idempotent consumers.
- How to keep data consistent across services: Saga, Transactional Outbox, eventual consistency.
- How to make local development reproducible: Docker Compose and service configuration.
- How to reduce load and handle duplicate requests: Redis caching and idempotency keys.
- How to reason about concurrent code: executors, locks, atomics, race conditions.

## Sprint Structure

Each sprint has:

- objective;
- theory topics;
- practical tasks;
- definition of done;
- code review checklist;
- interview questions.

Overtime rule: if a sprint task does not fit the sprint budget, reduce scope before adding complexity.

## Six-Week Plan

### Week 1: Order Service Foundation

Objective: turn the current skeleton into a working REST service.

Problem:

- clients need to create and read orders through a stable API;
- invalid requests must not corrupt the domain model;
- business logic must not leak into controllers.

Minimum tools:

- layered architecture versus hexagonal architecture;
- Spring MVC request lifecycle;
- DTO boundaries and validation;
- transaction boundary placement;
- domain invariants.

Practice:

- implement `POST /orders`;
- implement `GET /orders/{id}`;
- add request and response DTOs;
- add validation and error handling;
- add service-layer transaction;
- add unit and slice tests.

### Week 2: Hibernate and PostgreSQL Foundation

Objective: make persistence explicit, stable, and interview-ready.

Problem:

- the service needs durable order storage;
- schema changes must be controlled;
- reads must not accidentally become slow or fragile.

Minimum tools:

- JPA entity states;
- dirty checking;
- lazy loading and N+1;
- cascade and orphan removal;
- optimistic locking;
- Flyway migrations.

Practice:

- replace `ddl-auto: update` with migrations;
- add indexes for common order queries;
- add optimistic locking to aggregate roots;
- write repository tests;
- reproduce and fix an N+1 query.

### Week 3: PostgreSQL Performance and Transactions

Objective: understand how PostgreSQL executes and protects data.

Problem:

- order queries can become slow as data grows;
- concurrent requests can produce inconsistent results;
- indexes improve reads but add write and storage cost.

Minimum tools:

- ACID;
- MVCC;
- isolation levels;
- locks and deadlocks;
- B-tree indexes;
- `EXPLAIN ANALYZE`.

Practice:

- analyze order queries with `EXPLAIN ANALYZE`;
- compare sequential scan and index scan;
- create a transaction anomaly demo;
- add a safe pagination query;
- document index trade-offs.

### Week 4: Kafka and Event-Driven Flow

Objective: introduce asynchronous communication between services.

Problem:

- payment, inventory, and notifications should not block the order API;
- messages can be duplicated, delayed, or fail;
- consumers must process events safely.

Minimum tools:

- Kafka topic, partition, offset, consumer group;
- delivery guarantees;
- retry strategies;
- dead letter topics;
- event schema design.

Practice:

- publish `OrderCreated`;
- consume payment and inventory events;
- add retry and DLQ conventions;
- add idempotent event handling;
- test Kafka integration with Testcontainers or local Docker.

### Week 5: Microservice Consistency Patterns

Objective: implement reliability patterns required for real microservices.

Problem:

- one business operation spans multiple services;
- distributed transactions are usually not available;
- failures require compensation and repeat-safe processing.

Minimum tools:

- Saga orchestration versus choreography;
- Transactional Outbox;
- eventual consistency;
- compensation;
- idempotency.

Practice:

- add outbox table;
- persist domain event and order in one transaction;
- publish outbox events asynchronously;
- implement compensation path for failed payment or inventory;
- document the order saga.

### Week 6: Production Readiness and Interview Simulation

Objective: prepare for system design, code review, and live discussion.

Problem:

- the service must be easy to run, observe, and discuss under interview pressure;
- repeated requests and high load must be handled intentionally;
- FinTech interviews often test concurrency reasoning.

Minimum tools:

- Docker Compose local platform;
- Redis caching and idempotency keys;
- service observability;
- concurrency fundamentals;
- senior-level trade-off reasoning.

Practice:

- add Docker Compose for PostgreSQL, Kafka, Redis;
- add Redis-backed idempotency for order creation;
- add health checks and metrics;
- run mock interview sessions;
- perform final architecture review.

## Review Standards

Code is accepted only when:

- business invariants are enforced in the domain or application layer;
- API contracts are explicit and validated;
- persistence changes are represented by migrations;
- transactional boundaries are intentional;
- errors are mapped to useful HTTP responses;
- tests cover core behavior and failure paths;
- naming communicates business meaning;
- infrastructure assumptions are documented.
