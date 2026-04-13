# Day 3 — Outbound Connector Strategy

**Goals**

- Understand what outbound connectors are and how they differ
- Add Kafka and AMQP outbound connectors in addition to REST
- Know which connector fits your application architecture

**Estimated time**: 2 hours

[Download starting code](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-02.zip)

## What outbound connectors do

When EDDIE receives data from a region connector — a permission status update, accounting point master data, or consumption data — it needs a way to hand that data to your application.
Outbound connectors are that handover mechanism.
EDDIE supports three connectors for application integration, and you can run any combination of them simultaneously:

| Connector | Delivery model | Best for                               |
|-----------|----------------|----------------------------------------|
| REST      | Pull           | Getting started, polling, serverless   |
| Kafka     | Push           | Event-driven backends, high throughput |
| AMQP      | Push           | Message-queue workflows, microservices |

Your choice of connector does not affect how you configure EDDIE, your data needs, or the EDDIE button.
The data your application receives is identical regardless of which connector delivers it.

## The REST outbound connector (review)

You already have the REST connector running from Day 1.
It stores messages in memory and serves them on a set of HTTP endpoints under port `9090`.

```dotenv
OUTBOUND_CONNECTOR_REST_ENABLED=true
```

The connector retains messages for 48 hours by default and cleans up once per hour.
You can adjust both settings if your polling interval is longer or if you want to reduce memory usage:

```dotenv
OUTBOUND_CONNECTOR_REST_ENABLED=true
# Keep messages for 7 days, clean up once per day
OUTBOUND_CONNECTOR_REST_RETENTION_TIME=P7D
OUTBOUND_CONNECTOR_REST_RETENTION_REMOVAL='0 0 0 * * *'
```

The full set of endpoints available on port `9090`:

| Endpoint                                 | Data                               |
|------------------------------------------|------------------------------------|
| `/agnostic/connection-status-messages`   | Permission status updates          |
| `/agnostic/raw-data-messages`            | Raw connector payloads             |
| `/cim_0_82/permission-md`                | Permission market documents        |
| `/cim_0_82/accounting-point-data-md`     | Accounting point master data       |
| `/cim_0_82/validated-historical-data-md` | Historical consumption (CIM v0.82) |
| `/cim_1_04/validated-historical-data-md` | Historical consumption (CIM v1.04) |
| `/cim_1_12/near-real-time-data-md`       | Near real-time data                |

> [!NOTE]
> Each endpoint accepts `Accept: application/json`, `Accept: application/xml`, or `Accept: text/event-stream`.
> Without OAuth2 enabled, no authorization header is needed.
> OAuth2 protection for the management port is covered on Day 11.

The REST connector is the simplest way to get data out of EDDIE.
Its main limitation is that it is pull-based: if your application is not polling, messages accumulate in memory until they expire.
For production backends that process data continuously, one of the push-based connectors below is a better fit.

## The Kafka outbound connector

Kafka is a natural fit when your backend is event-driven or when you expect high data volumes.
EDDIE publishes each incoming message to a dedicated Kafka topic the moment it arrives, so your application consumes data in real time without polling.

### Add Kafka to your Compose file

Add a `kafka` service to your `docker-compose.yml`.
EDDIE can produce messages larger than Kafka's 1 MB default, so the buffer and request size limits are increased accordingly.

```yaml [docker-compose.yml]
name: 21-days
services:
  db:
    image: postgres:17-bookworm
    environment:
      POSTGRES_USER: eddie
      POSTGRES_PASSWORD: eddie
      POSTGRES_DB: eddie
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U eddie" ]
      interval: 10s
      timeout: 3s
      retries: 5

  kafka:
    image: docker.io/apache/kafka:4.1.0
    ports:
      - "9094:9094"
    environment:
      - KAFKA_NODE_ID=0
      - KAFKA_PROCESS_ROLES=controller,broker
      - KAFKA_CONTROLLER_QUORUM_VOTERS=0@kafka:9093
      - KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094
      - KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT
      - KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER
      - KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT
      - KAFKA_SOCKET_REQUEST_MAX_BYTES=104857600
      - KAFKA_MESSAGE_MAX_BYTES=104857600
      - KAFKA_AUTO_CREATE_TOPICS_ENABLE=true
      - KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1

  eddie:
    image: ghcr.io/eddie-energy/eddie:latest
    ports:
      - "8080:8080"
      - "9090:9090"
    volumes:
      - ./data-needs.json:/opt/core/config/data-needs.json
    env_file: .env
    depends_on:
      db:
        condition: service_healthy
```

### Configure the Kafka outbound connector

Enable the connector and point it at your Kafka broker.
The `OUTBOUND_CONNECTOR_KAFKA_EDDIE_ID` value is used as part of every topic name,
so keep it short and consistent across restarts.

```dotenv [.env]
# Enable Kafka outbound connector
OUTBOUND_CONNECTOR_KAFKA_ENABLED=true
OUTBOUND_CONNECTOR_KAFKA_EDDIE_ID=eddie
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

### Topic naming

EDDIE creates one topic per data type.
With `OUTBOUND_CONNECTOR_KAFKA_EDDIE_ID=eddie`, the topics are:

| Topic                                            | Data                         |
|--------------------------------------------------|------------------------------|
| `ep.eddie.agnostic.connection-status-message`    | Permission status updates    |
| `ep.eddie.agnostic.raw-data-message`             | Raw connector payloads       |
| `ep.eddie.cim_0_82.permission-md`                | Permission market documents  |
| `ep.eddie.cim_0_82.accounting-point-md`          | Accounting point master data |
| `ep.eddie.cim_0_82.validated-historical-data-md` | Historical consumption       |
| `ep.eddie.cim_1_12.near-real-time-data-md`       | Near real-time data          |

Each message carries `connection-id`, `permission-id`, and
`data-need-id` as Kafka headers so your consumer can route messages without deserializing the payload.

### Restart and verify

Apply the changes and wait for EDDIE to connect to Kafka:

```shell
docker compose up -d
docker compose logs -f eddie
```

Now trigger the simulation again using the demo page at http://localhost:8080/demo.
Go through the permission flow and submit a **CREATED** status and some meter readings as you did on Day 1.

Open a second terminal and consume from the connection status topic to confirm the message arrived:

```shell
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic ep.eddie.agnostic.connection-status-message \
  --from-beginning
