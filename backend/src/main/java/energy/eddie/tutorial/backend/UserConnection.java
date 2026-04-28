package energy.eddie.tutorial.backend;

import jakarta.persistence.*;

@Entity
class UserConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, unique = true)
    private String permissionId;

    @Column(nullable = false)
    private String dataNeedId;

    @Column(nullable = false)
    private String status;

    protected UserConnection() {
    }

    public UserConnection(String userId, String permissionId, String dataNeedId, String status) {
        this.userId = userId;
        this.permissionId = permissionId;
        this.dataNeedId = dataNeedId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public String getDataNeedId() {
        return dataNeedId;
    }

    public String getStatus() {
        return status;
    }
}