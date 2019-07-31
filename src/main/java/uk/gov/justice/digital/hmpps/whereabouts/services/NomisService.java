package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.dto.elite.CaseNoteDto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
public class NomisService {

    private final OAuth2RestTemplate restTemplate;

    public NomisService(final OAuth2RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void putAttendance(final Long bookingId, final long activityId, final EventOutcome eventOutcome) {
        final var url = "/bookings/{bookingIds}/activities/{activityId}/attendance";

        restTemplate.put(url, eventOutcome, bookingId, activityId);
    }


    public void putAttendanceForMultipleBookings(final Set<Long> bookings, final long activityId, final EventOutcome eventOutcome) {
        final var url = "/bookings/activities/{activityId}/attendance";

        final var body =  Map.of(
                "bookingIds", bookings,
                "eventOutcome", eventOutcome.getEventOutcome(),
                "performance", eventOutcome.getPerformance(),
                "outcomeComment", eventOutcome.getOutcomeComment()
        );

        restTemplate.put(url, body, activityId);
    }

    public CaseNoteDto postCaseNote(final long bookingId, final String type, final String subType, final String text, final LocalDateTime occurrence) {
        final var url = "/bookings/{bookingIds}/caseNotes";

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
        final var url = "/bookings/{bookingIds}/caseNotes/{caseNoteId}";

        restTemplate.put(url, Map.of("text", text), bookingId, caseNoteId);
    }
}

