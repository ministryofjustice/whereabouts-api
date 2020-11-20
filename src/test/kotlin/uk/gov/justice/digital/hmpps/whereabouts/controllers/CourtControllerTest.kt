package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.whereabouts.services.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserMdcFilter

@WebMvcTest(CourtController::class)
@Import(UserMdcFilter::class, StubUserSecurityUtilsConfig::class)
class CourtControllerTest : TestController() {
  @MockBean
  lateinit var courtService: CourtService

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `Accept a valid request, main appointment only`() {
    passWithJson(
      gson.toJson(
        mapOf(
          "bookingId" to 1,
          "court" to "Test Court 1",
          "main" to mapOf(
            "locationId" to 2,
            "startTime" to "2020-12-01T09:00",
            "endTime" to "2020-12-01T09:30"
          )
        )
      )
    )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `Accept a valid request, pre, main and post`() {
    passWithJson(
      gson.toJson(
        mapOf(
          "bookingId" to 1,
          "court" to "Test Court 1",
          "comment" to "Comment",
          "madeByTheCourt" to false,
          "pre" to mapOf(
            "locationId" to 1,
            "startTime" to "2020-12-01T09:00",
            "endTime" to "2020-12-01T09:30"
          ),
          "main" to mapOf(
            "locationId" to 2,
            "startTime" to "2020-12-01T09:30",
            "endTime" to "2020-12-01T10:00"
          ),
          "post" to mapOf(
            "locationId" to 3,
            "startTime" to "2020-12-01T10:00",
            "endTime" to "2020-12-01T10:30"
          )
        )
      )
    )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `Reject missing main`() {
    failWithJson(
      gson.toJson(
        mapOf(
          "bookingId" to 1,
          "court" to "Test Court 1"
        )
      ),
      400
    )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `Reject missing bookingId`() {
    failWithJson(
      gson.toJson(
        mapOf(
          "court" to "Test Court 1",
          "main" to mapOf(
            "startTime" to "2020-12-01T09:30",
            "endTime" to "2020-12-01T10:00"
          ),
        )
      ),
      400
    )
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `Reject missing locationId`() {
    failWithJson(
      gson.toJson(
        mapOf(
          "bookingId" to 1,
          "court" to "Test Court 1",
          "main" to mapOf(
            "startTime" to "2020-12-01T09:30",
            "endTime" to "2020-12-01T10:00"
          ),
        )
      ),
      400
    )
  }

  private fun passWithJson(json: String) {
    whenever(courtService.createVideoLinkBooking(any())).thenReturn(1L)

    mockMvc.perform(
      post("/court/video-link-bookings")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
    )
      .andExpect(status().isCreated)
      .andExpect(jsonPath("$").value(1))
  }

  private fun failWithJson(json: String, expectedStatus: Int) {
    whenever(courtService.createVideoLinkBooking(any())).thenReturn(1L)

    mockMvc.perform(
      post("/court/video-link-bookings")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
    )
      .andExpect(status().`is`(expectedStatus))
  }
}
