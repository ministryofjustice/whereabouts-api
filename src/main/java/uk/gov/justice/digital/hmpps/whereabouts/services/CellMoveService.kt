package uk.gov.justice.digital.hmpps.whereabouts.services

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveReasonDto
import uk.gov.justice.digital.hmpps.whereabouts.model.CellMoveReasonPK
import uk.gov.justice.digital.hmpps.whereabouts.repository.CellMoveReasonRepository

/**
 * Reads the frozen CELL_MOVE_REASON table. This service no longer makes cell moves - that moved to
 * hmpps-change-someones-cell-api, and the endpoint that did it was removed with MAPA-282 - so
 * nothing writes here any more. What is left serves the historic rows to the last consumer still
 * reading them, and goes when that consumer does.
 */
@Service
class CellMoveService(
  val cellMoveRepository: CellMoveReasonRepository,
) {

  fun getCellMoveReason(bookingId: Long, bedAssigmentSequence: Int): CellMoveReasonDto {
    val (_, _, caseNoteId) = cellMoveRepository.findById(CellMoveReasonPK(bookingId, bedAssigmentSequence))
      .orElseThrow {
        EntityNotFoundException("Cell move reason not found for booking id $bookingId and bed assignment sequence $bedAssigmentSequence")
      }

    return CellMoveReasonDto(bookingId, bedAssigmentSequence, caseNoteId)
  }
}
