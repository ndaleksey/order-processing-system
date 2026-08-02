# Спринт 3. Outbox Publisher и Kafka Producer

## Статус

**Завершён.**

Рабочая ветка:

```text
feature/outbox-publisher
```

Результат спринта смержен в:

```text
main
```

---

## Цель спринта

Реализовать доставку сохранённых outbox-событий из `order-service` в Kafka.

Итоговый сценарий:

```text
POST /orders
    → Order и OutboxEvent сохраняются в PostgreSQL
    → OutboxPublicationScheduler периодически запускает publisher
    → OutboxPublisher выбирает неопубликованные события
    → OrderEventProducer отправляет событие в Kafka
    → Kafka подтверждает приём сообщения
    → OutboxEvent получает publishedAt
```

При недоступной Kafka:

```text
POST /orders
    → Order и OutboxEvent сохраняются
    → попытка публикации завершается ошибкой
    → транзакция publisher откатывается
    → publishedAt остаётся null
    → событие выбирается при следующем polling cycle
```

---

## Реализованные компоненты

### `KafkaTopicsProperties`

Путь:

```text
order-service/src/main/java/com/nd/orderservice/order/infrastructure/kafka/KafkaTopicsProperties.java
```

Ответственность:

* чтение Kafka topic из конфигурации;
* предоставление имени topic для событий заказов;
* отсутствие бизнес-логики.

Конфигурационное свойство:

```yaml
app:
  kafka:
    topics:
      order-events: order.events
```

---

### `OrderEventProducer`

Путь:

```text
order-service/src/main/java/com/nd/orderservice/order/infrastructure/kafka/OrderEventProducer.java
```

Ответственность:

* принимать `aggregateId` и JSON payload;
* использовать `aggregateId` как Kafka message key;
* отправлять сообщение через `KafkaTemplate`;
* возвращать `CompletableFuture<SendResult<String, String>>`;
* не работать с outbox-репозиторием.

Формат сообщения:

```text
topic = order.events
key   = aggregateId.toString()
value = OutboxEvent.payload
```

Использование `aggregateId` как key позволяет событиям одного заказа попадать в одну partition при неизменном количестве partitions и неизменной стратегии partitioning.

Порядок гарантируется только внутри одной partition, а не глобально для всего topic.

---

### `OutboxPublisher`

Путь:

```text
order-service/src/main/java/com/nd/orderservice/order/infrastructure/outbox/OutboxPublisher.java
```

Ответственность:

* выбрать до 10 неопубликованных outbox-событий;
* обрабатывать их в порядке `createdAt`;
* передать каждое событие в `OrderEventProducer`;
* дождаться результата отправки через `CompletableFuture.join()`;
* установить `publishedAt` только после успешного подтверждения Kafka;
* оставить событие неопубликованным при ошибке.

Метод публикации выполняется в PostgreSQL-транзакции:

```java
@Transactional
public void publishPendingEvents()
```

После успешной отправки вызывается:

```java
event.markPublished();
```

Отдельный вызов `repository.save(...)` не требуется, поскольку сущность остаётся managed внутри транзакции, а Hibernate сохраняет изменение через dirty checking.

---

### `OutboxPublicationScheduler`

Путь:

```text
order-service/src/main/java/com/nd/orderservice/order/infrastructure/outbox/OutboxPublicationScheduler.java
```

Scheduler вынесен в отдельный компонент и не содержит логики публикации.

Его ответственность:

* запускать `OutboxPublisher`;
* читать cron из конфигурации;
* не управлять транзакцией и состоянием событий.

Runtime-конфигурация:

```yaml
app:
  kafka:
    publication:
      cron: "*/5 * * * * *"
```

Publisher запускается каждые 5 секунд.

Для тестового профиля scheduler отключён:

```java
@Profile("!test")
```

Это предотвращает фоновые вызовы publisher во время интеграционных тестов.

---

## Kafka-конфигурация

Приложение запускается на хостовой машине, а Kafka работает в Docker.

Для подключения используется внешний listener:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:29092
```

Внутренний listener:

```text
kafka:9092
```

предназначен для сервисов, запущенных внутри Docker-сети.

Внешний listener:

```text
localhost:29092
```

предназначен для приложений, запущенных из IntelliJ IDEA или локального терминала.

Producer настроен с:

```yaml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
```

`acks=all` означает, что producer ожидает подтверждения от всех требуемых in-sync replicas согласно конфигурации Kafka topic.

---

## Гарантия доставки

Текущая реализация обеспечивает семантику:

```text
at-least-once
```

Система стремится не потерять событие, но допускает повторную публикацию.

Возможный сценарий дублирования:

```text
Kafka приняла сообщение
    → приложение завершилось
    → PostgreSQL-транзакция не успела сохранить publishedAt
    → после перезапуска событие снова выбрано publisher
    → сообщение отправлено повторно
