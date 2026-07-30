# Спринт 2. События приложения и основа Transactional Outbox

## Статус

**Завершён.**

Результаты спринта уже включены в `main`. Текущая ветка `feature/outbox-publisher` развивает созданную основу и относится к следующему спринту.

---

## Цель

После успешного создания заказа сформировать явное событие `OrderCreated` и сохранить его в outbox в той же транзакции, что и заказ.

Приложение при этом должно разделять:

* бизнес-факт создания заказа;
* внутреннюю публикацию application event;
* преобразование события в outbox-запись;
* будущую доставку события во внешнюю систему.

К концу спринта должен работать сценарий:

```text
Order создан
    → сформирован OrderCreatedEvent
    → событие передано через application event publisher
    → listener преобразовал событие в OutboxEvent
    → Order и OutboxEvent сохранены в одной транзакции
```

---

## Проблема

После создания заказа другие части системы должны получить информацию о произошедшем бизнес-факте.

При этом application layer не должен напрямую зависеть от:

* Kafka;
* RabbitMQ;
* HTTP-вызовов другого сервиса;
* конкретного способа внешней доставки.

Прямой вызов Kafka producer из use case создавал бы сразу несколько проблем:

* application layer зависел бы от transport technology;
* транзакция PostgreSQL не включала бы Kafka;
* заказ мог бы сохраниться, а событие — не отправиться;
* повторная попытка могла бы привести к дубликатам;
* unit-тестирование use case стало бы сложнее.

---

# Теория

В рамках спринта изучены:

* business event;
* domain event;
* application event;
* Spring `ApplicationEventPublisher`;
* `@EventListener`;
* ports и implementations;
* dependency inversion;
* синхронные Spring Events;
* transaction boundary;
* проблема dual write;
* Transactional Outbox;
* rollback при ошибке listener;
* отличие внутреннего события процесса от сообщения Kafka.

---

# Важное различие

## Application Event

Application Event существует внутри одного процесса приложения.

Он:

* передаётся через память текущей JVM;
* не является сообщением между микросервисами;
* по умолчанию обрабатывается синхронно;
* не переживает остановку приложения;
* не предоставляет Kafka offsets, partitions или consumer groups.

## Kafka Event

Kafka event является внешним сообщением.

Он:

* хранится в Kafka;
* может быть прочитан другим сервисом;
* имеет topic, partition и offset;
* может быть доставлен повторно;
* требует сериализации;
* требует обработки ошибок и идемпотентности.

Spring Events не заменяют Kafka. В проекте они используются для отделения application use case от инфраструктурной реализации outbox.

---

# Реализованные компоненты

## `OrderCreatedEvent`

Пакет:

```java
com.nd.orderservice.order.application.event
```

Назначение:

* описывает бизнес-факт создания заказа;
* содержит необходимые идентификаторы;
* не зависит от Kafka;
* не содержит JPA entity.

---

## `OrderEventPublisher`

Пакет application layer.

Назначение:

* является портом публикации события;
* позволяет use case не зависеть от Spring Event API напрямую;
* отделяет бизнес-операцию от механизма доставки события внутри приложения.

---

## `SpringOrderEventPublisher`

Infrastructure implementation порта.

Назначение:

* использует Spring `ApplicationEventPublisher`;
* публикует application event;
* скрывает детали Spring Events от application service.

---

## Listener события создания заказа

Назначение:

* реагирует на `OrderCreatedEvent`;
* создаёт `OutboxEvent`;
* сохраняет его через `OutboxEventRepository`;
* работает в рамках транзакции создания заказа.

Первоначальный logging listener использовался как промежуточный учебный шаг. Итоговая реализация заменяет логирование сохранением события в outbox.

---

## `OutboxEventFactory`

Пакет:

```java
com.nd.orderservice.order.infrastructure.outbox
```

Назначение:

* преобразует `OrderCreatedEvent` или данные заказа в сериализованный payload;
* использует `ObjectMapper`;
* создаёт `OutboxEvent`;
* отделяет сериализацию от application service.

---

## `OutboxEvent`

Содержит:

* `id` — уникальный идентификатор события;
* `type` — тип события;
* `aggregateId` — идентификатор заказа;
* `createdAt` — время создания;
* `payload` — JSON-представление события.

