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

    public void updateAttendance(String offenderNo, long activityId, EventOutcome eventOutcome) {
        final var url = "/bookings/offenderNo/{offenderNo}/activities/{activityId}/attendance";
        restTemplate.put(url, eventOutcome, offenderNo, activityId);

    }

    public CaseNoteDto postCaseNote(long bookingId, String type, String subType, String text, LocalDateTime occurrence) {
        final var url = "/bookings/{bookingId}/caseNotes";

        restTemplate.postForEntity(url,
                Map.of(
                        "type", type,
                        "subType", subType,
                        "text", text,
                        "occurrence", occurrence.toString()),
                null, bookingId);

        return new CaseNoteDto();
    }
}
