package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.Location

class VideoLinkBookingRoomsIntegrationTest : IntegrationTest() {

  @Test
  fun `should retrieve details of rooms of type VIDE only`() {
    prisonApiMockServer.stubGetAgencyLocationsForTypeUnrestricted("MDI", "APP", getVideLocations())

    webTestClient.get()
      .uri("/video-link-rooms/MDI")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("[0].locationId").isEqualTo(1)
      .jsonPath("[0].description").isEqualTo("Room A")
      .jsonPath("[1].locationId").isEqualTo(2)
      .jsonPath("[1].description").isEqualTo("room-b")
      .jsonPath("$.length()").value<Int> { assertThat(it).isEqualTo(2) }
  }
}

private fun getVideLocations() =
  listOf(
    Location(
      locationId = 1, locationType = "VIDE", description = "room-a",
      locationUsage = "", agencyId = "MDI", parentLocationId = 123,
      currentOccupancy = 2, locationPrefix = "", operationalCapacity = 10,
      userDescription = "Room A", internalLocationCode = "",
    ),

    Location(
      locationId = 2, locationType = "VIDE", description = "room-b",
      locationUsage = "", agencyId = "MDI", parentLocationId = 123,
      currentOccupancy = 2, locationPrefix = "", operationalCapacity = 10,
      userDescription = null, internalLocationCode = "",
    ),

    Location(
      locationId = 3, locationType = "MEETING", description = "Meeting room",
      locationUsage = "", agencyId = "MDI", parentLocationId = 123,
      currentOccupancy = 2, locationPrefix = "", operationalCapacity = 10,
      userDescription = "", internalLocationCode = "",
    ),
  )
