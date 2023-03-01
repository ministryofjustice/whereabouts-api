package uk.gov.justice.digital.hmpps.whereabouts.controllers

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationIdAndDescription
import uk.gov.justice.digital.hmpps.whereabouts.services.LocationService
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserMdcFilter

@WebMvcTest(
  VideoLinkBookingRoomsController::class,
  excludeAutoConfiguration = [SecurityAutoConfiguration::class, OAuth2ClientAutoConfiguration::class, OAuth2ResourceServerAutoConfiguration::class],
)
@Import(UserMdcFilter::class, StubUserSecurityUtilsConfig::class)
class VideoLinkBookingRoomsControllerTest : TestController() {
  @MockBean
  lateinit var locationService: LocationService

  private val location1 = LocationIdAndDescription(
    locationId = 1,
    description = "Video Room A",
  )

  private val location2 = LocationIdAndDescription(
    locationId = 2,
    description = "Video Room B",
  )

  val locations: List<LocationIdAndDescription> = listOf(location1, location2)

  @Nested
  inner class `Video link rooms` {
    @Test
    @WithMockUser(username = "ITAG_USER")
    fun `get all video link rooms`() {
      whenever(locationService.getVideoLinkRoomsForPrison("MDI")).thenReturn(locations)

      mockMvc.perform(
        get("/video-link-rooms/MDI")
          .contentType(MediaType.APPLICATION_JSON),
      )
        .andExpect(status().isOk)
        .andExpect(jsonPath("[0].locationId").value(1))
        .andExpect(jsonPath("[0].description").value("Video Room A"))
        .andExpect(jsonPath("[1].locationId").value(2))
        .andExpect(jsonPath("[1].description").value("Video Room B"))
    }
  }
}
