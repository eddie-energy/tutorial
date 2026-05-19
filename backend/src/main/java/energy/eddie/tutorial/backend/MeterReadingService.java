package energy.eddie.tutorial.backend;

import energy.eddie.cim.v1_04.vhd.VHDEnvelope;
import energy.eddie.cim.v1_12.rtd.QuantityTypeKind;
import energy.eddie.cim.v1_12.rtd.RTDEnvelope;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        eddie.nearRealTimeData(this::handleNearRealTimeData);
    }

    List<MeterReading> findLatestPerPermission(String userId) {
        return repository.findLatestPerPermission(userId);
    }

    String findByUserId(String userId, Instant from, Instant to, String interval) {
        return repository.findByUserId(userId, from, to, interval);
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

    private void handleNearRealTimeData(RTDEnvelope message) {
        var meta = message.getMessageDocumentHeader().getMetaInformation();
        var connectionId = meta.getConnectionId();
        var permissionId = meta.getRequestPermissionId();

        var meterReadings = new ArrayList<MeterReading>();

        for (var series : message.getMarketDocument().getTimeSeries()) {
            var timestamp = series.getDateAndOrTimeDateTime().toInstant();

            for (var quantity : series.getQuantities()) {
                if (quantity.getType() == QuantityTypeKind.TOTAL_ACTIVE_ENERGY_CONSUMED_KWH) {
                    var value = quantity.getQuantity().multiply(BigDecimal.valueOf(1000));
                    var reading = new MeterReading(connectionId, permissionId, timestamp, value);
                    meterReadings.add(reading);
                    break;
                }
            }
        }

        repository.saveAll(meterReadings);
    }
}
