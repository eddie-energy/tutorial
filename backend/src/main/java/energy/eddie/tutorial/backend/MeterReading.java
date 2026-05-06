package energy.eddie.tutorial.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
class MeterReading {

    @EmbeddedId
    private Id id;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private BigDecimal quantity;

    protected MeterReading() {
    }

    public MeterReading(String userId, String permissionId, Instant timestamp, BigDecimal quantity) {
        this.id = new Id(permissionId, timestamp);
        this.userId = userId;
        this.quantity = quantity;
    }

    @Embeddable
    record Id(
            String permissionId,
            Instant timestamp
    ) {
    }

    public String getPermissionId() {
        return id.permissionId;
    }

    public Instant getTimestamp() {
        return id.timestamp;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
}