package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveResult
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.CaseNoteDto
import uk.gov.justice.digital.hmpps.whereabouts.model.CellMoveReason
import uk.gov.justice.digital.hmpps.whereabouts.repository.CellMoveReasonRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional

class CellMoveServiceTest {

  private val prisonApiService: PrisonApiService = mock()
  private val caseNoteService: CaseNotesService = mock()
  private val cellMoveRepository: CellMoveReasonRepository = mock()
  private val clock = Clock.fixed(Instant.parse(SOME_OCCURRENCE_DATE_TIME + "Z"), ZoneId.of("UTC"))

  @Test
  fun `should make a call to prison api cell move method`() {
    stubPutCellMove()

    val service = CellMoveService(prisonApiService, caseNoteService, cellMoveRepository, clock)

    val details = service.makeCellMove(
      cellMoveDetails = CellMoveDetails(
        bookingId = SOME_BOOKING_ID,
        offenderNo = SOME_OFFENDER_NO,
        internalLocationDescriptionDestination = SOME_ASSIGNED_LIVING_UNIT_DESC,
        cellMoveReasonCode = SOME_REASON_CODE,
        commentText = SOME_TEXT
      )
    )

    assertThat(details.agencyId).isEqualTo(SOME_AGENCY_ID)
    assertThat(details.assignedLivingUnitDesc).isEqualTo(SOME_ASSIGNED_LIVING_UNIT_DESC)
    assertThat(details.assignedLivingUnitId).isEqualTo(SOME_ASSIGNED_LIVING_UNIT_ID)
    assertThat(details.agencyId).isEqualTo(SOME_AGENCY_ID)

    verify(prisonApiService).putCellMove(SOME_BOOKING_ID, SOME_ASSIGNED_LIVING_UNIT_DESC, SOME_REASON_CODE)
  }

  @Test
  fun `should make a call to create a case note`() {
    stubPutCellMove()

    val service = CellMoveService(prisonApiService, caseNoteService, cellMoveRepository, clock)

    service.makeCellMove(
      cellMoveDetails = CellMoveDetails(
        bookingId = SOME_BOOKING_ID,
        offenderNo = SOME_OFFENDER_NO,
        internalLocationDescriptionDestination = SOME_ASSIGNED_LIVING_UNIT_DESC,
        cellMoveReasonCode = SOME_REASON_CODE,
        commentText = SOME_TEXT
      )
    )

    verify(caseNoteService).postCaseNote(
      SOME_OFFENDER_NO,
      SOME_CASE_NOTE_TYPE,
      SOME_REASON_CODE,
      SOME_TEXT,
      LocalDateTime.parse(SOME_OCCURRENCE_DATE_TIME)
    )
  }

  @Test
  fun `should store the cell move history and case note ids`() {
    stubPutCellMove()

    val service = CellMoveService(prisonApiService, caseNoteService, cellMoveRepository, clock)

    service.makeCellMove(
      cellMoveDetails = CellMoveDetails(
        bookingId = SOME_BOOKING_ID,
        offenderNo = SOME_OFFENDER_NO,
        internalLocationDescriptionDestination = SOME_ASSIGNED_LIVING_UNIT_DESC,
        cellMoveReasonCode = SOME_REASON_CODE,
        commentText = SOME_TEXT
      )
    )

    verify(cellMoveRepository).save(
      CellMoveReason(
        bookingId = SOME_BOOKING_ID,
        caseNoteId = SOME_CASE_NOTE_ID,
        bedAssignmentsSequence = SOME_BED_ASSIGNMENT_SEQUENCE
      )
    )
  }

  @Test
  fun `should return cell reason dto`() {
    whenever(
      cellMoveRepository.findById(any())
    ).thenReturn(
      Optional.of(
        CellMoveReason(
          bookingId = SOME_BOOKING_ID,
          bedAssignmentsSequence = SOME_BED_ASSIGNMENT_SEQUENCE,
          caseNoteId = SOME_CASE_NOTE_ID
        )
      )
    )

    val service = CellMoveService(prisonApiService, caseNoteService, cellMoveRepository, clock)

    val cellReasonDto = service.getCellMoveReason(SOME_BOOKING_ID, SOME_BED_ASSIGNMENT_SEQUENCE)

    assertThat(cellReasonDto.bookingId).isEqualTo(SOME_BOOKING_ID)
    assertThat(cellReasonDto.bedAssignmentsSequence).isEqualTo(SOME_BED_ASSIGNMENT_SEQUENCE)
    assertThat(cellReasonDto.caseNoteId).isEqualTo(SOME_CASE_NOTE_ID)
  }

  private fun stubPutCellMove() {
    whenever(prisonApiService.putCellMove(anyLong(), anyString(), anyString()))
      .thenReturn(
        CellMoveResult(
          bookingId = SOME_BOOKING_ID,
          agencyId = SOME_AGENCY_ID,
          assignedLivingUnitId = SOME_ASSIGNED_LIVING_UNIT_ID,
          assignedLivingUnitDesc = SOME_ASSIGNED_LIVING_UNIT_DESC,
          bedAssignmentHistorySequence = SOME_BED_ASSIGNMENT_SEQUENCE,
          caseNoteId = SOME_CASE_NOTE_ID
        )
      )
    whenever(caseNoteService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any()))
      .thenReturn(CaseNoteDto.builder().caseNoteId(SOME_CASE_NOTE_ID).build())
  }

  companion object {
    private const val SOME_BOOKING_ID = -10L
    private const val SOME_AGENCY_ID = "MDI"
    private const val SOME_OFFENDER_NO = "A12345"
    private const val SOME_ASSIGNED_LIVING_UNIT_ID = 123L
    private const val SOME_ASSIGNED_LIVING_UNIT_DESC = "MDI-2-2-006"
    private const val SOME_CASE_NOTE_TYPE = "MOVED_CELL"
    private const val SOME_REASON_CODE = "ADM"
    private const val SOME_TEXT = "some text"
    private const val SOME_OCCURRENCE_DATE_TIME = "2020-10-03T20:00:00"
    private const val SOME_CASE_NOTE_ID = 1L
    private const val SOME_BED_ASSIGNMENT_SEQUENCE = 1
  }
}
