package uk.gov.justice.digital.hmpps.whereabouts.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendancesDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendancesResponse;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Set;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Api(tags = {"attendances"})
@RestController()
@RequestMapping(
        value = "attendances",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class AttendancesController {
    private final AttendanceService attendanceService;

    public AttendancesController(final AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create new attendance records for multiple offenders (This endpoint does not trigger IEP warnings)",
            response = AttendancesResponse.class,
            notes = "Stores new attendance record for multiple offenders, posts attendance details back up to PNOMIS")
    public AttendancesResponse postAttendances(
            @ApiParam(value = "Attendance parameters parameters", required = true)
            @RequestBody
            @Valid final AttendancesDto attendances) {

        return AttendancesResponse.builder()
                .attendances(attendanceService.createAttendances(attendances))
                .build();
    }

    @GetMapping("/{prison}/{event-location}")
    @ApiOperation(value = "Returns set of attendance details",
            response = AttendancesResponse.class,
            notes = "Request attendance details")
    public AttendancesResponse getAttendanceForEventLocation(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") String prisonId,
                                                             @ApiParam(value = "Location id of event") @PathVariable("event-location") Long eventLocationId,
                                                             @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) LocalDate date,
                                                             @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") TimePeriod period) {

        return AttendancesResponse.builder()
                .attendances(attendanceService.getAttendanceForEventLocation(prisonId, eventLocationId, date, period))
                .build();

    }

    @GetMapping("/{prison}/absences")
    @ApiOperation(value = "Returns set of attendance details for attendances with an absent reason",
            response = AttendancesResponse.class,
            notes = "Request absences details")
    public AttendancesResponse getAbsences(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") String prisonId,
                                           @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) LocalDate date,
                                           @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") TimePeriod period) {

        return AttendancesResponse.builder()
                .attendances(attendanceService.getAbsences(prisonId, date, period))
                .build();

    }

    @GetMapping("/{prison}")
    @ApiOperation(value = "Returns set of attendance details for set of booking ids",
            response = AttendancesResponse.class,
            notes = "Request attendance details")
    public AttendancesResponse getAttendanceForBookings(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") String prisonId,
                                                        @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) LocalDate date,
                                                        @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") TimePeriod period,
                                                        @ApiParam(value = "Booking ids (bookings=1&bookings=2)", required = true) @RequestParam(name = "bookings") Set<Long> bookings) {

        return AttendancesResponse.builder()
                .attendances(attendanceService.getAttendanceForBookings(prisonId, bookings, date, period))
                .build();

    }

    @PostMapping("/{prison}")
    @ApiOperation(value = "Returns set of attendance details for set of booking ids",
            response = AttendancesResponse.class,
            notes = "Request attendance details")
    public AttendancesResponse getAttendanceForBookingsByPost(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") String prisonId,
                                                              @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) LocalDate date,
                                                              @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") TimePeriod period,
                                                              @ApiParam(value = "Booking ids (bookings=1&bookings=2)", required = true) @RequestBody Set<Long> bookings) {

        return AttendancesResponse.builder()
                .attendances(attendanceService.getAttendanceForBookings(prisonId, bookings, date, period))
                .build();

    }

    @GetMapping("/{prison}/attendance-for-scheduled-activities")
    @ApiOperation(value = "Return a set of attendance details for all offenders that have scheduled activity",
            response = AttendancesResponse.class,
            notes = "Request attendance details")
    public AttendancesResponse getAttendanceForOffendersThatHaveScheduleActivity(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") String prisonId,
                                                                                 @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) LocalDate date,
                                                                                 @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") TimePeriod period) {

        return AttendancesResponse.builder()
                .attendances(attendanceService.getAttendanceForOffendersThatHaveScheduledActivity(prisonId, date, period))
                .build();
    }
}
