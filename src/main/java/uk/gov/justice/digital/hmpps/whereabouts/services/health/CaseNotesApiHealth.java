package uk.gov.justice.digital.hmpps.whereabouts.services.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CaseNotesApiHealth extends HealthCheck {

    @Autowired
    public CaseNotesApiHealth(@Qualifier("caseNotesApiHealthRestTemplate") final RestTemplate restTemplate) {
        super(restTemplate);
    }
}
