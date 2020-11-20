package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestClientException
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveReasonDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveResult
import uk.gov.justice.digital.hmpps.whereabouts.services.CellMoveService
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserMdcFilter
import javax.persistence.EntityNotFoundException

@WebMvcTest(CellMoveController::class)
@Import(UserMdcFilter::class, StubUserSecurityUtilsConfig::class)
class CellControllerTest : TestController() {

  @MockBean
  lateinit var cellMoveService: CellMoveService

  @Test
  fun `returns a an unauthorized error when no valid login is present`() {
    mockMvc.perform(
      put("/cell/booking/$SOME_BOOKING_ID/living-unit/$SOME_ASSIGNED_LIVING_UNIT_ID")
    ).andDo(MockMvcResultHandlers.print())
      .andExpect(status().isUnauthorized)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `handle not found errors correctly`() {
    whenever(cellMoveService.makeCellMove(any())).thenThrow(EntityNotFoundException(SOME_ERROR_MESSAGE))

    mockMvc.perform(
      post("/cell/make-cell-move")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          getJsonBody(
            SOME_BOOKING_ID,
            SOME_OFFENDER_NO,
            SOME_ASSIGNED_LIVING_UNIT_DESC,
            SOME_REASON_CODE,
            SOME_COMMENT_TEXT
          )
        )
    ).andDo(MockMvcResultHandlers.print())
      .andExpect(status().isNotFound)
      .andExpect(jsonPath(".developerMessage").value(SOME_ERROR_MESSAGE))
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `handle server errors correctly`() {
    whenever(cellMoveService.makeCellMove(any())).thenThrow(RestClientException(SOME_ERROR_MESSAGE))

    mockMvc.perform(
      post("/cell/make-cell-move")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          getJsonBody(
            SOME_BOOKING_ID,
            SOME_OFFENDER_NO,
            SOME_ASSIGNED_LIVING_UNIT_DESC,
            SOME_REASON_CODE,
            SOME_COMMENT_TEXT
          )
        )
    ).andDo(MockMvcResultHandlers.print())
      .andExpect(status().is5xxServerError)
      .andExpect(jsonPath(".developerMessage").value(SOME_ERROR_MESSAGE))
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `return cll move details`() {
    whenever(cellMoveService.makeCellMove(any()))
      .thenReturn(
        CellMoveResult(
          bookingId = SOME_BOOKING_ID.toLong(),
          assignedLivingUnitDesc = SOME_ASSIGNED_LIVING_UNIT_DESC,
          agencyId = SOME_AGENCY_ID,
          assignedLivingUnitId = SOME_ASSIGNED_LIVING_UNIT_ID.toLong(),
          bedAssignmentHistorySequence = SOME_BED_ASSIGNMENT_ID,
          caseNoteId = SOME_CASE_NOTE_ID
        )
      )

    mockMvc.perform(
      post("/cell/make-cell-move")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
          getJsonBody(
            SOME_BOOKING_ID,
            SOME_OFFENDER_NO,
            SOME_ASSIGNED_LIVING_UNIT_DESC,
            SOME_REASON_CODE,
            SOME_COMMENT_TEXT
          )
        )
    )
      .andDo(MockMvcResultHandlers.print())
      .andExpect(status().isCreated())
      .andExpect(jsonPath(".bookingId").value(SOME_BOOKING_ID))
      .andExpect(jsonPath(".agencyId").value(SOME_AGENCY_ID))
      .andExpect(jsonPath(".assignedLivingUnitId").value(SOME_ASSIGNED_LIVING_UNIT_ID))
      .andExpect(jsonPath(".assignedLivingUnitDesc").value(SOME_ASSIGNED_LIVING_UNIT_DESC))
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should handle missing body`() {
    mockMvc.perform(
      post("/cell/make-cell-move")
        .contentType(MediaType.APPLICATION_JSON)
    ).andDo(MockMvcResultHandlers.print())
      .andExpect(status().isBadRequest)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should handle missing null body values`() {
    mockMvc.perform(
      post("/cell/make-cell-move")
        .contentType(MediaType.APPLICATION_JSON)
        .content(getJsonBody(null, null, null, null, null))
    ).andDo(MockMvcResultHandlers.print())
      .andExpect(status().isBadRequest)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `should return cell move reason`() {
    whenever(
      cellMoveService.getCellMoveReason(anyLong(), anyInt())
    ).thenReturn(CellMoveReasonDto(1L, 2, 3L))

    mockMvc.perform(
      get("/cell/cell-move-reason/booking/$SOME_BOOKING_ID/bed-assignment-sequence/$SOME_BED_ASSIGNMENT_SEQUENCE")
    )
      .andDo(MockMvcResultHandlers.print())
      .andExpect(status().isOk)
      .andExpect(jsonPath(".bookingId").value(1))
      .andExpect(jsonPath(".bedAssignmentsSequence").value(2))
      .andExpect(jsonPath(".caseNoteId").value(3))
  }

  private fun getJsonBody(
    bookingId: Int?,
    offenderNo: String?,
    destination: String?,
    cellMoveReason: String?,
    commentText: String?
  ) =
    gson.toJson(
      mapOf(
        "bookingId" to bookingId,
        "offenderNo" to offenderNo,
        "offenderNo" to offenderNo,
        "cellMoveReasonCode" to cellMoveReason,
        "internalLocationDescriptionDestination" to destination,
        "commentText" to commentText
      )
    )

  companion object {
    private const val SOME_BOOKING_ID = -10
    private const val SOME_OFFENDER_NO = "A12345"
    private const val SOME_AGENCY_ID = "MDI"
    private const val SOME_ASSIGNED_LIVING_UNIT_ID = 123
    private const val SOME_ASSIGNED_LIVING_UNIT_DESC = "MDI-2-2-006"
    private const val SOME_REASON_CODE = "ADM"
    private const val SOME_ERROR_MESSAGE = "some error message"
    private const val SOME_BED_ASSIGNMENT_ID = 1
    private const val SOME_CASE_NOTE_ID = 2L
    private const val SOME_BED_ASSIGNMENT_SEQUENCE = 3
    private const val SOME_COMMENT_TEXT = "some comment text"
  }
}
