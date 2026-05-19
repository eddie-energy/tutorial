# Example App

This document tracks the planned application scope for the example app resulting from the tutorial.

## Stack

- Backend: Java/Spring
- Frontend: Angular
- Auth: Keycloak
- Database: PostgreSQL
- Messaging: Kafka

## Services

- EDDIE: 8080, 9090
- AIIDA: 8081
- Backend: 8082
- Keycloak: 8888
- PostgreSQL: 5432
- AMQP: 5672, 15672
- Kafka: 9092
- Frontend: 4200

## Components and Services

| Day (changed) | Feature                                       | Frontend Components                   | Frontend Services                               | Backend Controllers                            | Backend Services                                                   | Entities                               |
|---------------|-----------------------------------------------|---------------------------------------|-------------------------------------------------|------------------------------------------------|--------------------------------------------------------------------|----------------------------------------|
| 5 (10,20)     | App shell, auth, onboarding base              | `App`                                 |                                                 | `UserController`                               | `SecurityConfig`                                                   |                                        |
| 6             | Button customization and `connectionId` trace | `App`                                 |                                                 | `UserConnectionController`                     | `UserConnectionService`, `EddieRestClient`                         | `UserConnection`                       |
| 7 (10,17,18)  | Permission and connection tracking            | `App`                                 |                                                 | `UserConnectionController`                     | `UserConnectionService`, `EddieRestClient`                         | `UserConnection`                       |
| 7 (10)        | Historical data persistence and exploration   | `App`                                 |                                                 | `MeterReadingController`                       | `MeterReadingService`, `MeterReadingRepository`, `EddieRestClient` | `MeterReading`                         |
| 8 (15)        | Incremental and future data handling          | `Chart`, `App`                        |                                                 | `MeterReadingController`                       | `MeterReadingService`, `GapDetectionService`                       | `MeterReading`, `ImportCheckpoint`     |
| 10 (17)       | Typed client and message parsing              |                                       |                                                 |                                                | `EddieRestClient`, `MeterReadingService`                           |                                        |
| 12 (15)       | Historical charting                           | `Chart`, `App`                        |                                                 | `MeterReadingController`                       | `MeterReadingService`, `MeterReadingRepository`                    | `MeterReading`                         |
| 13 (15)       | Carbon and mix enrichment                     | `Chart`, `App`                        |                                                 | `MeterReadingController`                       | `MeterReadingService`, `CarbonMixService`                          | `MeterReading`, `EnrichedReadingPoint` |
| 14 (17)       | Real-time acquisition                         | `App`                                 |                                                 | `RealtimeController`                           | `AiidaIngestionService`, `RealtimeProjectionService`               | `RealtimeMeasurement`                  |
| 15 (17)       | Real-time UI                                  | `Chart`, `RealtimeChart`, `App`       | `RealtimeStreamService`                         | `RealtimeController`                           | `RealtimeProjectionService`                                        | `RealtimeMeasurement`                  |
| 16 (17)       | IoT command flow                              | `DeviceCommandPage`, `CommandHistory` | `DeviceCommandService`                          | `DeviceCommandController`                      | `DeviceCommandService`, `CommandAckService`                        | `DeviceCommand`, `CommandResult`       |
| 17            | Event-driven switch from REST to Kafka        | `App`, `Chart`, `RealtimeChart`       | `RealtimeStreamService`, `DeviceCommandService` | `MeterReadingController`, `RealtimeController` | `OutboundIngestionPort`, `KafkaOutboundAdapter`                    | `DomainEvent`                          |

## Connection tracking

Two options for tracking permissions from the app:

- Connection ID = User ID → 1 connection ID ⇔ N permission IDs
- Connection IDs map to User IDs → 1 connection ID ⇔ 1 permission ID

For registering created connections:
- The frontend can send an API call when the button dispatches the creation event
- The backend can subscribe to status messages and create connections when it sees a new permission creation

## Questions

- Label to created connections?
- How to style without effort?