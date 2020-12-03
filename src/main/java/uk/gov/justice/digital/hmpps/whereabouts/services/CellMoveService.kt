package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveReasonDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveResult
import uk.gov.justice.digital.hmpps.whereabouts.model.CellMoveReason
import uk.gov.justice.digital.hmpps.whereabouts.model.CellMoveReasonPK
import uk.gov.justice.digital.hmpps.whereabouts.repository.CellMoveReasonRepository
import java.time.Clock
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional

@Service
class CellMoveService(
  val prisonApiService: PrisonApiService,
  val caseNotesService: CaseNotesService,
  val cellMoveRepository: CellMoveReasonRepository,
  val telemetryClient: TelemetryClient,
  val clock: Clock
) {

  @Transactional
  fun makeCellMove(cellMoveDetails: CellMoveDetails): CellMoveResult {
    val occurrenceDateTime = LocalDateTime.now(clock)

    log.info("Making a cell move: {}", cellMoveDetails.copy(commentText = ""))

    val moveResult = prisonApiService.putCellMove(
      cellMoveDetails.bookingId,
      cellMoveDetails.internalLocationDescriptionDestination,
      cellMoveDetails.cellMoveReasonCode
    )

    log.info(
      "Creating a case note: offenderNo: {} type: {} subType: {}",
      cellMoveDetails.offenderNo,
      MOVE_CELL,
      cellMoveDetails.cellMoveReasonCode
    )

    val caseNoteDetails = caseNotesService.postCaseNote(
      cellMoveDetails.offenderNo,
      MOVE_CELL,
      cellMoveDetails.cellMoveReasonCode,
      cellMoveDetails.commentText,
      occurrenceDateTime
    )

    val cellMove = CellMoveReason(
      bookingId = moveResult.bookingId,
      bedAssignmentsSequence = moveResult.bedAssignmentHistorySequence,
      caseNoteId = caseNoteDetails.caseNoteId
    )

    log.info("Saving cell move details: {}", cellMove)

    cellMoveRepository.save(cellMove)

    telemetryClient.trackEvent(
      "CellMove",
      mapOf(
        "bookingId" to moveResult.bookingId.toString(),
        "assignedLivingUnitDesc" to moveResult.assignedLivingUnitDesc,
        "assignedLivingUnitId" to moveResult.assignedLivingUnitId.toString(),
        "cellMoveReasonCode" to cellMoveDetails.cellMoveReasonCode
      ),
      null
    )

    return moveResult.copy(caseNoteId = caseNoteDetails.caseNoteId)
  }

  fun getCellMoveReason(bookingId: Long, bedAssigmentSequence: Int): CellMoveReasonDto {
    val (_, _, caseNoteId) = cellMoveRepository.findById(CellMoveReasonPK(bookingId, bedAssigmentSequence))
      .orElseThrow {
        EntityNotFoundException("Cell move reason not found for booking id $bookingId and bed assignment sequence $bedAssigmentSequence")
      }

    return CellMoveReasonDto(bookingId, bedAssigmentSequence, caseNoteId)
  }

  companion object {
    private const val MOVE_CELL = "MOVED_CELL"
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
