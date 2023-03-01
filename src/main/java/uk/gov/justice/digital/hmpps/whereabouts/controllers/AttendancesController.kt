package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import lombok.extern.slf4j.Slf4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.ScheduledResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AbsencesResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceChangesResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceHistoryDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceSummary
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendancesDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendancesResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import java.time.LocalDate
import java.time.LocalDateTime

@Tag(name = "attendances")
@RestController
@RequestMapping(value = ["attendances"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Slf4j
class AttendancesController(private val attendanceService: AttendanceService) {

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Create new attendance records for multiple offenders (This endpoint does not trigger IEP warnings)",
    description = "Stores new attendance record for multiple offenders, posts attendance details back up to PNOMIS",
  )
  fun postAttendances(
    @Parameter(description = "Attendance parameters", required = true)
    @RequestBody
    @Valid
    attendances: AttendancesDto,
  ): AttendancesResponse = AttendancesResponse(
    attendances = attendanceService.createAttendances(attendances),
  )

  @GetMapping("/{prison}/{event-location}")
  @Operation(
    description = "Returns set of attendance details",
    summary = "Request attendance details",
  )
  fun getAttendanceForEventLocation(
    @Parameter(description = "Prison id (LEI)")
    @PathVariable(name = "prison")
    prisonId: String,
    @Parameter(description = "Location id of event")
    @PathVariable("event-location")
    eventLocationId: Long?,
    @Parameter(
      description = "Date of event in format YYYY-MM-DD",
      required = true,
    )
    @RequestParam(name = "date")
    @DateTimeFormat(iso = DATE)
    date: LocalDate,
    @Parameter(description = "Time period", required = true)
    @RequestParam(name = "period")
    period: TimePeriod,
  ): AttendancesResponse = AttendancesResponse(
    attendances = attendanceService.getAttendanceForEventLocation(prisonId, eventLocationId, date, period),
  )

  @GetMapping("/{prison}/absences")
  @Operation(
    description = "Returns set of attendance details for attendances with an absent reason",
    summary = "Request absences details",
  )
  fun getAbsences(
    @Parameter(description = "Prison id (LEI)")
    @PathVariable(name = "prison")
    prisonId: String,
    @Parameter(
      description = "Date of event in format YYYY-MM-DD",
      required = true,
    )
    @RequestParam(name = "date")
    @DateTimeFormat(iso = DATE)
    date: LocalDate,
    @Parameter(description = "Time period", required = true)
    @RequestParam(name = "period")
    period: TimePeriod,
  ): AttendancesResponse = AttendancesResponse(
    attendances = attendanceService.getAbsencesForReason(prisonId, date, period),
  )

  @GetMapping("/{prison}")
  @Operation(
    description = "Returns set of attendance details for set of booking ids",
    summary = "Request attendance details",
  )
  fun getAttendanceForBookings(
    @Parameter(description = "Prison id (LEI)")
    @PathVariable(name = "prison")
    prisonId: String,
    @Parameter(
      description = "Date of event in format YYYY-MM-DD",
      required = true,
    )
    @RequestParam(name = "date")
    @DateTimeFormat(iso = DATE)
    date: LocalDate,
    @Parameter(description = "Time period", required = true)
    @RequestParam(name = "period")
    period: TimePeriod,
    @Parameter(
      description = "Booking ids (bookings=1&bookings=2)",
      required = true,
    )
    @RequestParam(name = "bookings")
    bookings: Set<Long>,
  ): AttendancesResponse = AttendancesResponse(
    attendances = attendanceService.getAttendanceForBookings(prisonId, bookings, date, period),
  )

  @GetMapping("/offender/{offenderNo}/unacceptable-absences")
  @Operation(
    description = "Returns unacceptable absence attendance details for an offender",
    summary = "Request unacceptable absence details",
  )
  fun getAttendanceDetailsForOffender(
    @Parameter(description = "offender or Prison number or Noms id")
    @PathVariable(name = "offenderNo")
    offenderNo: String,
    @Parameter(description = "Start date of range to summarise in format YYYY-MM-DD", required = true)
    @RequestParam(name = "fromDate")
    @DateTimeFormat(iso = DATE)
    fromDate: LocalDate,
    @Parameter(description = "End date of range to summarise in format YYYY-MM-DD", required = true)
    @RequestParam(name = "toDate")
    @DateTimeFormat(iso = DATE)
    toDate: LocalDate,
    @PageableDefault(page = 0, size = 20) pageable: Pageable,
  ): Page<AttendanceHistoryDto> =
    attendanceService.getAttendanceDetailsForOffender(offenderNo, fromDate, toDate, pageable)

  @PostMapping("/{prison}")
  @Operation(
    description = "Returns set of attendance details for set of booking ids",
    summary = "Request attendance details",
  )
  fun getAttendanceForBookingsByPost(
    @Parameter(description = "Prison id (LEI)")
    @PathVariable(name = "prison")
    prisonId: String,
    @Parameter(
      description = "Date of event in format YYYY-MM-DD",
      required = true,
    )
    @RequestParam(name = "date")
    @DateTimeFormat(iso = DATE)
    date: LocalDate,
    @Parameter(description = "Time period", required = true)
    @RequestParam(name = "period")
    period: TimePeriod,
    @Parameter(description = "Set of booking ids, for example [1,2]", required = true) @RequestBody bookings: Set<Long>,
  ): AttendancesResponse = AttendancesResponse(
    attendances = attendanceService.getAttendanceForBookings(prisonId, bookings, date, period),
  )

  @PostMapping("/{prison}/attendance-over-date-range")
  @Operation(
    description = "Returns set of attendance details for set of booking ids",
    summary = "Request attendance details",
  )
  fun getAttendanceForBookingsOverDateRangeByPost(
    @Parameter(description = "Prison id (LEI)")
    @PathVariable(name = "prison")
    prisonId: String,
    @Parameter(
      description = "Date of event in format YYYY-MM-DD",
      required = true,
    )
    @RequestParam(name = "fromDate")
    @DateTimeFormat(iso = DATE)
    fromDate: LocalDate,
    @Parameter(description = "Date of event in format YYYY-MM-DD defaults to fromDate")
    @RequestParam(name = "toDate")
    @DateTimeFormat(
      iso = DATE,
    )
    toDate: LocalDate?,
    @Parameter(description = "Time period. Leave blank for AM + PM")
    @RequestParam(name = "period")
    period: TimePeriod?,
    @Parameter(description = "Set of booking ids, for example [1,2]", required = true) @RequestBody bookings: Set<Long>,
  ): AttendancesResponse = AttendancesResponse(
    attendances = attendanceService.getAttendanceForBookingsOverDateRange(
      prisonId,
      bookings,
      fromDate,
      toDate,
      period,
    ),
  )

  @GetMapping("/{prison}/unaccounted-for")
  @Operation(
    description = "Return a set of prisoners that haven't attended a scheduled activity",
    summary = "Request unaccounted for prisoners",
  )
  fun getPrisonersUnaccountedFor(
    @Parameter(description = "Prison id (LEI)")
    @PathVariable(name = "prison")
    prisonId: String,
    @Parameter(description = "Date of event in format YYYY-MM-DD", required = true)
    @RequestParam(name = "date")
    @DateTimeFormat(iso = DATE)
    date: LocalDate,
    @Parameter(description = "Time period", required = true)
    @RequestParam(name = "period")
    period: TimePeriod,
  ) = ScheduledResponse(attendanceService.getPrisonersUnaccountedFor(prisonId, date, period))

  @GetMapping("/{prison}/absences-for-scheduled-activities/{absentReason}")
  @Operation(
    description = "Return a set of absences for all offenders that have scheduled activity",
    summary = "Request absences",
  )
  fun getAbsencesForReason(
    @Parameter(description = "Prison id (LEI)")
    @PathVariable(name = "prison")
    prisonId: String,
    @Parameter(description = "Absent reason (e.g Refused, AcceptableAbsence)")
    @PathVariable(name = "absentReason")
    absentReason: AbsentReason,
    @Parameter(
      description = "Date of event in format YYYY-MM-DD",
      required = true,
    )
    @RequestParam(name = "fromDate")
    @DateTimeFormat(iso = DATE)
    fromDate: LocalDate,
    @Parameter(description = "Date of event in format YYYY-MM-DD defaults to fromDate")
    @RequestParam(name = "toDate")
    @DateTimeFormat(
      iso = DATE,
    )
    toDate: LocalDate?,
    @Parameter(description = "Time period")
    @RequestParam(name = "period")
    period: TimePeriod?,
  ): AbsencesResponse = AbsencesResponse(
    description = absentReason.labelWithAddedWarning,
    absences = attendanceService.getAbsencesForReason(prisonId, absentReason, fromDate, toDate, period),
  )

  @GetMapping("/changes")
  @Operation(description = "Return all changes relating to an attendance")
  fun getAttendanceChanges(
    @Parameter(
      description = "Date and Time of change in format YYYY-MM-DDT09:10",
      required = true,
    )
    @RequestParam(name = "fromDateTime")
    @DateTimeFormat(iso = DATE_TIME)
    fromDateTime: LocalDateTime,
    @Parameter(description = "Date and Time of the change in format YYYY-MM-DDT:09:45")
    @RequestParam(name = "toDateTime")
    @DateTimeFormat(
      iso = DATE_TIME,
    )
    toDateTime: LocalDateTime?,
  ): AttendanceChangesResponse = AttendanceChangesResponse(
    changes = attendanceService.getAttendanceChanges(fromDateTime, toDateTime),
  )

  @GetMapping("/offender/{offenderNo}/unacceptable-absence-count")
  @Operation(description = "Return counts of unacceptable absences and totals over time for an offender")
  fun getAttendanceSummary(
    @Parameter(description = "offender or Prison number or Noms id")
    @PathVariable(name = "offenderNo")
    offenderNo: String,
    @Parameter(description = "Start date of range to summarise in format YYYY-MM-DD", required = true)
    @RequestParam(name = "fromDate")
    @DateTimeFormat(iso = DATE)
    fromDate: LocalDate,
    @Parameter(description = "End date of range to summarise in format YYYY-MM-DD", required = true)
    @RequestParam(name = "toDate")
    @DateTimeFormat(iso = DATE)
    toDate: LocalDate,
  ): AttendanceSummary = attendanceService.getAttendanceAbsenceSummaryForOffender(offenderNo, fromDate, toDate)
}
