package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveResult

class CellMoveServiceTest {

  private val prisonApiService: PrisonApiService = mock()

  @Test
  fun `should make a call to prison api cell move method`() {

    whenever(prisonApiService.putCellMove(anyLong(), anyString(), anyString()))
      .thenReturn(
        CellMoveResult(
          bookingId = SOME_BOOKING_ID,
          agencyId = SOME_AGENCY_ID,
          assignedLivingUnitId = SOME_ASSIGNED_LIVING_UNIT_ID,
          assignedLivingUnitDesc = SOME_ASSIGNED_LIVING_UNIT_DESC
        )
      )

    val service = CellMoveService(prisonApiService)

    val details = service.makeCellMove(cellMoveDetails = CellMoveDetails(SOME_BOOKING_ID, SOME_ASSIGNED_LIVING_UNIT_DESC, SOME_REASON_CODE))

    assertThat(details.agencyId).isEqualTo(SOME_AGENCY_ID)
    assertThat(details.assignedLivingUnitDesc).isEqualTo(SOME_ASSIGNED_LIVING_UNIT_DESC)
    assertThat(details.assignedLivingUnitId).isEqualTo(SOME_ASSIGNED_LIVING_UNIT_ID)
    assertThat(details.agencyId).isEqualTo(SOME_AGENCY_ID)

    verify(prisonApiService).putCellMove(SOME_BOOKING_ID, SOME_ASSIGNED_LIVING_UNIT_DESC, SOME_REASON_CODE)
  }

  companion object {
    private const val SOME_BOOKING_ID = -10L
    private const val SOME_AGENCY_ID = "MDI"
    private const val SOME_ASSIGNED_LIVING_UNIT_ID = 123L
    private const val SOME_ASSIGNED_LIVING_UNIT_DESC = "MDI-2-2-006"
    private const val SOME_REASON_CODE = "ADM"
  }
}
