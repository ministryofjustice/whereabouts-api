package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.services.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkAppointmentLinker
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserMdcFilter
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException

@WebMvcTest(CourtController::class)
@Import(UserMdcFilter::class, StubUserSecurityUtilsConfig::class)
class CourtControllerTest : TestController() {
  @MockBean
  lateinit var courtService: CourtService

  @MockBean
  lateinit var videoLinkAppointmentLinker: VideoLinkAppointmentLinker

  @Nested
  inner class `Creating a booking` {

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Accept a valid request, main appointment only`() {
      passWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
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
    fun `Missing madeByTheCourt doesn't fail`() {
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
            "madeByTheCourt" to true,
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
            "madeByTheCourt" to true,
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
    fun `Reject missing startTime`() {
      failWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
            "main" to mapOf(
              "locationId" to 1,
              "endTime" to "2020-12-01T10:00"
            ),
          )
        ),
        400
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Reject bad startTime`() {
      failWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
            "main" to mapOf(
              "locationId" to 1,
              "startTime" to "2020-13-45T00:00",
              "endTime" to "2020-12-01T10:00"
            ),
          )
        ),
        400
      )
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `Reject missing endTime`() {
      failWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
            "main" to mapOf(
              "locationId" to 1,
              "startTime" to "2020-12-01T10:00"
            ),
          )
        ),
        400
      )
    }

    @Test
    fun `Valid request, no user`() {
      failWithJson(
        gson.toJson(
          mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "madeByTheCourt" to true,
            "main" to mapOf(
              "locationId" to 2,
              "startTime" to "2020-12-01T09:00",
              "endTime" to "2020-12-01T09:30"
            )
          )
        ),
        401
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

  @Nested
  inner class `Get a booking` {



    private fun passWithJson(videoBookingId: Long, json: String) {
      whenever(courtService.getVideoLinkBooking(any())).thenReturn(VideoLinkBookingResponse(
        bookingId = 100,
        court = "Test Court",
        pre = VideoLinkBookingResponse.VideoLinkAppointmentDto(
          locationId = 10,
          startTime = LocalDateTime.of(2020, 2, 7, 12, 0 ),
          endTime = LocalDateTime.of(2020, 2, 7, 13, 0 ),
        ))

      mockMvc.perform(
        get("/court/video-link-bookings/{videoBookingId}", videoBookingId)
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").value(1))
      )
    }

    private fun failWithJson(videoBookingId: Long, json: String, expectedStatus: Int) {
      whenever(courtService.getVideoLinkBooking(any())).thenReturn(1L)

      mockMvc.perform(
        get("/court/video-link-bookings/{videoBookingId}", videoBookingId)
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().`is`(expectedStatus))
    }
  }

    @Nested
  inner class `Deleting a booking` {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `successful deletion`() {
      val bookingId = 1L
      mockMvc.perform(
        delete("/court/video-link-bookings/$bookingId")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().isNoContent)

        verify(courtService).deleteVideoLinkBooking(1)
    }

    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `the booking does not exist`() {
      val bookingId = 1L
      doThrow(EntityNotFoundException("Video link booking with id $bookingId not found")).whenever(courtService).deleteVideoLinkBooking(bookingId)

      mockMvc.perform(
        delete("/court/video-link-bookings/$bookingId")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().`is`(404))
    }

    @Test
    fun `there is no user`() {
      val bookingId = 1L

      mockMvc.perform(
        delete("/court/video-link-bookings/$bookingId")
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().`is`(401))
    }

  }
}
