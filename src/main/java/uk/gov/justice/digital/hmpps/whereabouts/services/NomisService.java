package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.digital.hmpps.whereabouts.dto.CaseNoteDto;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class NomisService {

    private RestTemplate restTemplate;

    public NomisService(@Qualifier("elite2ApiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void putAttendance(final Long bookingId, final long activityId, final EventOutcome eventOutcome) {
        final var url = "/bookings/{bookingId}/activities/{activityId}/attendance";

        restTemplate.put(url, eventOutcome, bookingId, activityId);
    }

    public CaseNoteDto postCaseNote(final long bookingId, final String type, final String subType, final String text, final LocalDateTime occurrence) {
        final var url = "/bookings/{bookingId}/caseNotes";

        final var response = restTemplate.postForEntity(
                url,
                Map.of(
                        "type", type,
                        "subType", subType,
                        "text", text,
                        "occurrence", occurrence.toString()),
                CaseNoteDto.class, bookingId);

        return response.getBody();
    }

    public void putCaseNoteAmendment(final long bookingId, final long caseNoteId, final String text) {
        final var url = "/bookings/{bookingId}/caseNotes/{caseNoteId}";

        restTemplate.put(url, Map.of("text", text), bookingId, caseNoteId);
    }
}

