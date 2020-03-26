package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.justice.digital.hmpps.whereabouts.dto.*;
import uk.gov.justice.digital.hmpps.whereabouts.model.Location;
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class Elite2ApiService {
    private final WebClient webClient;

    public Elite2ApiService(@Qualifier("elite2WebClient") final WebClient webClient) {
        this.webClient = webClient;
    }

    public void putAttendance(final Long bookingId, final long activityId, final EventOutcome eventOutcome) {
        webClient.put()
                .uri("/bookings/{bookingId}/activities/{activityId}/attendance", bookingId, activityId)
                .bodyValue(eventOutcome)
                .retrieve();
    }

    public void putAttendanceForMultipleBookings(final Set<BookingActivity> bookingActivities, final EventOutcome eventOutcome) {
        webClient.post()
                .uri("/bookings/activities/attendance")
                .bodyValue(new EventOutcomesDto(
                        eventOutcome.getEventOutcome(),
                        eventOutcome.getPerformance(),
                        eventOutcome.getOutcomeComment(),
                        bookingActivities
                ))
                .retrieve();
    }

    public Set<Long> getBookingIdsForScheduleActivities(final String prisonId, final LocalDate date, final TimePeriod period) {
        final var responseType = new ParameterizedTypeReference<List<Map>>() {
        };

        //TODO: Make it more reactive
        return Objects.requireNonNull(webClient.get()
                .uri("/schedules/{prisonId}/activities?date={date}&timeSlot={period}", prisonId, date, period)
                .retrieve()
                .bodyToMono(responseType)
                .block()).stream()
                .filter(entry -> entry.containsKey("bookingId"))
                .map(entry -> Long.valueOf(entry.get(Integer.parseInt("bookingId")).toString()))
                .collect(Collectors.toSet());
    }

    public String getOffenderNoFromBookingId(final Long bookingId) {
        return webClient.get()
                .uri("/bookings/{bookingId}?basicInfo=true", bookingId)
                .retrieve()
                .bodyToMono(Map.class)
                .map(entry -> entry.get("offenderNo"))
                .cast(String.class)
                .block();
    }

    public List<Long> getBookingIdsForScheduleActivitiesByDateRange(final String prisonId, final TimePeriod period, final LocalDate fromDate, final LocalDate toDate) {
        final var responseType = new ParameterizedTypeReference<List<Map>>() {
        };

        return Objects.requireNonNull(webClient.get()
                .uri("/schedules/{prisonId}/activities-by-date-range?fromDate={fromDate}&toDate={toDate}&timeSlot={period}", prisonId, fromDate, toDate, period)
                .retrieve()
                .bodyToMono(responseType)
                .block())
                .stream()
                .filter(entry -> entry.containsKey("bookingId"))
                .map(entry -> Long.valueOf(entry.get("bookingId").toString()))
                .collect(Collectors.toList());
    }

    public List<OffenderDetails> getScheduleActivityOffenderData(final String prisonId, final LocalDate fromDate, final LocalDate toDate, final TimePeriod period) {
        final var responseType = new ParameterizedTypeReference<List<OffenderDetails>>() {
        };

        return webClient.get()
                .uri("/schedules/{prisonId}/activities-by-date-range?fromDate={fromDate}&toDate={toDate}&timeSlot={period}", prisonId, fromDate, toDate, period)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    public Long getOffenderBookingId(final String offenderNo) {

        final var responseType = new ParameterizedTypeReference<OffenderDetails>() {
        };

        return webClient.get()
                .uri("/bookings/offenderNo/{offenderNo}?fullInfo=false", offenderNo)
                .retrieve()
                .bodyToMono(responseType)
                .map(OffenderDetails::getBookingId)
                .block();
    }

    public List<LocationGroup> getLocationGroups(final String agencyId) {
        final var responseType = new ParameterizedTypeReference<List<LocationGroup>>() {
        };

        return webClient.get()
                .uri("/agencies/{agencyId}/locations/groups", agencyId)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    public List<Location> getAgencyLocationsForType(final String agencyId, final String locationType) {
        final var responseType = new ParameterizedTypeReference<List<Location>>() {
        };

        return webClient.get()
                .uri("/agencies/{agencyId}/locations/type/{type}", agencyId, locationType)
                .retrieve()
                .onStatus(NOT_FOUND::equals, response -> Mono.error(new EntityNotFoundException(String.format("Locations not found for agency %s with location type %s", agencyId, locationType))))
                .bodyToMono(responseType)
                .block();
    }

    public Long postAppointment(final long bookingId, @NotNull CreateBookingAppointment createbookingAppointment) {
        final var responseType = new ParameterizedTypeReference<Event>() {
        };
        return webClient.post()
                .uri("/bookings/{bookingId}/appointments", bookingId)
                .bodyValue(createbookingAppointment)
                .retrieve()
                .bodyToMono(responseType)
                .map(Event::getEventId)
                .block();
    }
}

