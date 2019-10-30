package uk.gov.justice.digital.hmpps.whereabouts.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.hmpps.whereabouts.dto.*;
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceLocked;
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceNotFound;
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService;

import javax.validation.Valid;

@Api(tags = {"attendance"})
@RestController()
@RequestMapping(
        value = "attendance",
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
}
