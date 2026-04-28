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
class UserController {

    private final UserConnectionService userConnectionService;

    UserController(UserConnectionService userConnectionService) {
        this.userConnectionService = userConnectionService;
    }

    @GetMapping("/api/me")
    Map<String, String> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("id", jwt.getSubject(), "name", jwt.getClaimAsString("name"));
    }

    @GetMapping("/api/connections")
    List<UserConnection> connections(@AuthenticationPrincipal Jwt jwt) {
        return userConnectionService.findAllByUserId(jwt.getSubject());
    }
}
