package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.CellMoveReasonResponse
import uk.gov.justice.digital.hmpps.whereabouts.services.CellMoveService

@Tag(name = "cell")
@RestController
@RequestMapping(value = ["cell"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CellMoveController {

  @Autowired
  private lateinit var cellMoveService: CellMoveService

  @PostMapping("/make-cell-move")
  @Operation(description = "Make a cell move for an offender. Triggers the creation of a MOVED_CELL case note.")
  @ResponseStatus(HttpStatus.CREATED)
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "400", description = "Invalid request."), ApiResponse(
        responseCode = "404",
        description = "Requested resource not found.",
        content =
        [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],

      ), ApiResponse(
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
  fun makeCellMove(
    @RequestBody @Valid
    cellMoveDetails: CellMoveDetails,
  ): CellMoveResponse =
    CellMoveResponse(cellMoveResult = cellMoveService.makeCellMove(cellMoveDetails))

  @GetMapping("/cell-move-reason/booking/{bookingId}/bed-assignment-sequence/{bedAssignmentId}")
  @Operation(description = "Return cell move reason")
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
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],

      ), ApiResponse(
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
  fun getCellMoveReason(
    @PathVariable(name = "bookingId") bookingId: Long,
    @PathVariable(name = "bedAssignmentId") bedAssignmentId: Int,
  ): CellMoveReasonResponse {
    val cellMoveReason = cellMoveService.getCellMoveReason(bookingId, bedAssignmentId)
    return CellMoveReasonResponse(cellMoveReason = cellMoveReason)
  }
}
