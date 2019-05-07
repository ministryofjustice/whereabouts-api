package uk.gov.justice.digital.hmpps.whereabouts.controllers;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AbsentReasonDto;
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Api(tags = {"attendance"})
@RestController()
@RequestMapping(
        value="attendance",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class OffenderAttendanceController {

    private AttendanceService attendanceService;

    public OffenderAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void updateAttendance(@RequestBody @Valid AttendanceDto attendance) {
        this.attendanceService.updateOffenderAttendance(attendance);
    }

    @GetMapping("/{prison}/{event-location}")
    public Set<AttendanceDto> getAttendance(@PathVariable("prison") String prisonId,
                                             @PathVariable("event-location") Long eventLocationId,
                                             @RequestParam @DateTimeFormat(iso= DATE) LocalDate date,
                                             @RequestParam String period) {

       return this.attendanceService.getAttendance(prisonId, eventLocationId, date, TimePeriod.valueOf(period));

    }

    @GetMapping("/absence-reasons")
    public List<AbsentReasonDto> reasons() {
        return this.attendanceService.getAbsentReasons();
    }
}
