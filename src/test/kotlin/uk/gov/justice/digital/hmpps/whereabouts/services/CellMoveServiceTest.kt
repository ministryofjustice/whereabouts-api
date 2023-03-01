package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
  private val telemetryClient: TelemetryClient = mock()
  private val clock = Clock.fixed(Instant.parse(OCCURRENCE_DATE_TIME + "Z"), ZoneId.of("UTC"))

  @Test
  fun `should make a call to prison api cell move method`() {
    stubPutCellMove()

    val service = CellMoveService(prisonApiService, caseNoteService, cellMoveRepository, telemetryClient, clock)

    val details = service.makeCellMove(
      cellMoveDetails = CellMoveDetails(
        bookingId = BOOKING_ID,
        offenderNo = OFFENDER_NO,
        internalLocationDescriptionDestination = ASSIGNED_LIVING_UNIT_DESC,
        cellMoveReasonCode = REASON_CODE,
        commentText = TEXT,
      ),
    )

    assertThat(details.agencyId).isEqualTo(AGENCY_ID)
    assertThat(details.assignedLivingUnitDesc).isEqualTo(ASSIGNED_LIVING_UNIT_DESC)
    assertThat(details.assignedLivingUnitId).isEqualTo(ASSIGNED_LIVING_UNIT_ID)
    assertThat(details.agencyId).isEqualTo(AGENCY_ID)

    verify(prisonApiService).putCellMove(BOOKING_ID, ASSIGNED_LIVING_UNIT_DESC, REASON_CODE)
  }

  @Test
  fun `should make a call to create a case note`() {
    stubPutCellMove()

    val service = CellMoveService(prisonApiService, caseNoteService, cellMoveRepository, telemetryClient, clock)

    service.makeCellMove(
      cellMoveDetails = CellMoveDetails(
        bookingId = BOOKING_ID,
        offenderNo = OFFENDER_NO,
        internalLocationDescriptionDestination = ASSIGNED_LIVING_UNIT_DESC,
        cellMoveReasonCode = REASON_CODE,
        commentText = TEXT,
      ),
    )

    verify(caseNoteService).postCaseNote(
      OFFENDER_NO,
      CASE_NOTE_TYPE,
      REASON_CODE,
      TEXT,
      LocalDateTime.parse(OCCURRENCE_DATE_TIME),
    )
  }

  @Test
  fun `should store the cell move history and case note ids`() {
    stubPutCellMove()

    val service = CellMoveService(prisonApiService, caseNoteService, cellMoveRepository, telemetryClient, clock)

    service.makeCellMove(
      cellMoveDetails = CellMoveDetails(
        bookingId = BOOKING_ID,
        offenderNo = OFFENDER_NO,
        internalLocationDescriptionDestination = ASSIGNED_LIVING_UNIT_DESC,
        cellMoveReasonCode = REASON_CODE,
        commentText = TEXT,
      ),
    )

    verify(cellMoveRepository).save(
      CellMoveReason(
        bookingId = BOOKING_ID,
        caseNoteId = CASE_NOTE_ID,
        bedAssignmentsSequence = BED_ASSIGNMENT_SEQUENCE,
      ),
    )
  }

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

    val service = CellMoveService(prisonApiService, caseNoteService, cellMoveRepository, telemetryClient, clock)

    val cellReasonDto = service.getCellMoveReason(BOOKING_ID, BED_ASSIGNMENT_SEQUENCE)

    assertThat(cellReasonDto.bookingId).isEqualTo(BOOKING_ID)
    assertThat(cellReasonDto.bedAssignmentsSequence).isEqualTo(BED_ASSIGNMENT_SEQUENCE)
    assertThat(cellReasonDto.caseNoteId).isEqualTo(CASE_NOTE_ID)
  }

  @Test
  fun `should raise a telemetry event`() {
    stubPutCellMove()

    val service = CellMoveService(prisonApiService, caseNoteService, cellMoveRepository, telemetryClient, clock)

    service.makeCellMove(
      cellMoveDetails = CellMoveDetails(
        bookingId = BOOKING_ID,
        offenderNo = OFFENDER_NO,
        internalLocationDescriptionDestination = ASSIGNED_LIVING_UNIT_DESC,
        cellMoveReasonCode = REASON_CODE,
        commentText = TEXT,
      ),
    )

    verify(telemetryClient).trackEvent(
      "CellMove",
      mapOf(
        "bookingId" to BOOKING_ID.toString(),
        "assignedLivingUnitDesc" to ASSIGNED_LIVING_UNIT_DESC,
        "assignedLivingUnitId" to ASSIGNED_LIVING_UNIT_ID.toString(),
        "cellMoveReasonCode" to REASON_CODE,
      ),
      null,
    )
  }

  private fun stubPutCellMove() {
    whenever(prisonApiService.putCellMove(anyLong(), anyString(), anyString()))
      .thenReturn(
        CellMoveResult(
          bookingId = BOOKING_ID,
          agencyId = AGENCY_ID,
          assignedLivingUnitId = ASSIGNED_LIVING_UNIT_ID,
          assignedLivingUnitDesc = ASSIGNED_LIVING_UNIT_DESC,
          bedAssignmentHistorySequence = BED_ASSIGNMENT_SEQUENCE,
          caseNoteId = CASE_NOTE_ID,
        ),
      )
    whenever(caseNoteService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any()))
      .thenReturn(CaseNoteDto.builder().caseNoteId(CASE_NOTE_ID).build())
  }

  companion object {
    private const val BOOKING_ID = -10L
    private const val AGENCY_ID = "MDI"
    private const val OFFENDER_NO = "A12345"
    private const val ASSIGNED_LIVING_UNIT_ID = 123L
    private const val ASSIGNED_LIVING_UNIT_DESC = "MDI-2-2-006"
    private const val CASE_NOTE_TYPE = "MOVED_CELL"
    private const val REASON_CODE = "ADM"
    private const val TEXT = "some text"
    private const val OCCURRENCE_DATE_TIME = "2020-10-03T20:00:00"
    private const val CASE_NOTE_ID = 1L
    private const val BED_ASSIGNMENT_SEQUENCE = 1
  }
}
