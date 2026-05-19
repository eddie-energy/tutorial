package energy.eddie.tutorial.backend;

import energy.eddie.cim.v1_12.rtd.RTDEnvelope;
import energy.eddie.cim.v1_04.vhd.VHDEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Consumer;

@Component
public class EddieRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EddieRestClient.class);

    private final WebClient client = WebClient.create("http://localhost:9090/outbound-connectors/rest");

    public void connectionStatusMessages(Consumer<ConnectionStatusMessage> consumer) {
        client.get().uri("/agnostic/connection-status-messages")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(ConnectionStatusMessage.class)
                .doOnError(error -> LOGGER.error("Error while retrieving connection status messages", error))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5)))
                .subscribe(consumer);
    }

    public void validatedHistoricalData(Consumer<VHDEnvelope> consumer) {
        client.get().uri("/cim_1_04/validated-historical-data-md")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(VHDEnvelope.class)
                .doOnError(error -> LOGGER.error("Error while retrieving permission-md", error))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5)))
                .subscribe(consumer);
    }

    public void nearRealTimeData(Consumer<RTDEnvelope> consumer) {
        client.get().uri("/cim_1_12/near-real-time-data-md")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(RTDEnvelope.class)
                .doOnError(error -> LOGGER.error("Error while retrieving near real-time data", error))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5)))
                .subscribe(consumer);
    }
}