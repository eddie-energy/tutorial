package energy.eddie.tutorial.backend;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
class UserConnectionController {

    private final UserConnectionService userConnectionService;

    UserConnectionController(UserConnectionService userConnectionService) {
        this.userConnectionService = userConnectionService;
    }

    @GetMapping("/api/connections")
    List<UserConnection> connections(@AuthenticationPrincipal Jwt jwt) {
        return userConnectionService.findAllByUserId(jwt.getSubject());
    }
}
