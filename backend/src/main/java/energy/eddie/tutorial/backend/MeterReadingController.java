package energy.eddie.tutorial.backend;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
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

    @GetMapping(value = "/api/readings", produces = MediaType.APPLICATION_JSON_VALUE)
    String readings(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "1 hour") String interval) {
        return meterReadingService.findByUserId(jwt.getSubject(), from, to, interval);
    }
}
