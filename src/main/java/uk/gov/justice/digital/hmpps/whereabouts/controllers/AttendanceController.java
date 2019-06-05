package uk.gov.justice.digital.hmpps.whereabouts.controllers;

import io.swagger.annotations.Api;
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
    public void postAttendance(
            @ApiParam(value = "New attendance details." , required= true )
            @RequestBody
            @Valid final CreateAttendanceDto attendance) {

        this.attendanceService.createAttendance(attendance);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> putAttendance(@PathVariable("id") long id, @RequestBody @Valid final UpdateAttendanceDto attendance) {
        try {
            this.attendanceService.updateAttendance(id, attendance);
        } catch (AttendanceNotFound e) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(null);
    }

    @GetMapping("/{prison}/{event-location}")
    public Set<AttendanceDto> getAttendance(@PathVariable("prison") String prisonId,
                                             @PathVariable("event-location") Long eventLocationId,
                                             @RequestParam @DateTimeFormat(iso= DATE) LocalDate date,
                                             @RequestParam String period) {

       return this.attendanceService.getAttendance(prisonId, eventLocationId, date, TimePeriod.valueOf(period));

    }

    @GetMapping("/absence-reasons")
    public AbsentReasonsDto reasons() {
       return attendanceService.getAbsenceReasons();
    }
}
