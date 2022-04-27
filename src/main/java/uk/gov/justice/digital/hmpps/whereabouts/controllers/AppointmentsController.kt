package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentSearchDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.services.AppointmentService
import java.time.LocalDate

@Tag(name = "appointments")
@RestController
@RequestMapping(value = ["appointments"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AppointmentsController(
  private val appointmentService: AppointmentService
) {

  @GetMapping("/{agencyId}")
  @Operation(
    description = "List of appointments for the given agency that match the search criteria.",
    summary = "getAppointments"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = AppointmentDto::class, responseContainer = "List"),
      ApiResponse(
        code = 400,
        message = "Invalid request.",
        response = ErrorResponse::class,
        responseContainer = "List"
      ),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class,
        responseContainer = "List"
      )
    ]
  )
  fun getAppointments(
    @Parameter(name = "The agency Id") @PathVariable("agencyId") agencyId: String,
    @Parameter(
      name = "Date the appointments are scheduled",
      required = true
    ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("date") date: LocalDate,
    @Parameter(name = "AM, PM or ED") @RequestParam(
      value = "timeSlot",
      required = false
    ) timeSlot: TimePeriod?,
    @Parameter(
      name = "The location prefix of any offenders' residence associated with a returned appointment",
      example = "Block A"
    ) @RequestParam(value = "offenderLocationPrefix", required = false) offenderLocationPrefix: String?,
    @Parameter(name = "Location id") @RequestParam(value = "locationId", required = false) locationId: Long?
  ): List<AppointmentSearchDto> =
    appointmentService.getAppointments(agencyId, date, timeSlot, offenderLocationPrefix, locationId)
}
