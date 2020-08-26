package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.CellWithAttributes
import uk.gov.justice.digital.hmpps.whereabouts.model.Location

class LocationIntegrationTest: IntegrationTest() {

  @Test
  fun `location groups for agency by location name - defined in properties - selects relevant locations only`() {
    prisonApiMockServer.stubAgencyLocationsByType("RNI", "CELL", getRniHb7Locations())

    webTestClient.get()
        .uri("/locations/groups/RNI/House block 7")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.[0].locationId").isEqualTo(507011)
        .jsonPath("$.[0].locationType").isEqualTo("CELL")
        .jsonPath("$.[0].description").isEqualTo("Hb7-1-002")
        .jsonPath("$.[0].agencyId").isEqualTo("RNI")
        .jsonPath("$.[0].currentOccupancy").isEqualTo(0)
        .jsonPath("$.[0].locationPrefix").isEqualTo("RNI-HB7-1-002")
        .jsonPath("$.[0].operationalCapacity").isEqualTo(0)
        .jsonPath("$.[0].userDescription").isEmpty
        .jsonPath("$.[0].internalLocationCode").isEmpty
        .jsonPath("$.[1].locationId").isEqualTo(507031)
        .jsonPath("$.[1].locationType").isEqualTo("CELL")
        .jsonPath("$.[1].description").isEqualTo("Hb7-1-021")
        .jsonPath("$.[1].agencyId").isEqualTo("RNI")
        .jsonPath("$.[1].currentOccupancy").isEqualTo(0)
        .jsonPath("$.[1].locationPrefix").isEqualTo("RNI-HB7-1-021")
        .jsonPath("$.[1].operationalCapacity").isEqualTo(0)
        .jsonPath("$.[1].userDescription").isEmpty
        .jsonPath("$.[1].internalLocationCode").isEmpty
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
        .jsonPath("$.[0].locationId").isEqualTo(507011)
        .jsonPath("$.[0].locationType").isEqualTo("CELL")
        .jsonPath("$.[0].description").isEqualTo("House_block_7-1-002")
        .jsonPath("$.[0].agencyId").isEqualTo("LEI")
        .jsonPath("$.[0].currentOccupancy").isEqualTo(0)
        .jsonPath("$.[0].locationPrefix").isEqualTo("LEI-House-block-7-1-002")
        .jsonPath("$.[0].operationalCapacity").isEqualTo(0)
        .jsonPath("$.[0].userDescription").isEmpty
        .jsonPath("$.[0].internalLocationCode").isEmpty
        .jsonPath("$.[1].locationId").isEqualTo(507031)
        .jsonPath("$.[1].locationType").isEqualTo("CELL")
        .jsonPath("$.[1].description").isEqualTo("House_block_7-1-021")
        .jsonPath("$.[1].agencyId").isEqualTo("LEI")
        .jsonPath("$.[1].currentOccupancy").isEqualTo(0)
        .jsonPath("$.[1].locationPrefix").isEqualTo("LEI-House-block-7-1-021")
        .jsonPath("$.[1].operationalCapacity").isEqualTo(0)
        .jsonPath("$.[1].userDescription").isEmpty
        .jsonPath("$.[1].internalLocationCode").isEmpty
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
        .jsonPath("$.developerMessage").isEqualTo("500 Internal Server Error from GET http://localhost:8999/api/agencies/any_agency/locations/type/CELL")
  }

  @Test
  fun `cells with capacity - no attribute`() {
    prisonApiMockServer.stubCellsWithCapacityNoAttribute("RNI", getRniHb7Cells())

    webTestClient.get()
        .uri("/locations/cellsWithCapacity/RNI/House block 7")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.[0].id").isEqualTo(507011)
        .jsonPath("$.[0].description").isEqualTo("RNI-HB7-1-002")
        .jsonPath("$.[0].noOfOccupants").isEqualTo(1)
        .jsonPath("$.[0].capacity").isEqualTo(2)
        .jsonPath("$.[1].id").isEqualTo(507031)
        .jsonPath("$.[1].description").isEqualTo("RNI-HB7-1-021")
        .jsonPath("$.[1].noOfOccupants").isEqualTo(1)
        .jsonPath("$.[1].capacity").isEqualTo(2)
        .jsonPath("$.[1].attributes").isEmpty
  }

  @Test
  fun `cells with capacity - passes attribute`() {
    prisonApiMockServer.stubCellsWithCapacityWithAttribute("RNI", getRniHb7Cells(), "LC")

    webTestClient.get()
        .uri("/locations/cellsWithCapacity/RNI/House block 7?attribute=LC")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.[0].id").isEqualTo(507011)
        .jsonPath("$.[0].description").isEqualTo("RNI-HB7-1-002")
        .jsonPath("$.[0].noOfOccupants").isEqualTo(1)
        .jsonPath("$.[0].capacity").isEqualTo(2)
        .jsonPath("$.[1].id").isEqualTo(507031)
        .jsonPath("$.[1].description").isEqualTo("RNI-HB7-1-021")
        .jsonPath("$.[1].noOfOccupants").isEqualTo(1)
        .jsonPath("$.[1].capacity").isEqualTo(2)
        .jsonPath("$.[1].attributes").isEmpty
  }

  private fun getRniHb7Locations() =
      listOf(
          aLocation(locationId = 507011, description = "Hb7-1-002", agencyId = "RNI", locationPrefix = "RNI-HB7-1-002"),
          aLocation(locationId = 507031, description = "Hb7-1-021", agencyId = "RNI", locationPrefix = "RNI-HB7-1-021"),
          aLocation(locationId = 108582, description = "S-1-001",   agencyId = "RNI", locationPrefix = "RNI-S-1-001"),
          aLocation(locationId = 108583, description = "S-1-002",   agencyId = "RNI", locationPrefix = "RNI-S-1-002")
      )

  private fun getLeiHb7Locations() =
      listOf(
          aLocation(locationId = 507011, description = "House_block_7-1-002", agencyId = "LEI", locationPrefix = "LEI-House-block-7-1-002"),
          aLocation(locationId = 507031, description = "House_block_7-1-021", agencyId = "LEI", locationPrefix = "LEI-House-block-7-1-021"),
          aLocation(locationId = 108582, description = "S-1-001",   agencyId = "LEI", locationPrefix = "LEI-S-1-001"),
          aLocation(locationId = 108583, description = "S-1-002",   agencyId = "LEI", locationPrefix = "LEI-S-1-002")
      )

  private fun getRniHb7Cells() =
          listOf(
                  CellWithAttributes(id = 507011, description = "RNI-HB7-1-002", noOfOccupants = 1, capacity = 2),
                  CellWithAttributes(id = 507031, description = "RNI-HB7-1-021", noOfOccupants = 1, capacity = 2),
                  CellWithAttributes(id = 108582, description = "RNI-S-1-001", noOfOccupants = 1, capacity = 2),
                  CellWithAttributes(id = 108583, description = "RNI-S-1-002", noOfOccupants = 1, capacity = 2)
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
          internalLocationCode = ""
      )
}