```

You should see a JSON message arrive immediately.

<!-- TODO: screenshot of kafka-console-consumer.sh output -->

## The AMQP outbound connector

AMQP is suited for architectures that already use a message broker, particularly when you need acknowledgement semantics, dead-letter queues, or routing rules between exchanges.
EDDIE supports AMQP 1.0, and RabbitMQ 4 implements it natively.

### Add RabbitMQ to your Compose file

Add a `rabbitmq` service.
The `management` variant of the image includes a web UI for inspecting queues — useful while you explore.

```yaml [docker-compose.yml]
  rabbitmq:
    image: rabbitmq:4-management
    ports:
      - "5672:5672"
      - "15672:15672"
```

Your full `docker-compose.yml` should now have four services: `db`, `kafka`, `rabbitmq`, and `eddie`.

### Configure the AMQP outbound connector

```dotenv [.env]
# Enable AMQP outbound connector
OUTBOUND_CONNECTOR_AMQP_ENABLED=true
OUTBOUND_CONNECTOR_AMQP_EDDIE_ID=eddie
OUTBOUND_CONNECTOR_AMQP_URI=amqp://guest:guest@rabbitmq:5672
```

### Queue naming

AMQP follows the same naming convention as Kafka.
With `OUTBOUND_CONNECTOR_AMQP_EDDIE_ID=eddie`, the queues are:

| Queue                                            | Data                         |
|--------------------------------------------------|------------------------------|
| `ep.eddie.agnostic.connection-status-message`    | Permission status updates    |
| `ep.eddie.agnostic.raw-data-message`             | Raw connector payloads       |
| `ep.eddie.cim_0_82.permission-md`                | Permission market documents  |
| `ep.eddie.cim_0_82.accounting-point-md`          | Accounting point master data |
| `ep.eddie.cim_0_82.validated-historical-data-md` | Historical consumption       |
| `ep.eddie.cim_1_12.near-real-time-data-md`       | Near real-time data          |

Message properties `connection-id`, `permission-id`, and `data-need-id` are set on every outbound message for routing.

### Restart and verify

```shell
docker compose up -d
docker compose logs -f eddie
```

Open the RabbitMQ management console at http://localhost:15672 and log in with `guest` / `guest`.
Navigate to **Queues and Streams**.
EDDIE creates all queues on startup, and messages will appear there after you trigger a simulation flow.

<!-- TODO: screenshot of RabbitMQ management UI showing queues -->

## Your full `.env` on Day 3

After enabling both new connectors, your `.env` should look like this.

```dotenv [.env]
JDBC_URL=jdbc:postgresql://db:5432/eddie
JDBC_USER=eddie
JDBC_PASSWORD=eddie

EDDIE_JWT_HMAC_SECRET=EZawglo19bseUT94HDE7sQDy2sqbX3IhkU+Mveruj5w=

REGION_CONNECTOR_SIM_ENABLED=true
EDDIE_DEMO_BUTTON_ENABLED=true
EDDIE_DATA_NEEDS_CONFIG_FILE=./config/data-needs.json

# REST outbound connector
OUTBOUND_CONNECTOR_REST_ENABLED=true

# Kafka outbound connector
OUTBOUND_CONNECTOR_KAFKA_ENABLED=true
OUTBOUND_CONNECTOR_KAFKA_EDDIE_ID=eddie
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# AMQP outbound connector
OUTBOUND_CONNECTOR_AMQP_ENABLED=true
OUTBOUND_CONNECTOR_AMQP_EDDIE_ID=eddie
OUTBOUND_CONNECTOR_AMQP_URI=amqp://guest:guest@rabbitmq:5672
```

## Choosing a connector

All three connectors receive the same messages.
You can run them all at once during development to compare, then settle on the right one for your architecture.

| Concern             | REST             | Kafka              | AMQP                 |
|---------------------|------------------|--------------------|----------------------|
| Delivery model      | Pull (poll)      | Push (subscribe)   | Push (subscribe)     |
| Message persistence | In-memory, 48h   | Log-based, durable | Queue-based, durable |
| Latency             | Polling interval | Near real-time     | Near real-time       |
| Setup complexity    | None             | Kafka broker       | AMQP broker          |

For the rest of this tutorial, you will build a Spring backend that consumes from the REST connector first (Days 5–8), and revisit Kafka when you explore event-driven patterns on Day 17.

## Checkpoint

- [ ] `docker compose ps` shows `db`, `kafka`, `rabbitmq`, and `eddie` as running
- [ ] `kafka-console-consumer.sh` on topic
  `ep.eddie.agnostic.connection-status-message` returns a message after triggering the simulation
- [ ] The RabbitMQ management console at http://localhost:15672 shows EDDIE's queues under **Queues and Streams**
- [ ] `curl http://localhost:9090/outbound-connectors/rest/agnostic/connection-status-messages` still returns messages

## What's next

On Day 4 you will explore the permission lifecycle in depth — how permissions transition between states, how to terminate them via API, and how to track connections using
`connectionId` and `permissionId`.

[Download the result of the day](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-03.zip)
