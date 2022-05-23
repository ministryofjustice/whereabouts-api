package uk.gov.justice.digital.hmpps.whereabouts.services;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity;
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveResult;
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment;
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreatePrisonAppointment;
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreatedAppointmentDetailsDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse;
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event;
import uk.gov.justice.digital.hmpps.whereabouts.dto.EventOutcomesDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderBooking;
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderDetails;
import uk.gov.justice.digital.hmpps.whereabouts.dto.ScheduledEventDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.LocationDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.OffenderAttendance;
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentSearchDto;
import uk.gov.justice.digital.hmpps.whereabouts.model.CellWithAttributes;
import uk.gov.justice.digital.hmpps.whereabouts.model.Location;
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup;
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public abstract class PrisonApi {
    private static final Logger logger = LoggerFactory.getLogger(PrisonApi.class);
    private final WebClient webClient;

    public PrisonApi(final WebClient webClient) {
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

    @Data
    public static class AttendancePage {
        List<OffenderAttendance> content;
        Integer totalPages;
    }

    public Page<OffenderAttendance> getAttendanceForOffender(final String offenderNo, final LocalDate fromDate, final LocalDate toDate,
                                                             final String outcome, final Pageable pageable) {

        final var data = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/offender-activities/{offenderNo}/attendance-history")
                        .queryParam("fromDate", fromDate)
                        .queryParam("toDate", toDate)
                        .queryParamIfPresent("outcome", Optional.ofNullable(outcome))
                        .queryParam("page", pageable.isPaged() ? pageable.getPageNumber() : 0)
                        .queryParam("size", pageable.isPaged() ? pageable.getPageSize() : 10000)
                        .build(offenderNo))
                .retrieve()
                .bodyToMono(AttendancePage.class)
                .block();
        if (data == null) {
            throw new RuntimeException("No data returned");
        }
        if (data.getTotalPages() > 1) {
            throw new RuntimeException("Too many rows returned");
        }
        return new PageImpl<>(data.content, pageable, data.totalPages);
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

    public List<OffenderBooking> getOffenderDetailsFromOffenderNos(final Collection<String> offenderNos) {
        final var responseType = new ParameterizedTypeReference<List<OffenderBooking>>() {
        };

        return webClient.post()
                .uri("/bookings/offenders")
                .bodyValue(offenderNos)
                .retrieve()
                .bodyToMono(responseType)
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

    public PrisonerActivitiesCount getScheduleActivityCount(final String prisonId, final LocalDate fromDate, final LocalDate toDate, final Set<TimePeriod> periods) {
        return webClient.get()
                .uri("/schedules/{prisonId}/count-activities",
                        b -> b.queryParam("fromDate", fromDate)
                                .queryParam("toDate", toDate)
                                .queryParam("timeSlots", periods)
                                .build(prisonId))
                .retrieve()
                .bodyToMono(PrisonerActivitiesCount.class)
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

    /**
     * Version of getAgencyLocationsForType that does not check that the invoker has the selected agency in their caseload.
     *
     * @param agencyId     'WWI' etc.
     * @param locationType 'APP', 'CELL'
     * @return set of matching locations.
     */
    public List<Location> getAgencyLocationsForTypeUnrestricted(final String agencyId, final String locationType) {
        final var responseType = new ParameterizedTypeReference<List<Location>>() {
        };

        try {
            return webClient.get()
                    .uri("/agencies/{agencyId}/locations?eventType={locationType}", agencyId, locationType)
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

    public List<CreatedAppointmentDetailsDto> createAppointments(final CreatePrisonAppointment createPrisonAppointment) {
        final var responseType = new ParameterizedTypeReference<List<CreatedAppointmentDetailsDto>>() {
        };

        return webClient.post()
                .uri("/appointments")
                .bodyValue(createPrisonAppointment)
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

        webClient.delete()
                .uri("/appointments/{appointmentId}", appointmentId)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(WebClientResponseException.NotFound.class, notFound -> {
                    logger.info("Ignoring appointment with id: '{}' that does not exist in nomis", appointmentId);
                    return Mono.empty();
                })
                .block();
    }

    public void updateAppointmentComment(long appointmentId, @Nullable String comment) {
        webClient.put()
                .uri("/appointments/{appointmentId}/comment", appointmentId)
                .contentType(MediaType.TEXT_PLAIN)
                .body(comment != null ? BodyInserters.fromValue(comment) : BodyInserters.empty())
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(WebClientResponseException.NotFound.class, notFound -> {
                    logger.info("Ignoring appointment with id: '{}' that does not exist in nomis", appointmentId);
                    return Mono.empty();
                })
                .block();
    }

    public List<ScheduledAppointmentSearchDto> getScheduledAppointments(final String agencyId, final LocalDate date, final TimePeriod timeSlot, final Long locationId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/schedules/{agencyId}/appointments")
                        .queryParam("date", "{date}")
                        .queryParamIfPresent("timeSlot", Optional.ofNullable(timeSlot))
                        .queryParamIfPresent("locationId", Optional.ofNullable(locationId))
                        .build(agencyId, date))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ScheduledAppointmentSearchDto>>() {
                })
                .block();
    }


    public List<ScheduledAppointmentDto> getScheduledAppointments(final String agencyId, final LocalDate date) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/schedules/{agencyId}/appointments")
                        .queryParam("date", "{date}")
                        .build(agencyId, date))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ScheduledAppointmentDto>>() {
                })
                .block();
    }


    public LocationDto getLocation(long locationId) {
        return webClient.get()
                .uri("/locations/{locationId}", locationId)
                .retrieve()
                .bodyToMono(LocationDto.class)
                .onErrorResume(WebClientResponseException.NotFound.class, notFound -> Mono.empty())
                .blockOptional()
                .orElse(null);
    }

    public void deleteAppointments(final List<Long> appointmentIds) {
        webClient.post()
                .uri("/appointments/delete")
                .bodyValue(appointmentIds)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public List<ScheduledEventDto> getEvents(final String offenderNo, final LocalDate fromDate, final LocalDate toDate) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/offenders/{offenderNo}/events")
                        .queryParam("fromDate", "{fromDate}")
                        .queryParam("toDate", "{toDate}")
                        .build(offenderNo, fromDate, toDate))
                .retrieve()
                .onStatus(s -> s == NOT_FOUND, response ->
                        response.bodyToMono(ErrorResponse.class).map(r -> new EntityNotFoundException(r.getDeveloperMessage()))
                )
                .bodyToMono(new ParameterizedTypeReference<List<ScheduledEventDto>>() {
                })
                .block();
    }
}
