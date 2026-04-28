package energy.eddie.tutorial.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class UserConnectionService {
    private final UserConnectionRepository repository;
    private final EddieRestClient eddie;

    UserConnectionService(UserConnectionRepository repository, EddieRestClient eddie) {
        this.repository = repository;
        this.eddie = eddie;
    }

    @PostConstruct
    void init() {
        eddie.connectionStatusMessages(message -> {
            var userConnection = repository
                    .findByPermissionId(message.permissionId())
                    .orElse(new UserConnection(
                            message.connectionId(),
                            message.permissionId(),
                            message.dataNeedId(),
                            message.status()));
            repository.save(userConnection);
        });
    }

    List<UserConnection> findAllByUserId(String userId) {
        return repository.findAllByUserId(userId);
    }
}