<!--
Goal: Efficient developer consumption
Activities:
- Retrieve client libraries via Maven / Gradle
- Understand semantic versioning strategy
- Explore documentation structure
- Implement client usage example
Outcome: Reduced integration friction
See: https://architecture.eddie.energy/framework/2-integrating/messages/cim/client-libraries.html
-->

# Day 10 — Client Libraries & Versioning

**Goal**:

- Understand CIM and use it to process incoming data from various regions in one format
- Use the data transfer objects provided by the client library when retrieving data from EDDIE

**Estimated time**: 1h

[Download starting code](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-09.zip)

## Step 1 — About CIM and the EDDIE client library

For the previous days we used the raw data format and wrote our own data transfer objects to communicate with EDDIE.
Following this approach, you would have to write a data transfer object and parser for each region you want to connect.
The Common Information Model (CIM) can model any type of data that a region connector could produce or consume.
This allows you to consume data from and send data to any region connector in one singular format.
CIM is versioned independently of EDDIE and can therefore work with any version of the framework.

The [EDDIE client library](https://architecture.eddie.energy/framework/2-integrating/messages/cim/client-libraries.html)
is available as a [Maven package](https://central.sonatype.com/artifact/energy.eddie/cim)
and holds Java classes for creating and parsing messages for communicating with EDDIE.
This includes
- various CIM documents for its supported versions, as well as
- agnostic documents like connection status messages and raw data.

To use the client library in your app you add it as a dependency in your `build.gradle` file.

```groovy [build.gradle]
implementation 'energy.eddie:cim:3.8.0'
```

## Step 2 — Persisting historical data from any region connector

Inside the `EddieRestClient` adjust the `rawDataMessages` messages method to consume data in CIM format.
Note that the CIM version of the imported model matches that of the endpoint!

```java [EddieRestClient.java]
import energy.eddie.cim.v1_04.vhd.VHDEnvelope;

public void validatedHistoricalData(Consumer<VHDEnvelope> consumer) {
    client.get().uri("/cim_1_04/validated-historical-data-md")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(VHDEnvelope.class)
            .doOnError(error -> LOGGER.error("Error while retrieving validated historical data", error))
            .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5)))
            .subscribe(consumer);
}
```

Now in the `MeterReadingService` we want to save meter readings from incoming validated historical data.
We will do this in a new method `handleValidatedHistoricalData`.

```java [MeterReadingService.java]
private void handleValidatedHistoricalData(VHDEnvelope message) {
    var connectionId = message.getMessageDocumentHeaderMetaInformationConnectionId();
    var permissionId = message.getMessageDocumentHeaderMetaInformationPermissionId();

    var meterReadings = new ArrayList<MeterReading>();

    for (var series : message.getMarketDocument().getTimeSeries()) {
        for (var period : series.getPeriods()) {
            var start = OffsetDateTime.parse(period.getTimeInterval().getStart()).toInstant();
            var duration = Duration.ofMillis(period.getResolution().getTimeInMillis(new Date()));

            for (var point : period.getPoints()) {
                var timestamp = start.plus(duration.multipliedBy(point.getPosition() - 1));
                var quantity = point.getEnergyQuantityQuantity();

                var reading = new MeterReading(connectionId, permissionId, timestamp, quantity);
                meterReadings.add(reading);
            }
        }
    }

    repository.saveAll(meterReadings);
}
```

We then adjust the `init` method to use this handler when data arrives from the EDDIE client.

```java [MeterReadingService.java]
@PostConstruct
void init() {
    eddie.validatedHistoricalData(this::handleValidatedHistoricalData);
}
```

The raw data handler and its dependencies can be safely removed from the `MeterReadingService`.
Your complete `MeterReadingService.java` should now look like this:

```java [MeterReadingService.java]
import energy.eddie.cim.v1_04.vhd.VHDEnvelope;

@Service
class MeterReadingService {

    private final MeterReadingRepository repository;
    private final EddieRestClient eddie;

    MeterReadingService(MeterReadingRepository repository, EddieRestClient eddie) {
        this.repository = repository;
        this.eddie = eddie;
    }

    @PostConstruct
    void init() {
        eddie.validatedHistoricalData(this::handleValidatedHistoricalData);
    }

    List<MeterReading> findLatestPerPermission(String userId) {
        return repository.findLatestPerPermission(userId);
    }

    private void handleValidatedHistoricalData(VHDEnvelope message) {
        var connectionId = message.getMessageDocumentHeaderMetaInformationConnectionId();
        var permissionId = message.getMessageDocumentHeaderMetaInformationPermissionId();

        var meterReadings = new ArrayList<MeterReading>();

        for (var series : message.getMarketDocument().getTimeSeries()) {
            for (var period : series.getPeriods()) {
                var start = OffsetDateTime.parse(period.getTimeInterval().getStart()).toInstant();
                var duration = Duration.ofMillis(period.getResolution().getTimeInMillis(new Date()));

                for (var point : period.getPoints()) {
                    var timestamp = start.plus(duration.multipliedBy(point.getPosition() - 1));
                    var quantity = point.getEnergyQuantityQuantity();

                    var reading = new MeterReading(connectionId, permissionId, timestamp, quantity);
                    meterReadings.add(reading);
                }
            }
        }

        repository.saveAll(meterReadings);
    }
}
```

By using the EDDIE client library we can get rid of our previous data transfer objects.
Inside the `EddieRestClient` simply import the `ConnectionStatusMessage` class from the client library.

```java [EddieRestClient.java]
import energy.eddie.cim.agnostic.ConnectionStatusMessage;
```

In the `UserConnectionService` we need to replace `message.status()` with `message.status().name()`, 
as it is now read as an enum.

```java
@PostConstruct
void init() {
    eddie.connectionStatusMessages(message -> {
        var userConnection = repository
                .findByPermissionId(message.permissionId())
                .map(connection -> {
                    connection.setStatus(message.status().name());
                    return connection;
                })
                .orElse(new UserConnection(
                        message.connectionId(),
                        message.permissionId(),
                        message.dataNeedId(),
                        message.status().name()));
        repository.save(userConnection);
    });
}
```

With that you can remove:
- `ConnectionStatusMessage.java`
- `RawDataMessage.java`
- `SimulationMeterReading.java`

## Checkpoint

- Your app can persist data from any region connector
- Data transfer objects are imported from the client library

## What's next

On day 11 we will complete our tour of EDDIE features by exploring its admin console,
a graphical user interface to trace permissions, inspect region connectors, and manage data needs.

[Download the result of the day](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-10.zip)
