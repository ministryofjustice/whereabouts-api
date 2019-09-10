package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Elite2ApiService {
    private final OAuth2RestTemplate restTemplate;

    public Elite2ApiService(@Qualifier("elite2ApiRestTemplate") final OAuth2RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void putAttendance(final Long bookingId, final long activityId, final EventOutcome eventOutcome) {
        final var url = "/bookings/{bookingId}/activities/{activityId}/attendance";

        restTemplate.put(url, eventOutcome, bookingId, activityId);
    }

    public void putAttendanceForMultipleBookings(final Set<BookingActivity> bookingActivities, final EventOutcome eventOutcome) {
        final var url = "/bookings/activities/attendance";

        final var body = Map.of(
                "bookingActivities", bookingActivities,
                "eventOutcome", eventOutcome.getEventOutcome(),
                "performance", eventOutcome.getPerformance(),
                "outcomeComment", eventOutcome.getOutcomeComment()
        );

        restTemplate.put(url, body);
    }

    public Set<Long> getBookingIdsForScheduleActivities(final String prisonId, final LocalDate date, final TimePeriod period) {
        final var url = "/schedules/{prisonId}/activities?date={date}&timeSlot={period}";

        final var responseType = new ParameterizedTypeReference<List<Map>>() {
        };
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

    public String getOffenderNoFromBookingId(final Long bookingId) {
        final var entity = restTemplate.getForEntity("/bookings/{bookingId}?basicInfo=true", Map.class, bookingId);
        return (String) Objects.requireNonNull(entity.getBody()).get("offenderNo");
    }
}

