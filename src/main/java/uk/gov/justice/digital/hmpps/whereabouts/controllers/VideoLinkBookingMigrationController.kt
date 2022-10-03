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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkBookingMigrationService

@Tag(name = "court")
@RestController
@RequestMapping(value = ["court"], produces = [MediaType.APPLICATION_JSON_VALUE])
class VideoLinkBookingMigrationController(
  private val videoLinkBookingMigrationService: VideoLinkBookingMigrationService
) {

  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/migrate-existing-bookings/{batchSize}"])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    summary = "Migrate booking",
    description = "Migrate missing booking data from Nomis to Whereabouts "
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "400", description = "Invalid request."),
      ApiResponse(
        responseCode = "404",
        description = "Requested resource not found.",
        content =
        [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ],

      ), ApiResponse(
        responseCode = "500",
        description = "Unrecoverable error occurred whilst processing request.",
        content =
        [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ],

      )
    ]
  )
  fun migrateExistingBooking(
    @Parameter(description = "Batch Size", required = true)
    @PathVariable("batchSize")
    batchSize: Int
  ): VideoLinkAppointmentMigrationResponse = videoLinkBookingMigrationService.migrateFromNomis(batchSize)
}
