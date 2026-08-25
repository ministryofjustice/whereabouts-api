package uk.gov.justice.digital.hmpps.whereabouts.controllers

import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.whenever
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestClientException
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveReasonDto
import uk.gov.justice.digital.hmpps.whereabouts.services.CellMoveService

/**
 * Only the deprecated read survives on this controller - making a cell move moved to
 * hmpps-change-someones-cell-api, and the export that fed the migration went with it (MAPA-282).
 * The error-mapping cases below used to be driven through the cell move; they are kept, pointed at
 * the read, so ControllerAdvice's 404 and 500 mappings stay covered.
 */
@WebMvcTest(CellMoveController::class)
@ContextConfiguration(classes = [CellMoveController::class])
class CellControllerTest : TestController() {

  @MockitoBean
  lateinit var cellMoveService: CellMoveService

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `handle not found errors correctly`() {
    whenever(cellMoveService.getCellMoveReason(anyLong(), anyInt())).thenThrow(EntityNotFoundException(SOME_ERROR_MESSAGE))

    mockMvc.perform(
      get("/cell/cell-move-reason/booking/$SOME_BOOKING_ID/bed-assignment-sequence/$SOME_BED_ASSIGNMENT_SEQUENCE"),
    ).andDo(MockMvcResultHandlers.print())
      .andExpect(status().isNotFound)
      .andExpect(jsonPath(".developerMessage").value(SOME_ERROR_MESSAGE))
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `handle server errors correctly`() {
    whenever(cellMoveService.getCellMoveReason(anyLong(), anyInt())).thenThrow(RestClientException(SOME_ERROR_MESSAGE))

    mockMvc.perform(
      get("/cell/cell-move-reason/booking/$SOME_BOOKING_ID/bed-assignment-sequence/$SOME_BED_ASSIGNMENT_SEQUENCE"),
    ).andDo(MockMvcResultHandlers.print())
      .andExpect(status().is5xxServerError)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should return cell move reason`() {
    whenever(
      cellMoveService.getCellMoveReason(anyLong(), anyInt()),
    ).thenReturn(CellMoveReasonDto(1L, 2, 3L))

    mockMvc.perform(
      get("/cell/cell-move-reason/booking/$SOME_BOOKING_ID/bed-assignment-sequence/$SOME_BED_ASSIGNMENT_SEQUENCE"),
    )
      .andDo(MockMvcResultHandlers.print())
      .andExpect(status().isOk)
      .andExpect(jsonPath(".bookingId").value(1))
      .andExpect(jsonPath(".bedAssignmentsSequence").value(2))
      .andExpect(jsonPath(".caseNoteId").value(3))
  }

  companion object {
    private const val SOME_BOOKING_ID = -10
    private const val SOME_ERROR_MESSAGE = "some error message"
    private const val SOME_BED_ASSIGNMENT_SEQUENCE = 3
  }
}
