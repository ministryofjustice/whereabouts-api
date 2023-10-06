package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.ScheduledEventDto
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiServiceAuditable
import java.time.LocalDate

@Tag(name = "events")
@RestController
@RequestMapping(value = ["events"], produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyRole('MAINTAIN_WHEREABOUTS')")
class EventsController(
  private val prisonApiServiceAuditable: PrisonApiServiceAuditable,
) {

  @ApiResponses(
    ApiResponse(responseCode = "400", description = "Invalid request."),
    ApiResponse(responseCode = "404", description = "Requested resource not found."),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request."),
  )
  @Operation(
    description = "All scheduled events for offender.  This endpoint filters out cancelled events.",
    summary = "getEvents",
  )
  @GetMapping("/{offenderNo}")
  fun getEvents(
    @Parameter(example = "A1234AA", required = true)
    @PathVariable(value = "offenderNo", required = true)
    @NotNull
    offenderNo: String,
    @Parameter(description = "Returned events must be scheduled on or after this date (in YYYY-MM-DD format).  This date must be on or after today.")
    @RequestParam(value = "fromDate", required = false)
    @DateTimeFormat(iso = DATE)
    fromDate: LocalDate?,
    @Parameter(description = "Returned events must be scheduled on or before this date (in YYYY-MM-DD format).  This date must be on or after the fromDate.")
    @RequestParam(value = "toDate", required = false)
    @DateTimeFormat(iso = DATE)
    toDate: LocalDate?,
  ): List<ScheduledEventDto> = prisonApiServiceAuditable.getScheduledEvents(offenderNo, fromDate, toDate)
}
