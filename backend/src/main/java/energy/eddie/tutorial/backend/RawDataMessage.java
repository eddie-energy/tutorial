package energy.eddie.tutorial.backend;

import java.time.ZonedDateTime;

public record RawDataMessage(
        String connectionId,
        String permissionId,
        String dataNeedId,
        String status,
        DataSourceInformation dataSourceInformation,
        ZonedDateTime timestamp,
        String rawPayload
) {
    private record DataSourceInformation(
            String countryCode,
            String regionConnectorId,
            String meteredDataAdministratorId,
            String permissionAdministratorId) {
    }
}
