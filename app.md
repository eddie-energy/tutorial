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

| Day (changed) | Feature                                       | Frontend Components                                                | Frontend Services                                               | Backend Controllers                            | Backend Services                                                | Entities                                 |
|---------------|-----------------------------------------------|--------------------------------------------------------------------|-----------------------------------------------------------------|------------------------------------------------|-----------------------------------------------------------------|------------------------------------------|
| 5 (10,20)     | App shell, auth, onboarding base              | `AppShell`, `HomePage`, `ConnectPage`, `EddieConnectButtonWrapper` | `AuthService`, `UserSessionService`, `EddieButtonConfigService` | `AuthController`, `UserController`             | `AuthService`, `UserProfileService`                             | `AppUser`                                |
| 6             | Button customization and `connectionId` trace | `ConnectPage`, `ConnectionTraceHint`                               | `ConnectionTraceService`                                        |                                                |                                                                 |                                          |
| 7 (10,17,18)  | Permission and connection tracking            | `PermissionsPage`, `ConnectionStatusList`                          | `PermissionApiService`, `ConnectionApiService`                  | `PermissionController`, `ConnectionController` | `PermissionService`, `ConnectionService`, `OutboundPollService` | `PermissionRecord`, `UserConnection`     |
| 7 (10)        | Raw payload persistence and exploration       | `RawMessagesPage`                                                  | `RawMessagesApiService`                                         | `RawDataController`                            | `RawDataIngestionService`, `MessageNormalizationService`        | `RawOutboundMessage`                     |
| 8 (15)        | Incremental and future data handling          |                                                                    |                                                                 | `DataQualityController`                        | `GapDetectionService`, `ReconciliationService`                  | `ConsumptionReading`, `ImportCheckpoint` |
| 10 (17)       | Typed client and message parsing              |                                                                    |                                                                 |                                                | `CimClientAdapter`, `MarketDocumentMapper`                      |                                          |
| 12 (15)       | Historical charting                           | `ConsumptionChartPage`, `TimeRangePicker`                          | `ConsumptionQueryService`                                       | `ConsumptionController`                        | `ConsumptionQueryService`                                       | `AccountingPoint`, `ConsumptionSeries`   |
| 13 (15)       | Carbon and mix enrichment                     | `EnrichmentLegend`                                                 | `EnrichmentService`                                             | `ConsumptionController`                        | `CarbonMixService`                                              | `EnrichedSeriesPoint`                    |
| 14 (17)       | Real-time acquisition                         |                                                                    |                                                                 | `RealtimeController`                           | `AiidaIngestionService`, `RealtimeProjectionService`            | `RealtimeMeasurement`                    |
| 15 (17)       | Real-time UI                                  | `RealtimePage`, `RealtimeChart`                                    | `RealtimeStreamService`                                         | `RealtimeController`                           | `RealtimeProjectionService`                                     | `RealtimeMeasurement`                    |
| 16 (17)       | IoT command flow                              | `DeviceCommandPage`, `CommandHistory`                              | `DeviceCommandService`                                          | `DeviceCommandController`                      | `DeviceCommandService`, `CommandAckService`                     | `DeviceCommand`, `CommandResult`         |
| 17            | Event-driven switch from REST to Kafka        |                                                                    |                                                                 |                                                | `OutboundIngestionPort`, `KafkaOutboundAdapter`                 | `DomainEvent`                            |

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