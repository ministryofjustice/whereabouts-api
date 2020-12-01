package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity;
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveResult;
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment;
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event;
import uk.gov.justice.digital.hmpps.whereabouts.dto.EventOutcomesDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderDetails;
import uk.gov.justice.digital.hmpps.whereabouts.model.CellWithAttributes;
import uk.gov.justice.digital.hmpps.whereabouts.model.Location;
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup;
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment;
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
public class PrisonApiService {
    private final WebClient webClient;

    public PrisonApiService(@Qualifier("elite2WebClient") final WebClient webClient) {
        this.webClient = webClient;
    }

    public void putAttendance(final Long bookingId, final long activityId, final EventOutcome eventOutcome) {
        webClient.put()
                .uri("/bookings/{bookingId}/activities/{activityId}/attendance", bookingId, activityId)
                .bodyValue(eventOutcome)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public void putAttendanceForMultipleBookings(final Set<BookingActivity> bookingActivities, final EventOutcome eventOutcome) {
        webClient.put()
                .uri("/bookings/activities/attendance")
                .bodyValue(new EventOutcomesDto(
                        eventOutcome.getEventOutcome(),
                        eventOutcome.getPerformance(),
                        eventOutcome.getOutcomeComment(),
                        bookingActivities
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public Set<Long> getBookingIdsForScheduleActivities(final String prisonId, final LocalDate date, final TimePeriod period) {
        final var responseType = new ParameterizedTypeReference<List<Map>>() {
        };

        return Objects.requireNonNull(webClient.get()
                .uri("/schedules/{prisonId}/activities?date={date}&timeSlot={period}", prisonId, date, period)
                .retrieve()
                .bodyToMono(responseType)
                .block())
                .stream()
                .map(entry -> Long.parseLong(entry.get("bookingId").toString()))
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

    public List<OffenderDetails> getScheduleActivityOffenderData(final String prisonId, final LocalDate fromDate, final LocalDate toDate, final TimePeriod period) {
        final var responseType = new ParameterizedTypeReference<List<OffenderDetails>>() {
        };

        return webClient.get()
                .uri("/schedules/{prisonId}/activities-by-date-range?fromDate={fromDate}&toDate={toDate}&timeSlot={period}&includeSuspended=true", prisonId, fromDate, toDate, period)
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

        try {
            return webClient.get()
                    .uri("/agencies/{agencyId}/locations/groups", agencyId)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().equals(NOT_FOUND)) {
                throw new EntityNotFoundException(String.format("Locations not found for agency %s", agencyId));
            }
            throw e;
        }
    }

    public List<Location> getAgencyLocationsForType(final String agencyId, final String locationType) {
        final var responseType = new ParameterizedTypeReference<List<Location>>() {
        };

        try {
            return webClient.get()
                    .uri("/agencies/{agencyId}/locations/type/{type}", agencyId, locationType)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().equals(NOT_FOUND)) {
                throw new EntityNotFoundException(String.format("Locations not found for agency %s with location type %s", agencyId, locationType));
            }
            throw e;
        }
    }

    public List<CellWithAttributes> getCellsWithCapacity(final String agencyId, final String attribute) {
        final var responseType = new ParameterizedTypeReference<List<CellWithAttributes>>() {
        };
        final var uri = attribute != null ? "/agencies/{agencyId}/cellsWithCapacity?attribute={attribute}" : "/agencies/{agencyId}/cellsWithCapacity";

        try {
            return webClient.get()
                    .uri(uri, agencyId, attribute)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().equals(NOT_FOUND)) {
                throw new EntityNotFoundException(String.format("No cells with capacity for agency %s and attribute %s", agencyId, attribute));
            }
            throw e;
        }
    }

    public Event postAppointment(final long bookingId, @NotNull CreateBookingAppointment createbookingAppointment) {
        final var responseType = new ParameterizedTypeReference<Event>() {
        };

        return webClient.post()
                .uri("/bookings/{bookingId}/appointments", bookingId)
                .bodyValue(createbookingAppointment)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    public CellMoveResult putCellMove(final long bookingId, final String internalLocationDescription, final String reasonCode) {
        final var responseType = new ParameterizedTypeReference<CellMoveResult>() {
        };

        return webClient.put()
                .uri("/bookings/{bookingId}/living-unit/{internalLocationDescription}?reasonCode={reasonCode}",
                        bookingId, internalLocationDescription, reasonCode)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    public List<PrisonAppointment> getPrisonAppointmentsForBookingId(long bookingId, int offset, int limit) {
        final var responseType = new ParameterizedTypeReference<List<PrisonAppointment>>() {
        };

        return webClient.get()
                .uri("/bookings/{bookingId}/appointments", bookingId)
                .header("fromDate", "2019-01-01")
                .header("toDate", "2050-01-01")
                .header("Page-Offset", Integer.toString(offset))
                .header("Page-Limit", Integer.toString(limit))
                .header("Sort-Fields", "startTime")
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    public PrisonAppointment getPrisonAppointment(long appointmentId) {
        return webClient.get()
                .uri("/appointments/{appointmentId}", appointmentId)
                .retrieve()
                .bodyToMono(PrisonAppointment.class)
                .onErrorResume(WebClientResponseException.NotFound.class, notFound -> Mono.empty())
                .blockOptional()
                .orElse(null);
    }

    public void deleteAppointment(final Long appointmentId) {
        try {
            webClient.delete()
                    .uri("/appointments/{appointmentId}", appointmentId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (!e.getStatusCode().equals(NOT_FOUND)) {
                throw e;
            }
        }
    }
}

