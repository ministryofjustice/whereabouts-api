package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import lombok.extern.slf4j.Slf4j
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.whereabouts.dto.AbsencesResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceChangesResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendancesDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendancesResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.Valid

@Api(tags = ["attendances"])
@RestController
@RequestMapping(value = ["attendances"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Slf4j
class AttendancesController(private val attendanceService: AttendanceService) {

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "Create new attendance records for multiple offenders (This endpoint does not trigger IEP warnings)", response = AttendancesResponse::class, notes = "Stores new attendance record for multiple offenders, posts attendance details back up to PNOMIS")
  fun postAttendances(
      @ApiParam(value = "Attendance parameters", required = true)
      @RequestBody
      @Valid
      attendances: AttendancesDto): AttendancesResponse {

    return AttendancesResponse(
        attendances = attendanceService.createAttendances(attendances)
    )
  }

  @GetMapping("/{prison}/{event-location}")
  @ApiOperation(value = "Returns set of attendance details", response = AttendancesResponse::class, notes = "Request attendance details")
  fun getAttendanceForEventLocation(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") prisonId: String,
                                    @ApiParam(value = "Location id of event") @PathVariable("event-location") eventLocationId: Long?,
                                    @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) date: LocalDate,
                                    @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") period: TimePeriod): AttendancesResponse {

    return AttendancesResponse(
        attendances = attendanceService.getAttendanceForEventLocation(prisonId, eventLocationId, date, period)
    )
  }

  @GetMapping("/{prison}/absences")
  @ApiOperation(value = "Returns set of attendance details for attendances with an absent reason", response = AttendancesResponse::class, notes = "Request absences details")
  fun getAbsences(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") prisonId: String,
                  @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) date: LocalDate,
                  @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") period: TimePeriod): AttendancesResponse {

    return AttendancesResponse(
        attendances = attendanceService.getAbsencesForReason(prisonId, date, period)
    )
  }

  @GetMapping("/{prison}")
  @ApiOperation(value = "Returns set of attendance details for set of booking ids", response = AttendancesResponse::class, notes = "Request attendance details")
  fun getAttendanceForBookings(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") prisonId: String,
                               @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) date: LocalDate,
                               @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") period: TimePeriod,
                               @ApiParam(value = "Booking ids (bookings=1&bookings=2)", required = true) @RequestParam(name = "bookings") bookings: Set<Long>): AttendancesResponse {

    return AttendancesResponse(
        attendances = attendanceService.getAttendanceForBookings(prisonId, bookings, date, period)
    )
  }

  @PostMapping("/{prison}")
  @ApiOperation(value = "Returns set of attendance details for set of booking ids", response = AttendancesResponse::class, notes = "Request attendance details")
  fun getAttendanceForBookingsByPost(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") prisonId: String,
                                     @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) date: LocalDate,
                                     @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") period: TimePeriod,
                                     @ApiParam(value = "Booking ids (bookings=1&bookings=2)", required = true) @RequestBody bookings: Set<Long>): AttendancesResponse {

    return AttendancesResponse(
        attendances = attendanceService.getAttendanceForBookings(prisonId, bookings, date, period)
    )
  }

  @PostMapping("/{prison}/attendance-over-date-range")
  @ApiOperation(value = "Returns set of attendance details for set of booking ids", response = AttendancesResponse::class, notes = "Request attendance details")
  fun getAttendanceForBookingsOverDateRangeByPost(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") prisonId: String,
                                                  @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "fromDate") @DateTimeFormat(iso = DATE) fromDate: LocalDate,
                                                  @ApiParam(value = "Date of event in format YYYY-MM-DD defaults to fromDate") @RequestParam(name = "toDate") @DateTimeFormat(iso = DATE) toDate: LocalDate?,
                                                  @ApiParam(value = "Time period. Leave blank for AM + PM") @RequestParam(name = "period") period: TimePeriod?,
                                                  @ApiParam(value = "Booking ids (bookings=1&bookings=2)", required = true) @RequestBody bookings: Set<Long>): AttendancesResponse {

    return AttendancesResponse(
            attendances = attendanceService.getAttendanceForBookingsOverDateRange(prisonId, bookings, fromDate, toDate, period)
    )
  }

  @GetMapping("/{prison}/attendance-for-scheduled-activities")
  @ApiOperation(value = "Return a set of attendance details for all offenders that have scheduled activity", response = AttendancesResponse::class, notes = "Request attendance details")
  fun getAttendanceForOffendersThatHaveScheduleActivity(@ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") prisonId: String,
                                                        @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "date") @DateTimeFormat(iso = DATE) date: LocalDate,
                                                        @ApiParam(value = "Time period", required = true) @RequestParam(name = "period") period: TimePeriod): AttendancesResponse {

    return AttendancesResponse(
        attendances = attendanceService.getAttendanceForOffendersThatHaveScheduledActivity(prisonId, date, period)
    )
  }

  @GetMapping("/{prison}/absences-for-scheduled-activities/{absentReason}")
  @ApiOperation(value = "Return a set of absences for all offenders that have scheduled activity", response = AbsencesResponse::class, notes = "Request absences")
  fun getAbsencesForReason(
      @ApiParam(value = "Prison id (LEI)") @PathVariable(name = "prison") prisonId: String,
      @ApiParam(value = "Absent reason (e.g Refused, AcceptableAbsence)") @PathVariable(name = "absentReason") absentReason: AbsentReason,
      @ApiParam(value = "Date of event in format YYYY-MM-DD", required = true) @RequestParam(name = "fromDate") @DateTimeFormat(iso = DATE) fromDate: LocalDate,
      @ApiParam(value = "Date of event in format YYYY-MM-DD defaults to fromDate") @RequestParam(name = "toDate") @DateTimeFormat(iso = DATE) toDate: LocalDate?,
      @ApiParam(value = "Time period") @RequestParam(name = "period") period: TimePeriod?
  ): AbsencesResponse = AbsencesResponse(
      absences = attendanceService.getAbsencesForReason(prisonId, absentReason, fromDate, toDate, period)
  )

  @GetMapping("/changes")
  @ApiOperation(value = "Return all changes relating to an attendance")
  fun getAttendanceChanges(
      @ApiParam(value = "Date and Time of change in format YYYY-MM-DDT09:10", required = true) @RequestParam(name = "fromDateTime") @DateTimeFormat(iso = DATE_TIME) fromDateTime: LocalDateTime,
      @ApiParam(value = "Date and Time of the change in format YYYY-MM-DDT:09:45") @RequestParam(name = "toDateTime") @DateTimeFormat(iso = DATE_TIME) toDateTime: LocalDateTime?
  ): AttendanceChangesResponse {
    return AttendanceChangesResponse(
        changes = attendanceService.getAttendanceChanges(fromDateTime, toDateTime)
    )
  }
}
