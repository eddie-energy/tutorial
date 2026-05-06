package energy.eddie.tutorial.backend;

import java.time.ZonedDateTime;
import java.util.List;

public record SimulationMeterReading(
        ZonedDateTime startDateTime,
        String meteringInterval,
        List<SimulationMeasurement> measurements
) {
    public record SimulationMeasurement(
            Double value
    ) {
    }
}