В следующем спринте модель расширяется полем:

* `publishedAt` — время успешной публикации в Kafka.

---

# Рабочий поток

```text
OrderService.create(command)
    ↓
OrderRepository.save(order)
    ↓
OrderCreatedEvent
    ↓
OrderEventPublisher
    ↓
Spring ApplicationEventPublisher
    ↓
Outbox listener
    ↓
OutboxEventFactory
    ↓
OutboxEventRepository.save(event)
    ↓
commit PostgreSQL transaction
```

Если сохранение outbox-события завершается ошибкой:

```text
OutboxEventRepository.save()
    ↓
exception
    ↓
rollback transaction
    ↓
Order не сохраняется
```

---

# Транзакционная семантика

Spring application listener выполняется синхронно в вызывающем потоке, если специально не настроено асинхронное выполнение.

Поэтому listener участвует в текущем вызове создания заказа.

Если exception из listener не перехвачен:

* транзакция помечается на rollback;
* заказ не фиксируется;
* outbox-событие не фиксируется;
* частичного состояния не остаётся.

Это позволяет сохранить заказ и outbox-событие атомарно в рамках одной PostgreSQL-транзакции.

При этом Kafka пока не участвует в этой транзакции.

---

# Реализованные тесты

## Успешное сохранение

Integration test проверяет:

* создание заказа;
* сохранение `OrderItem`;
* сохранение outbox-события;
* совпадение `aggregateId` с идентификатором заказа;
* тип `ORDER_CREATED`;
* повторное чтение состояния после `flush()` и `clear()`.

## Rollback

Integration test проверяет:

* искусственную ошибку `OutboxEventRepository`;
* выбрасывание exception;
* отсутствие нового заказа после завершения операции;
* вызов сохранения outbox-события.

---

# Definition of Done

* создание заказа формирует `OrderCreatedEvent`;
* application service не зависит от Kafka;
* публикация отделена через порт;
* Spring Events используются только для внутрипроцессного взаимодействия;
* listener создаёт outbox-событие;
* заказ и outbox сохраняются в одной транзакции;
* ошибка сохранения outbox откатывает заказ;
* payload хранится в JSONB;
* основной и ошибочный сценарии покрыты integration tests.

---

# Вопросы для собеседования

* Чем Spring Events отличаются от Kafka?
* Для чего нужен `ApplicationEventPublisher`?
* Почему listener по умолчанию выполняется синхронно?
* Что произойдёт, если listener выбросит exception?
* Почему application service не должен напрямую зависеть от Kafka?
* Какую проблему создаёт dual write?
* Почему PostgreSQL-транзакция не может атомарно зафиксировать запись в Kafka?
* Как Transactional Outbox решает проблему частичного сохранения?
* Почему outbox пока ещё не означает, что событие доставлено?
* Что произойдёт, если приложение завершится после commit, но до отправки события?
* Почему будущий Kafka consumer должен быть идемпотентным?

---

# Ограничения текущего решения

На завершении спринта:

* outbox-запись сохраняется, но ещё не публикуется в Kafka;
* у события нет состояния успешной публикации;
* нет scheduler;
* нет Kafka producer;
* нет повторных попыток внешней доставки;
* нет защиты от параллельной обработки несколькими экземплярами приложения;
* нет очистки опубликованных событий.

Это ожидаемые ограничения: их устранение относится к следующему спринту.

---

# Checklist code review

* Application layer не импортирует Kafka API.
* Event не содержит JPA entity.
* Event содержит только необходимые данные.
* Порт публикации объявлен отдельно от implementation.
* Сериализация не выполняется в controller.
* Outbox сохраняется в той же транзакции, что и заказ.
* Ошибка listener не проглатывается.
* Payload корректно отображается в PostgreSQL JSONB.
* Rollback подтверждён integration test.
* Названия классов отражают ответственность.

---

# Итог спринта

Получена база для надёжного межсервисного взаимодействия:

```text
Business operation
    → Application Event
    → Transactional Outbox
```

Следующим шагом станет внешняя доставка:

```text
Transactional Outbox
    → Outbox Publisher
    → Kafka
```

---
