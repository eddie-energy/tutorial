package energy.eddie.tutorial.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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
                .retry()
                .subscribe(consumer);
    }
}