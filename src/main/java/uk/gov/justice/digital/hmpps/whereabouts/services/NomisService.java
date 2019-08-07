package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity;
import uk.gov.justice.digital.hmpps.whereabouts.dto.elite.CaseNoteDto;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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


    public void putAttendanceForMultipleBookings(final Set<BookingActivity> bookingActivities, final EventOutcome eventOutcome) {
        final var url = "/bookings/activities/attendance";

        final var body =  Map.of(
                "bookingActivities", bookingActivities,
                "eventOutcome", eventOutcome.getEventOutcome(),
                "performance", eventOutcome.getPerformance(),
                "outcomeComment", eventOutcome.getOutcomeComment()
        );

        restTemplate.put(url, body);
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

    public Set<Long> getBookingIdsForScheduleActivities(final String prisonId, final LocalDate date, final TimePeriod period) {
        final var url = "/bookings/schedules/{prisonId}/activities?date={date}&period={period}";

        final var responseType = new ParameterizedTypeReference<List<Map>>() {};
        final var response = restTemplate.exchange(url, HttpMethod.GET, null, responseType, prisonId, date, period);
        final var body = response.getBody();

        if (body == null)
            return Collections.emptySet();

        return body
                .stream()
                .filter(entry -> entry.containsKey("bookingId"))
                .map(entry -> Long.valueOf(entry.get("bookingId").toString()))
                .collect(Collectors.toSet());

    }
}

