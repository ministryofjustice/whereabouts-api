package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDetailsDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreatedAppointmentDetailsDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.services.AppointmentService

@Tag(name = "appointment")
@RestController
@RequestMapping(value = ["appointment"], produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyRole('MAINTAIN_WHEREABOUTS')")
class AppointmentController(private val appointmentService: AppointmentService) {

  @GetMapping(path = ["/{id}"])
  @Operation(
    description = "Return appointment details",
    summary = "getAppointment",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "OK"),
      ApiResponse(
        responseCode = "404",
        description = "Appointment not found.",
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
  fun getAppointment(@PathVariable("id") id: Long): AppointmentDetailsDto = appointmentService.getAppointment(id)

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    description = "Create an appointment",
    summary = "createAppointment",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "400",
        description = "Bad request",
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
  fun createAppointment(@RequestBody createAppointmentSpecification: CreateAppointmentSpecification): List<CreatedAppointmentDetailsDto> =
    appointmentService.createAppointment(createAppointmentSpecification)

  @DeleteMapping(path = ["/{id}"])
  @Operation(
    description = "Delete an appointment",
    summary = "deleteAppointment",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "404",
        description = "Appointment not found.",
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
  fun deleteAppointment(
    @PathVariable(value = "id") id: Long,
  ) = appointmentService.deleteAppointment(id)

  @DeleteMapping(path = ["/recurring/{id}"])
  @Operation(
    description = "Delete the whole sequence of a recurring appointment",
    summary = "deleteRecurringAppointmentSequence",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "404",
        description = "Recurring appointment sequence not found.",
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
  fun deleteRecurringAppointmentSequence(
    @Parameter(description = "The id of the recurring appointment sequence.")
    @PathVariable(value = "id")
    id: Long,
  ) = appointmentService.deleteRecurringAppointmentSequence(id)
}
