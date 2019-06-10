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
import uk.gov.justice.digital.hmpps.whereabouts.dto.AbsentReasonsDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.UpdateAttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
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

    private AttendanceService attendanceService;

    public AttendanceController(final AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create new attendance",
            notes = "Stores new attendance record, posts attendance details back up to PNOMIS. IEP warnings are triggered when certain absence reasons are used.")
    public AttendanceDto postAttendance(
            @ApiParam(value = "Attendance details", required = true)
            @RequestBody
            @Valid final CreateAttendanceDto attendance) {

        return attendanceService.createAttendance(attendance);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Updates existing attendance information",
            notes = "Updates the attendance record, posts attendance details back up to PNOMIS. IEP warnings are triggered when certain absence reasons are used.")
    public ResponseEntity<Object> putAttendance(@PathVariable("id") long id, @RequestBody @Valid final UpdateAttendanceDto attendance) {
        try {
            attendanceService.updateAttendance(id, attendance);
        } catch (AttendanceNotFound e) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{prison}/{event-location}")
    @ApiOperation(value = "Updates existing attendance information",
            response = AttendanceDto.class,
            notes = "Request attendance details")
    public Set<AttendanceDto> getAttendance(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") String prisonId,
                                            @ApiParam(value = "Location id of event") @PathVariable("event-location") Long eventLocationId,
                                            @ApiParam(value = "Date of event in format YYYY-MM-DD") @RequestParam(name = "date") @DateTimeFormat(iso = DATE) LocalDate date,
                                            @ApiParam(value = "Time period") @RequestParam(name = "period") TimePeriod period) {

        return attendanceService.getAttendance(prisonId, eventLocationId, date, period);

    }

    @GetMapping("/absence-reasons")
    public AbsentReasonsDto reasons() {
       return attendanceService.getAbsenceReasons();
    }
}
