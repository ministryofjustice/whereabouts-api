package uk.gov.justice.digital.hmpps.whereabouts.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod;
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceStatistics;
import uk.gov.justice.digital.hmpps.whereabouts.services.Stats;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Api(tags = {"attendance-statistics"})
@RestController()
@RequestMapping(
        value = "attendance-statistics",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class AttendanceStatisticsController {

    private final AttendanceStatistics attendanceStatistics;

    public AttendanceStatisticsController(final AttendanceStatistics attendanceStatistics) {
        this.attendanceStatistics = attendanceStatistics;
    }

    @GetMapping("{prison}/over-date-range")
    @ApiOperation(value = "Request attendance statistics",
            response = Stats.class,
            notes = "Request attendance statistics")
    public Stats getAttendanceForEventLocation(
            @ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") String prisonId,
            @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") TimePeriod period,
            @ApiParam(value = "From date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "fromDate") @DateTimeFormat(iso = DATE) LocalDate fromDate,
            @ApiParam(value = "To date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "toDate") @DateTimeFormat(iso = DATE) LocalDate toDate) {

        return attendanceStatistics.getStats(prisonId, period, fromDate, toDate);

    }
}
