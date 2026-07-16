# Sprint 2: Event-Driven Foundation

## Objective

Introduce application events after successful order creation while keeping the application independent from transport technologies such as Kafka.

---

## Problem

After an order is created, other parts of the system should be notified.

The Order Service must not know:

- Kafka
- RabbitMQ
- HTTP
- future messaging technologies

Its responsibility is only to publish the business fact that an order has been created.

---

## Theory

Topics covered:

- Business Event
- Application Event
- Spring ApplicationEventPublisher
- @EventListener
- Ports versus implementations
- Why event publication should be separated from business logic

---

## Practice

Implemented:

- OrderCreatedEvent
- OrderEventPublisher
- SpringOrderEventPublisher
- LoggingOrderCreatedListener

Flow:

Order Created

↓

OrderCreatedEvent

↓

OrderEventPublisher

↓

Spring ApplicationEventPublisher

↓

Logging Listener

---

## Definition of Done

- Order creation publishes an event.
- Publishing is separated from business logic.
- Event listeners react without changing Order Service.
- The transport mechanism is still replaceable in the future.

---

## Interview Questions

- What is the difference between Spring Events and Kafka?
- Why use ApplicationEventPublisher instead of calling listeners directly?
- What are synchronous and asynchronous Spring events?
- Why should business logic not depend directly on Kafka?

---

## Next Sprint

Replace logging listener with Transactional Outbox.