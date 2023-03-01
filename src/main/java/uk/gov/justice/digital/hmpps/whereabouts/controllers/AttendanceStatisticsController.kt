package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.extern.slf4j.Slf4j
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceStatistics
import uk.gov.justice.digital.hmpps.whereabouts.services.Stats
import java.time.LocalDate

@Tag(name = "attendance-statistics")
@RestController
@RequestMapping(value = ["attendance-statistics"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Slf4j
class AttendanceStatisticsController(private val attendanceStatistics: AttendanceStatistics) {

  @GetMapping("{prison}/over-date-range")
  @Operation(
    description = "Request attendance statistics",
    summary = "Request attendance statistics"
  )
  fun getAttendanceForEventLocation(
    @Parameter(description = "Prison id (LEI)")
    @PathVariable(name = "prison")
    prisonId: String,
    @Parameter(description = "Time period. Leave blank for AM + PM")
    @RequestParam(name = "period")
    period: TimePeriod?,
    @Parameter(
      description = "From date of event in format YYYY-MM-DD",
      required = true
    )
    @RequestParam(name = "fromDate")
    @DateTimeFormat(iso = DATE)
    fromDate: LocalDate,
    @Parameter(
      description = "To date of event in format YYYY-MM-DD",
      required = true
    )
    @RequestParam(name = "toDate")
    @DateTimeFormat(iso = DATE)
    toDate: LocalDate
  ): Stats = attendanceStatistics.getStats(prisonId, period, fromDate, toDate)
}
