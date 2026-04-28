package energy.eddie.tutorial.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface UserConnectionRepository extends JpaRepository<UserConnection, String> {
    List<UserConnection> findAllByUserId(String userId);
    Optional<UserConnection> findByPermissionId(String permissionId);
}