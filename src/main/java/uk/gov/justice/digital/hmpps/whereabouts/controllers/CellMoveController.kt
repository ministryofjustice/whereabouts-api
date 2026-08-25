package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.CellMoveReasonResponse
import uk.gov.justice.digital.hmpps.whereabouts.services.CellMoveService

@Tag(name = "cell")
@RestController
@RequestMapping(value = ["cell"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CellMoveController {

  @Autowired
  private lateinit var cellMoveService: CellMoveService

  @GetMapping("/cell-move-reason/booking/{bookingId}/bed-assignment-sequence/{bedAssignmentId}")
  @Operation(
    summary = "Return cell move reason (deprecated - the data here is frozen)",
    deprecated = true,
    description = """
      **Deprecated. Switch to hmpps-change-someones-cell-api as soon as possible.**

      Cell moves are no longer recorded by this service. Since the cell move UI switched to
      `hmpps-change-someones-cell-api`, nothing writes to this table any more, so **the data behind
      this endpoint is stale and will never grow**: any move made from that point onwards is not
      here and never will be. A lookup for a recent move returns 404, which is indistinguishable
      from a move that genuinely had no reason recorded.

      The replacement is `GET /cell-movements/{bookingId}/bed-assignment/{bedAssignmentSequence}`
      on hmpps-change-someones-cell-api (role `ROLE_CELL_MOVEMENTS__RO`). It serves both the
      historic reasons migrated out of this service and every new move, so it is a superset of what
      this endpoint can return - there is no window in which this endpoint is the better source.

      This endpoint and its table are deleted once the last consumer has moved across.
    """,
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
