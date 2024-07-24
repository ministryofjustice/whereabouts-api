package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.Location

class LocationIntegrationTest : IntegrationTest() {

  @Test
  fun `location groups for agency by location name - defined in properties - selects relevant locations only`() {
    prisonApiMockServer.stubAgencyLocationsByType("RNI", "CELL", getRniHb7Locations())

    webTestClient.get()
      .uri("/locations/groups/RNI/House block 7")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(loadJsonFile("RNI_location_groups_agency_locname.json"))
  }

  @Test
  fun `location groups for agency by location name - defined in prison API - selects relevant locations only`() {
    prisonApiMockServer.stubAgencyLocationsByType("LEI", "CELL", getLeiHb7Locations())

    webTestClient.get()
      .uri("/locations/groups/LEI/House_block_7")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(loadJsonFile("LEI_location_groups_agency_locname.json"))
  }

  @Test
  fun `location groups for agency by location name - agency locations not found - returns not found`() {
    prisonApiMockServer.stubGetAgencyLocationsByTypeNotFound("not_an_agency", "CELL")

    webTestClient.get()
      .uri("/locations/groups/not_an_agency/House block 7")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.developerMessage").isEqualTo("Locations not found for agency not_an_agency with location type CELL")
  }

  @Test
  fun `location groups for agency by location name - server error from prison API - server error passed to client`() {
    prisonApiMockServer.stubGetAgencyLocationsByTypeServerError("any_agency", "CELL")

    webTestClient.get()
      .uri("/locations/groups/any_agency/any_location_type")
      .headers(setHeaders())
      .exchange()
      .expectStatus().is5xxServerError
      .expectBody()
      .jsonPath("$.developerMessage")
      .isEqualTo("500 Internal Server Error from GET http://localhost:8999/api/agencies/any_agency/locations/type/CELL")
  }

  @Test
  fun `get location prefix by group`() {
    webTestClient.get()
      .uri("/locations/MDI/Houseblock 1/location-prefix")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(loadJsonFile("MDI_location-prefix.json"))
  }

  private fun getRniHb7Locations() =
    listOf(
      aLocation(locationId = 507011, description = "Hb7-1-002", agencyId = "RNI", locationPrefix = "RNI-HB7-1-002"),
      aLocation(locationId = 507031, description = "Hb7-1-021", agencyId = "RNI", locationPrefix = "RNI-HB7-1-021"),
      aLocation(locationId = 108582, description = "S-1-001", agencyId = "RNI", locationPrefix = "RNI-S-1-001"),
      aLocation(locationId = 108583, description = "S-1-002", agencyId = "RNI", locationPrefix = "RNI-S-1-002"),
    )

  private fun getLeiHb7Locations() =
    listOf(
      aLocation(
        locationId = 507011,
        description = "House_block_7-1-002",
        agencyId = "LEI",
        locationPrefix = "LEI-House-block-7-1-002",
      ),
      aLocation(
        locationId = 507031,
        description = "House_block_7-1-021",
        agencyId = "LEI",
        locationPrefix = "LEI-House-block-7-1-021",
      ),
      aLocation(locationId = 108582, description = "S-1-001", agencyId = "LEI", locationPrefix = "LEI-S-1-001"),
      aLocation(locationId = 108583, description = "S-1-002", agencyId = "LEI", locationPrefix = "LEI-S-1-002"),
    )

  private fun aLocation(locationId: Long, description: String, agencyId: String, locationPrefix: String) =
    Location(
      locationId = locationId,
      locationType = "CELL",
      description = description,
      agencyId = agencyId,
      currentOccupancy = 0,
      locationPrefix = locationPrefix,
      operationalCapacity = 0,
      userDescription = "",
      internalLocationCode = "",
    )
}
