package uk.gov.justice.digital.hmpps.whereabouts.controllers

import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.whereabouts.services.LocationService
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.LocationIdAndDescription
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserMdcFilter
@WebMvcTest(VideoLinkBookingRoomsController::class)
@Import(UserMdcFilter::class, StubUserSecurityUtilsConfig::class)
class VideoLinkBookingRoomsControllerTest : TestController() {
  @MockBean
  lateinit var locationService: LocationService

  private val location1 = LocationIdAndDescription(
    locationId = 1,
    description = "Video Room A"
  )

  private val location2 = LocationIdAndDescription(
    locationId = 2,
    description = "Video Room B"
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
          .contentType(MediaType.APPLICATION_JSON)
      )
        .andExpect(status().isOk)
        .andExpect(jsonPath("[0].locationId").value(1))
        .andExpect(jsonPath("[0].description").value("Video Room A"))
        .andExpect(jsonPath("[1].locationId").value(2))
        .andExpect(jsonPath("[1].description").value("Video Room B"))
    }
  }
}
