package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.whereabouts.model.CellMoveReason
import uk.gov.justice.digital.hmpps.whereabouts.repository.CellMoveReasonRepository
import java.util.Optional

/**
 * Only the read is left: making a cell move moved to hmpps-change-someones-cell-api and the export
 * that fed the migration went with it (MAPA-282), taking the prison-api, case-notes and telemetry
 * collaborators this service used to need with them.
 */
class CellMoveServiceTest {

  private val cellMoveRepository: CellMoveReasonRepository = mock()

  @Test
  fun `should return cell reason dto`() {
    whenever(
      cellMoveRepository.findById(any()),
    ).thenReturn(
      Optional.of(
        CellMoveReason(
          bookingId = BOOKING_ID,
          bedAssignmentsSequence = BED_ASSIGNMENT_SEQUENCE,
          caseNoteId = CASE_NOTE_ID,
        ),
      ),
    )

    val service = CellMoveService(cellMoveRepository)

    val cellReasonDto = service.getCellMoveReason(BOOKING_ID, BED_ASSIGNMENT_SEQUENCE)

    assertThat(cellReasonDto.bookingId).isEqualTo(BOOKING_ID)
    assertThat(cellReasonDto.bedAssignmentsSequence).isEqualTo(BED_ASSIGNMENT_SEQUENCE)
    assertThat(cellReasonDto.caseNoteId).isEqualTo(CASE_NOTE_ID)
  }

  companion object {
    private const val BOOKING_ID = 1L
    private const val BED_ASSIGNMENT_SEQUENCE = 2
    private const val CASE_NOTE_ID = 3L
  }
}
