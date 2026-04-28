package energy.eddie.tutorial.backend;

public record ConnectionStatusMessage(
        String connectionId,
        String permissionId,
        String dataNeedId,
        String status
) {
}