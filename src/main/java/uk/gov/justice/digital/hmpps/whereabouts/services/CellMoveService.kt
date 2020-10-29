package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveResult

@Service
class CellMoveService(val prisonApiService: PrisonApiService) {
  fun makeCellMove(cellMoveDetails: CellMoveDetails): CellMoveResult {
    return prisonApiService.putCellMove(
      cellMoveDetails.bookingId,
      cellMoveDetails.internalLocationDescriptionDestination,
      cellMoveDetails.cellMoveReasonCode
    )
  }
}
