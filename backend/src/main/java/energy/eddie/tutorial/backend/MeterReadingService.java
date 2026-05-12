package energy.eddie.tutorial.backend;

import energy.eddie.cim.v1_04.vhd.VHDEnvelope;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
