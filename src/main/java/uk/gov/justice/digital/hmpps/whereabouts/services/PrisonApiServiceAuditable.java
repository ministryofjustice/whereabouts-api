package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PrisonApiServiceAuditable extends PrisonApi {
    public PrisonApiServiceAuditable(@Qualifier("prisonAPiWebClientAuditable") final WebClient webClient) {
        super(webClient);
    }
}
