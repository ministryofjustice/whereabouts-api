package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.ScheduledEventDto
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiServiceAuditable
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Api(tags = ["events"])
@RestController
@RequestMapping(value = ["events"], produces = [MediaType.APPLICATION_JSON_VALUE])
class EventsController(
  private val prisonApiServiceAuditable: PrisonApiServiceAuditable,
) {

  @ApiResponses(
    ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class),
    ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse::class),
    ApiResponse(
      code = 500,
      message = "Unrecoverable error occurred whilst processing request.",
      response = ErrorResponse::class,
    )
  )
  @ApiOperation(
    value = "All scheduled events for offender.",
    notes = "All scheduled events for offender.",
    nickname = "getEvents"
  )
  @GetMapping("/{offenderNo}")
  fun getEvents(
    @ApiParam(
      name = "offenderNo",
      value = "Offender No",
      example = "A1234AA",
      required = true,
    ) @PathVariable(value = "offenderNo", required = true) @NotNull offenderNo: String,
    @ApiParam("Returned events must be scheduled on or after this date (in YYYY-MM-DD format).")
    @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DATE) fromDate: LocalDate?,
    @ApiParam("Returned events must be scheduled on or before this date (in YYYY-MM-DD format).")
    @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DATE) toDate: LocalDate?
  ): List<ScheduledEventDto> = prisonApiServiceAuditable.getEvents(offenderNo, fromDate, toDate)
}
