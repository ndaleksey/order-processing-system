# Order Processing System

An event-driven order processing system built to practice distributed workflows, reliable messaging, and delivery to Kubernetes.

The project models an order lifecycle across independent services. It focuses on practical backend concerns: asynchronous communication, eventual consistency, idempotent event handling, the Transactional Outbox pattern, database state verification, and end-to-end testing.

> Status: active personal project for engineering practice. It is not intended for production use.

## Architecture

The system consists of three independently deployable Spring Boot services:

- **Order Service** - accepts order requests, stores order state, publishes domain events through the Transactional Outbox pattern, and exposes order status through REST API.
- **Inventory Service** - manages inventory availability and handles reservation requests.
- **Payment Service** - processes payment-related events in the order workflow.

Services communicate asynchronously through Apache Kafka. Each service owns its data and reacts to events independently, so the workflow reaches a consistent final state over time rather than through a distributed transaction.

More details are available in the [architecture documentation](docs/architecture), [business flows](docs/business-flows), [database notes](docs/database), and [Kafka documentation](docs/kafka).

## Key Engineering Decisions

- **Transactional Outbox**: an order change and the corresponding integration event are stored atomically; a separate publisher delivers pending events to Kafka.
- **At-least-once delivery**: consumers must tolerate duplicate messages.
- **Idempotent event handling**: services use an event identifier to prevent duplicate processing.
- **Eventual consistency**: order state is updated asynchronously as inventory reservation and payment results arrive.
- **Explicit failure paths**: the order can be confirmed or canceled depending on downstream results.
- **End-to-end verification**: critical flows are checked through public REST APIs and final database state.

## Technology Stack

- Java 21
- Spring Boot
- Spring MVC and REST APIs
- Apache Kafka
- PostgreSQL
- JPA / Hibernate
- Maven multi-module build
- Docker
- Kubernetes
- Helm
- GitLab CI/CD
- JUnit 5
- REST Assured
- Awaitility
- PlantUML

## Repository Structure

```text
.
├── order-service/       # Order API, state management, and outbox publishing
├── inventory-service/   # Inventory management and reservation handling
├── payment-service/     # Payment workflow processing
├── e2e-tests/           # End-to-end order-flow tests
├── helm/                # Helm charts for all services
├── k8s/                 # Kubernetes manifests
├── docs/                # Architecture, business flows, Kafka, and database docs
├── http/                # HTTP request examples
└── pom.xml              # Maven parent project
```

## Build and Test

Prerequisites:

- JDK 21
- Maven
- PostgreSQL and Apache Kafka available to the services
- Docker and Kubernetes cluster for deployment

Build all modules and run tests:

```bash
mvn clean verify
```

The project includes E2E tests for two core scenarios:

1. An order is canceled when inventory reservation fails.
2. An order is confirmed when inventory reservation succeeds, and the available inventory quantity is reduced.

The E2E tests use REST Assured to invoke the Order Service and Awaitility to wait for the asynchronous workflow to reach its final state.

## Deployment

Each service has its own Dockerfile, Kubernetes manifests, and Helm chart.

Build and publish service images to a container registry accessible from the target Kubernetes cluster. Configure image names, tags, environment variables, Kafka, and PostgreSQL connections in the relevant Helm values files.

Deploy the services:

```bash
helm upgrade --install inventory-service ./helm/inventory-service
helm upgrade --install payment-service ./helm/payment-service
helm upgrade --install order-service ./helm/order-service
```

After deployment, verify pod readiness and use the HTTP examples from the `http/` directory to create and inspect orders.

## CI/CD

The root GitLab CI configuration runs tests and includes per-service pipeline configuration for build and deployment stages.

```text
test -> build -> deploy
```

## What I Practice in This Project

- Designing service boundaries and asynchronous workflows
- Choosing event contracts and handling duplicate delivery
- Implementing reliable event publication with Transactional Outbox
- Modeling state transitions and compensating failure paths
- Writing E2E tests for eventually consistent systems
- Packaging Spring Boot services with Docker
- Deploying services with Kubernetes and Helm
- Automating delivery with GitLab CI/CD