```

Следовательно, будущий consumer должен быть идемпотентным.

Для дедупликации consumer сможет использовать идентификатор события, сохранённый в payload или отдельном message header.

Transactional Outbox сам по себе не обеспечивает exactly-once delivery между PostgreSQL и Kafka.

---

## Поведение транзакции

В текущей версии одна PostgreSQL-транзакция охватывает обработку всей выбранной пачки.

Пример:

```text
event-1 → Kafka успешно
event-2 → Kafka успешно
event-3 → Kafka ошибка
```

Результат:

```text
Kafka:
event-1 опубликован
event-2 опубликован
event-3 не опубликован

PostgreSQL:
транзакция откатилась
publishedAt у всех трёх остался null
```

При следующем polling cycle `event-1` и `event-2` могут быть отправлены повторно.

Это соответствует at-least-once, но увеличивает вероятность дубликатов.

---

## Почему используется `join()`

`KafkaTemplate.send(...)` выполняет отправку асинхронно и возвращает `CompletableFuture`.

В publisher используется:

```java
orderEventProducer.send(...).join();
```

`join()`:

* не делает внутреннюю работу Kafka producer синхронной;
* блокирует поток scheduler до завершения отправки;
* позволяет выполнить `markPublished()` только после успешного результата;
* преобразует ошибочное завершение в `CompletionException`.

Такой подход выбран для учебной реализации из-за простоты и понятных границ поведения.

Ограничение: PostgreSQL-транзакция и соединение с БД остаются открытыми, пока поток ожидает ответ Kafka.

---

## Реализованные тесты

### Unit test `OrderEventProducer`

Проверяет:

* правильное имя topic;
* использование `aggregateId` как key;
* передачу исходного payload;
* возврат future, полученного от `KafkaTemplate.send(...)`.

---

### Unit tests `OutboxPublisher`

Успешный сценарий проверяет:

```text
успешный Kafka send
→ markPublished()
→ publishedAt != null
```

Ошибочный сценарий проверяет:

```text
failed CompletableFuture
→ CompletionException
→ publishedAt == null
```

---

### Integration test успешной публикации

Проверяет:

* сохранение реального `OutboxEvent` в PostgreSQL;
* запуск настоящего `OutboxPublisher`;
* mock только для `OrderEventProducer`;
* сохранение `publishedAt` через dirty checking;
* отсутствие события в запросе pending-событий.

После публикации сущность повторно загружается из БД через `findById(...)`.

---

### Integration test ошибки Kafka

Проверяет:

* ошибочное завершение Kafka future;
* выбрасывание `CompletionException`;
* rollback транзакции publisher;
* сохранение `publishedAt == null`;
* повторное попадание того же события в pending-выборку.

---

## Ручная проверка

Проверен следующий сценарий:

1. Запущены PostgreSQL и Kafka.
2. `order-service` подключён к Kafka через `localhost:29092`.
3. Создан заказ.
4. В PostgreSQL появилась запись `OutboxEvent` с `publishedAt == null`.
5. Scheduler запустил publisher.
6. Producer отправил сообщение в topic `order.events`.
7. После подтверждения Kafka значение `publishedAt` было сохранено.
8. Все Maven-тесты проекта завершились успешно.

---

## Что изучено

В рамках спринта разобраны:

* Kafka broker;
* topic;
* partition;
* offset;
* message key;
* producer;
* `KafkaTemplate`;
* асинхронная отправка;
* `CompletableFuture`;
* `join()`;
* producer acknowledgements;
* `acks=all`;
* internal и external Kafka listeners;
* polling publisher;
* Spring Scheduler;
* dirty checking;
* границы PostgreSQL-транзакции;
* at-most-once;
* at-least-once;
* exactly-once semantics;
* повторная доставка;
* идемпотентность consumer;
* ограничения простой реализации Transactional Outbox.

---

## Ограничения текущей реализации

В текущей версии не реализованы:

* конкурентная работа нескольких экземпляров publisher;
* `SELECT ... FOR UPDATE SKIP LOCKED`;
* состояние `PROCESSING`;
* lease-механизм;
* восстановление зависших событий;
* отдельная транзакция на каждое событие;
* параллельная асинхронная публикация пачки;
* retry policy с backoff;
* Dead Letter Queue;
* Kafka transactions;
* Debezium или другой CDC-механизм;
* Schema Registry;
* consumer idempotency;
* автоматическое архивирование outbox;
* retention policy;
* distributed lock для scheduler;
* метрики publisher;
* distributed tracing.

---

## Возможные следующие улучшения

### Отдельная транзакция на событие

Каждое событие может обрабатываться независимо, чтобы ошибка одного события не откатывала `publishedAt` ранее успешно отправленных событий.

При реализации необходимо учитывать Spring proxy и проблему self-invocation.

---

### Claim-механизм

Перед отправкой событие может переводиться:

```text
NEW → PROCESSING → PUBLISHED
```

Это уменьшит риск одновременной обработки одной записи несколькими publisher-инстансами.

---

### Блокировки БД

Для нескольких экземпляров publisher может использоваться:

```sql
FOR UPDATE SKIP LOCKED
```

Каждый экземпляр будет получать отдельный набор событий.

---

### CDC

Вместо polling можно использовать Debezium и Kafka Connect для чтения изменений PostgreSQL WAL.

Такой подход уменьшает количество polling-запросов, но требует дополнительной инфраструктуры.

---

## Definition of Done

* [x] Добавлена зависимость Spring Kafka.
* [x] Настроен Kafka producer.
* [x] Название topic вынесено в конфигурацию.
* [x] `aggregateId` используется как message key.
* [x] Payload публикуется без повторной сериализации.
* [x] Реализован `OrderEventProducer`.
* [x] Реализован `OutboxPublisher`.
* [x] Publisher выбирает ограниченную пачку pending-событий.
* [x] Publisher ожидает результат Kafka send.
* [x] `publishedAt` устанавливается только после успешной отправки.
* [x] При ошибке `publishedAt` остаётся `null`.
* [x] Реализован `OutboxPublicationScheduler`.
* [x] Интервал scheduler вынесен в конфигурацию.
* [x] Scheduler отключён в профиле `test`.
* [x] Настроен external Kafka listener для локального приложения.
* [x] Реализованы unit-тесты producer.
* [x] Реализованы unit-тесты publisher.
* [x] Реализованы integration-тесты publisher.
* [x] Проверен rollback при ошибке Kafka.
* [x] Все Maven-тесты проходят.
* [x] Понятна гарантия at-least-once.
* [x] Зафиксированы ограничения реализации.

---

## Контрольные вопросы

1. Какую проблему решает Transactional Outbox?
2. Почему сохранение outbox-записи ещё не означает доставку события?
3. Почему PostgreSQL-транзакция не может атомарно включить Kafka?
4. Что такое Kafka topic?
5. Что такое partition?
6. Почему offset уникален только внутри partition?
7. Для чего используется message key?
8. Почему `aggregateId` подходит в качестве key?
9. Гарантирует ли Kafka глобальный порядок сообщений?
10. Что означает `acks=all`?
11. Почему `KafkaTemplate.send()` возвращает `CompletableFuture`?
12. Что делает `join()`?
13. Почему `join()` может выбросить `CompletionException`?
14. В какой момент нужно устанавливать `publishedAt`?
15. Почему нельзя установить `publishedAt` перед отправкой?
16. Почему текущая реализация имеет гарантию at-least-once?
17. В каком сценарии сообщение будет опубликовано повторно?
18. Почему consumer должен быть идемпотентным?
19. Почему одна транзакция на всю пачку увеличивает количество дубликатов?
20. Чем polling отличается от CDC?
21. Для чего нужен `FOR UPDATE SKIP LOCKED`?
22. Почему scheduler вынесен в отдельный класс?
23. Зачем scheduler отключается в профиле `test`?
24. Чем Kafka internal listener отличается от external listener?
25. Почему приложение с хоста использует `localhost:29092`, а не `kafka:9092`?

---

## Итог спринта

В `order-service` реализована рабочая цепочка доставки событий:

```text
PostgreSQL Outbox
    → scheduled polling
    → Kafka Producer
    → Kafka acknowledgement
    → publishedAt
```

Реализация покрыта unit- и integration-тестами и обеспечивает доставку с семантикой at-least-once.

Основное архитектурное ограничение текущего решения — блокирующее ожидание Kafka внутри PostgreSQL-транзакции и возможная повторная отправка ранее успешно опубликованных сообщений при rollback всей пачки.
