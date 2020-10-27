package uk.gov.justice.digital.hmpps.whereabouts.services.health;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public class PrisonApiHealth extends HealthCheck {

    @Autowired
    public PrisonApiHealth(
            @Qualifier("prisonApiHealthWebClient") final WebClient webClient,
            @Value("${api.health-timeout-ms}") final Duration timeout) {
        super(webClient, timeout);
    }
}
