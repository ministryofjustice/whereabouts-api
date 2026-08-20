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
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveReasonDto
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
    @RequestParam(name = "lockTimeout", required = false, defaultValue = "false") lockTimeout: Boolean,
    @RequestBody @Valid
    cellMoveDetails: CellMoveDetails,
  ): CellMoveResponse = CellMoveResponse(cellMoveResult = cellMoveService.makeCellMove(cellMoveDetails, lockTimeout))

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

  @GetMapping("/cell-move-reasons")
  @PreAuthorize("hasRole('ROLE_CELL_MOVEMENTS__SYNC__RW')")
  @Operation(
    description = "Export a page of cell move reasons, in (bookingId, bedAssignmentSequence) order. " +
      "Exists solely so hmpps-change-someones-cell-api can copy CELL_MOVE_REASON across ahead of this " +
      "service's decommission - it takes over the cell move and serves this data from its own tables. " +
      "Walk the table by passing the last key of the previous page; an empty page means the export is " +
      "complete. Requires role ROLE_CELL_MOVEMENTS__SYNC__RW.",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "401",
        description = "Missing the ROLE_CELL_MOVEMENTS__SYNC__RW role. (This service's error handling maps access denied to 401 rather than 403.)",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "500",
        description = "Unrecoverable error occurred whilst processing request.",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getCellMoveReasons(
    @RequestParam(name = "lastBookingId", required = false, defaultValue = "0") lastBookingId: Long,
    @RequestParam(name = "lastBedAssignmentSequence", required = false, defaultValue = "0") lastBedAssignmentSequence: Int,
    @RequestParam(name = "pageSize", required = false, defaultValue = "1000") pageSize: Int,
  ): CellMoveReasonsExportResponse = CellMoveReasonsExportResponse(
    // Clamped rather than rejected: this service's error handling has no mapping for parameter
    // constraint violations, and an export caller asking for too much simply gets the cap.
    cellMoveReasons = cellMoveService.getCellMoveReasons(lastBookingId, lastBedAssignmentSequence, pageSize.coerceIn(1, 1000)),
  )
}

data class CellMoveReasonsExportResponse(
  val cellMoveReasons: List<CellMoveReasonDto>,
)
