package energy.eddie.tutorial.backend;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
class MeterReadingController {

    private final MeterReadingService meterReadingService;

    MeterReadingController(MeterReadingService meterReadingService) {
        this.meterReadingService = meterReadingService;
    }

    @GetMapping("/api/readings/latest")
    List<MeterReading> latest(@AuthenticationPrincipal Jwt jwt) {
        return meterReadingService.findLatestPerPermission(jwt.getSubject());
    }
}
