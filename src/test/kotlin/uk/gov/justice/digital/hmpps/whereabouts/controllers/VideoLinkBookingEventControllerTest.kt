package uk.gov.justice.digital.hmpps.whereabouts.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingEventService
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserMdcFilter
import java.time.LocalDate

@WebMvcTest(
  VideoLinkBookingEventController::class,
  excludeAutoConfiguration = [SecurityAutoConfiguration::class, OAuth2ClientAutoConfiguration::class, OAuth2ResourceServerAutoConfiguration::class],
)
@Import(UserMdcFilter::class, StubUserSecurityUtilsConfig::class)
class VideoLinkBookingEventControllerTest : TestController() {
  @MockBean
  lateinit var videoLinkBookingEventService: VideoLinkBookingEventService

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `happy flow`() {
    whenever(videoLinkBookingEventService.getEventsAsCSV(any(), any())).thenReturn(",,,,,,,,\n")

    mockMvc
      .perform(
        get("/events/video-link-booking-events")
          .param("start-date", "2021-03-01")
          .accept("text/csv"),
      )
      .andExpect(status().isOk)
      .andExpect(content().contentType("text/csv;charset=UTF-8"))
      .andExpect(content().string(",,,,,,,,\n"))

    verify(videoLinkBookingEventService).getEventsAsCSV(LocalDate.of(2021, 3, 1), 7L)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `bad start-date`() {
    whenever(videoLinkBookingEventService.getEventsAsCSV(any(), any())).thenReturn(",,,,,,,,\n")

    mockMvc
      .perform(
        get("/events/video-link-booking-events")
          .param("start-date", "xxxxx")
          .accept("text/csv"),
      )
      .andExpect(status().isBadRequest)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `missing start-date`() {
    whenever(videoLinkBookingEventService.getEventsAsCSV(any(), any())).thenReturn(",,,,,,,,\n")

    mockMvc
      .perform(
        get("/events/video-link-booking-events")
          .accept("text/csv"),
      )
      .andExpect(status().isBadRequest)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `with days`() {
    whenever(videoLinkBookingEventService.getEventsAsCSV(any(), any())).thenReturn(",,,,,,,,\n")

    mockMvc
      .perform(
        get("/events/video-link-booking-events")
          .param("start-date", "2021-03-01")
          .param("days", "3")
          .accept("text/csv"),
      )
      .andExpect(status().isOk)

    verify(videoLinkBookingEventService).getEventsAsCSV(LocalDate.of(2021, 3, 1), 3L)
  }

  @Test
  @WithMockUser(username = "ITAG_USER")
  fun `bad days`() {
    whenever(videoLinkBookingEventService.getEventsAsCSV(any(), any())).thenReturn(",,,,,,,,\n")

    mockMvc
      .perform(
        get("/events/video-link-booking-events")
          .param("start-date", "2021-03-01")
          .param("days", "xxxx")
          .accept("text/csv"),
      )
      .andExpect(status().isBadRequest)
  }
}
