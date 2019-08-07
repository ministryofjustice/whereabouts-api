package uk.gov.justice.digital.hmpps.whereabouts.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.hmpps.whereabouts.dto.*;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceLocked;
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceNotFound;
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Set;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Api(tags = {"attendance"})
@RestController()
@RequestMapping(
        value="attendance",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(final AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create new attendance",
            response = AttendAllDto.class,
            notes = "Stores new attendance record, posts attendance details back up to PNOMIS. IEP warnings are triggered when certain absence reasons are used.")
    public AttendanceDto postAttendance(
            @ApiParam(value = "Attendance details", required = true)
            @RequestBody
            @Valid final CreateAttendanceDto attendance) {

        return attendanceService.createAttendance(attendance);
    }

    @PostMapping(value = "/attend-all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Attend all",
            responseContainer = "Set",
            notes = "Stores new attendance records as paid attended, posts attendance details back up to PNOMIS for multiple booking ids")
    public Set<AttendanceDto> attendAll(
            @ApiParam(value = "Attend all parameters", required = true)
            @RequestBody
            @Valid final AttendAllDto attendAll) {

        return attendanceService.attendAll(attendAll);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Updates existing attendance information",
            notes = "Updates the attendance record, posts attendance details back up to PNOMIS. IEP warnings are triggered when certain absence reasons are used.")
    public ResponseEntity<Object> putAttendance(@PathVariable("id") long id, @RequestBody @Valid final UpdateAttendanceDto attendance) {
        try {
            attendanceService.updateAttendance(id, attendance);
        } catch (AttendanceNotFound e) {
            return ResponseEntity.notFound().build();
        } catch (AttendanceLocked e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{prison}/{event-location}")
    @ApiOperation(value = "Returns set of attendance details",
            response = AttendanceDto.class,
            responseContainer = "Set",
            notes = "Request attendance details")
    public Set<AttendanceDto> getAttendanceForEventLocation(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") String prisonId,
                                            @ApiParam(value = "Location id of event") @PathVariable("event-location") Long eventLocationId,
                                            @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) LocalDate date,
                                            @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") TimePeriod period) {

        return attendanceService.getAttendanceForEventLocation(prisonId, eventLocationId, date, period);

    }

    @GetMapping("/{prison}/absences")
    @ApiOperation(value = "Returns set of attendance details for attendances with an absent reason",
            response = AttendanceDto.class,
            responseContainer = "Set",
            notes = "Request absences details")
    public Set<AttendanceDto> getAbsences(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") String prisonId,
                                        @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) LocalDate date,
                                        @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") TimePeriod period) {

        return attendanceService.getAbsences(prisonId, date, period);

    }

    @GetMapping("/{prison}")
    @ApiOperation(value = "Returns set of attendance details for set of booking ids",
            response = AttendanceDto.class,
            responseContainer = "Set",
            notes = "Request attendance details")
    public Set<AttendanceDto> getAttendanceForBookings(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") String prisonId,
                                            @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) LocalDate date,
                                            @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") TimePeriod period,
                                            @ApiParam(value = "Booking ids (bookings=1&bookings=2)", required = true) @RequestParam(name = "bookings") Set<Long> bookings) {

        return attendanceService.getAttendanceForBookings(prisonId, bookings, date, period);

    }

    @GetMapping("/{prison}/attendance-for-scheduled-activities")
    @ApiOperation(value = "Return a set of attendance details for all offenders that have scheduled activity",
        response = AttendanceDto.class,
        responseContainer = "Set",
        notes = "Request attendance details")
    public Set<AttendanceDto> getAttendanceForOffendersThatHaveScheduleActivity(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") String prisonId,
                                                       @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) LocalDate date,
                                                       @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") TimePeriod period) {

        return attendanceService.getAttendanceForOffendersThatHaveScheduledActivity(prisonId, date, period);
    }

    @GetMapping("/absence-reasons")
    public AbsentReasonsDto reasons() {
       return attendanceService.getAbsenceReasons();
    }
}
