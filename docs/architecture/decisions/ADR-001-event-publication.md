# ADR-001 Event Publication Strategy

## Status

Accepted

## Context

After an order is created, the application must notify other components.

The messaging technology has not yet been introduced.

## Decision

Business logic publishes an OrderCreatedEvent through the OrderEventPublisher abstraction.

The current implementation delegates to Spring's ApplicationEventPublisher.

## Consequences

Advantages

- Business logic does not know event listeners.
- Event publication is centralized.
- Easy migration to Outbox in future sprints.

Disadvantages

- Spring Events work only inside a single JVM.
- They are not suitable for inter-service communication.