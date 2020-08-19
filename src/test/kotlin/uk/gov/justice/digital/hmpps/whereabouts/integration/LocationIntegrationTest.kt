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
    elite2MockServer.stubAgencyLocationsByType("RNI", "CELL", getRniHb7Locations())

    val response: ResponseEntity<String> =
        restTemplate.exchange("/locations/groups/RNI/House block 7", HttpMethod.GET, createHeaderEntity(""))

    assertThatJsonFileAndStatus(response, 200, "RNI_location_groups_agency_locname.json")
  }

  @Test
  fun `location groups for agency by location name - defined in elite2 - selects relevant locations only`() {
    elite2MockServer.stubAgencyLocationsByType("LEI", "CELL", getLeiHb7Locations())

    val response: ResponseEntity<String> =
        restTemplate.exchange("/locations/groups/LEI/House_block_7", HttpMethod.GET, createHeaderEntity(""))

    assertThatJsonFileAndStatus(response, 200, "LEI_location_groups_agency_locname.json")
  }

  @Test
  fun `location groups for agency by location name - agency locations not found - returns not found`() {
    elite2MockServer.stubGetAgencyLocationsByTypeNotFound("not_an_agency", "CELL")

    val response: ResponseEntity<ErrorResponse> =
        restTemplate.exchange("/locations/groups/not_an_agency/House block 7", HttpMethod.GET, createHeaderEntity(""))

    assertThat(response.statusCodeValue).isEqualTo(404)
    assertThat(response.body.developerMessage).contains("not found").contains("not_an_agency").contains("CELL")
  }

  @Test
  fun `location groups for agency by location name - server error from elite2 - server error passed to client`() {
    elite2MockServer.stubGetAgencyLocationsByTypeServerError("any_agency", "CELL")

    val response: ResponseEntity<ErrorResponse> =
        restTemplate.exchange("/locations/groups/any_agency/any_location_type", HttpMethod.GET, createHeaderEntity(""))

    assertThat(response.statusCodeValue).isEqualTo(500)
    assertThat(response.body.developerMessage).contains("Server Error")
  }

  @Test
  fun `cells with capacity - no attribute`() {
    elite2MockServer.stubCellsWithCapacityNoAttribute("RNI", getRniHb7Cells())

    val response: ResponseEntity<String> =
            restTemplate.exchange("/locations/cellsWithCapacity/RNI/House block 7", HttpMethod.GET, createHeaderEntity(""))

    assertThatJsonFileAndStatus(response, 200, "RNI_cells_with_capacity.json")
  }

  @Test
  fun `cells with capacity - passes attribute`() {
    elite2MockServer.stubCellsWithCapacityWithAttribute("RNI", getRniHb7Cells(), "LC")

    val response: ResponseEntity<String> =
            restTemplate.exchange("/locations/cellsWithCapacity/RNI/House block 7?attribute=LC", HttpMethod.GET, createHeaderEntity(""))

    assertThatJsonFileAndStatus(response, 200, "RNI_cells_with_capacity.json")
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
