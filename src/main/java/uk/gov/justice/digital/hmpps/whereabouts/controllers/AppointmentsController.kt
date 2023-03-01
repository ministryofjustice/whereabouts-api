package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentSearchDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.services.AppointmentService
import java.time.LocalDate

@Tag(name = "appointments")
@RestController
@RequestMapping(value = ["appointments"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AppointmentsController(
  private val appointmentService: AppointmentService,
) {

  @GetMapping("/{agencyId}")
  @Operation(
    description = "List of appointments for the given agency that match the search criteria.",
    summary = "getAppointments",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "OK"),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request.",
        content =
        [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],

      ),
      ApiResponse(
        responseCode = "500",
        description = "Unrecoverable error occurred whilst processing request.",
        content =
        [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],

      ),
    ],
  )
  fun getAppointments(
    @Parameter(description = "The agency Id")
    @PathVariable("agencyId")
    agencyId: String,
    @Parameter(
      description = "Date the appointments are scheduled",
      required = true,
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @RequestParam("date")
    date: LocalDate,
    @Parameter(description = "AM, PM or ED")
    @RequestParam(
      value = "timeSlot",
      required = false,
    )
    timeSlot: TimePeriod?,
    @Parameter(
      description = "The location prefix of any offenders' residence associated with a returned appointment",
      example = "Block A",
    )
    @RequestParam(value = "offenderLocationPrefix", required = false)
    offenderLocationPrefix: String?,
    @Parameter(description = "Location id")
    @RequestParam(value = "locationId", required = false)
    locationId: Long?,
  ): List<AppointmentSearchDto> =
    appointmentService.getAppointments(agencyId, date, timeSlot, offenderLocationPrefix, locationId)
}
