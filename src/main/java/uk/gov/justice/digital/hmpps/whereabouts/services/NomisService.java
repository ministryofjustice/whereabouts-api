package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class NomisService {

    private RestTemplate restTemplate;

    public NomisService(@Qualifier("elite2ApiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void updateAttendance(String offenderNo, long activityId, String eventOutcome, String performance) {
        final var url = "/api/bookings/offenderNo/{offenderNo}/activities/{activityId}/attendance";
        restTemplate.put(url, Map.of("eventOutcome", eventOutcome, "performance", performance), offenderNo, activityId);
    }
}
