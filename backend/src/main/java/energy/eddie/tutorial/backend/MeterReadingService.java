package energy.eddie.tutorial.backend;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
class MeterReadingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeterReadingService.class);

    private final MeterReadingRepository repository;
    private final EddieRestClient eddie;
    private final ObjectMapper objectMapper;

    MeterReadingService(MeterReadingRepository repository, EddieRestClient eddie, ObjectMapper objectMapper) {
        this.repository = repository;
        this.eddie = eddie;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        eddie.rawDataMessages(message -> {
            if (message.dataSourceInformation().regionConnectorId().equals("sim")) {
                try {
                    var simulationReading = objectMapper.readValue(message.rawPayload(), SimulationMeterReading.class);
                    var interval = Duration.parse(simulationReading.meteringInterval());

                    var meterReadings = new ArrayList<MeterReading>();

                    var step = 1;
                    for (var measurement : simulationReading.measurements()) {
                        var timestamp = simulationReading
                                .startDateTime()
                                .plus(interval.multipliedBy(step++));

                        meterReadings.add(new MeterReading(
                                message.connectionId(),
                                message.permissionId(),
                                timestamp.toInstant(),
                                BigDecimal.valueOf(measurement.value())));
                    }

                    repository.saveAll(meterReadings);
                } catch (JacksonException e) {
                    LOGGER.warn("Failed to read simulation meter reading.", e);
                }
            }
        });
    }

    List<MeterReading> findLatestPerPermission(String userId) {
        return repository.findLatestPerPermission(userId);
    }
}
