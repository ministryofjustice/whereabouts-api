package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.digital.hmpps.whereabouts.dto.*;
import uk.gov.justice.digital.hmpps.whereabouts.model.Location;
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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

        final var body = new EventOutcomesDto(
                eventOutcome.getEventOutcome(),
                eventOutcome.getPerformance(),
                eventOutcome.getOutcomeComment(),
                bookingActivities
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

    public List<Long> getBookingIdsForScheduleActivitiesByDateRange(final String prisonId, final TimePeriod period, final LocalDate fromDate, final LocalDate toDate) {
        final var url = "/schedules/{prisonId}/activities-by-date-range?fromDate={fromDate}&toDate={toDate}&timeSlot={period}";

        final var responseType = new ParameterizedTypeReference<List<Map>>() {
        };
        final var response = restTemplate.exchange(url, HttpMethod.GET, null, responseType, prisonId, fromDate, toDate, period);
        final var body = response.getBody();

        if (body == null)
            return emptyList();

        return body
                .stream()
                .filter(entry -> entry.containsKey("bookingId"))
                .map(entry -> Long.valueOf(entry.get("bookingId").toString()))
                .collect(Collectors.toList());
    }

    public List<OffenderDetails> getScheduleActivityOffenderData(final String prisonId, final LocalDate fromDate, final LocalDate toDate, final TimePeriod period) {
        final var url = "/schedules/{prisonId}/activities-by-date-range?fromDate={fromDate}&toDate={toDate}&timeSlot={period}";

        final var responseType = new ParameterizedTypeReference<List<OffenderDetails>>() {
        };
        final var response = restTemplate.exchange(url, HttpMethod.GET, null, responseType, prisonId, fromDate, toDate, period);
        final var body = response.getBody();

        return body != null ? body : emptyList();
    }

    public Long getOffenderBookingId(final String offenderNo) {
       final var url = "/bookings/offenderNo/{offenderNo}?fullInfo=false";

        final var responseType = new ParameterizedTypeReference<OffenderDetails>() {};
        final var response = restTemplate.exchange(url, HttpMethod.GET, null, responseType, offenderNo);
        final var body = response.getBody();

        return Objects.requireNonNull(body).getBookingId();
    }

    List<LocationGroup> getLocationGroups(final String agencyId) {
        final var url = "/agencies/{agencyId}/locations/groupsNew";

        final var responseType = new ParameterizedTypeReference<List<LocationGroup>>() {};
        final var response = restTemplate.exchange(url, HttpMethod.GET, null, responseType, agencyId);
        final var body = response.getBody();

        if (body == null) {
            return emptyList();
        }

        return body;
    }

    public List<Location> getAgencyLocationsForType(final String agencyId, final String locationType) {
        final var url = "/agencies/{agencyId}/locations/type/{type}";
        final var responseType = new ParameterizedTypeReference<List<Location>>() {};

        List<Location> locations;
        try {
            final var response = restTemplate.exchange(url, HttpMethod.GET, null, responseType, agencyId, locationType);
            locations = response.getBody();
        } catch(HttpClientErrorException e) {
            if (e.getStatusCode().equals(NOT_FOUND)) {
                throw new EntityNotFoundException(String.format("Locations not found for agency %s with location type %s", agencyId, locationType));
            }
            throw e;
        }

        return locations;
    }

    public Long postAppointment(final long bookingId, @NotNull CreateBookingAppointment createbookingAppointment) {
        final var url  = "/bookings/{bookingId}/appointments";
        final var responseType = new ParameterizedTypeReference<Event>() {};
        final var response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(createbookingAppointment, null), responseType, bookingId);

        return Objects.requireNonNull(response.getBody()).getEventId();
    }
}

