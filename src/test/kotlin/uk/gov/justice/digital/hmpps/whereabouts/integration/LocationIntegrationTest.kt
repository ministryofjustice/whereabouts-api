package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.Location

class LocationIntegrationTest: IntegrationTest() {

  @Test
  fun `location groups for agency by location name - defined in properties - selects relevant locations only`() {
    elite2MockServer.stubAgencyLocationsByType("RNI", "CELL", getRniHb7Locations())

    val response: ResponseEntity<List<Location>> =
        restTemplate.exchange("/locations/groups/RNI/House block 7", HttpMethod.GET, createHeaderEntity(""))

    assertThat(response.statusCodeValue).isEqualTo(200)
    assertThat(response.body).containsExactlyInAnyOrder(
        aLocation(locationId = 507011, description = "Hb7-1-002", agencyId = "RNI"),
        aLocation(locationId = 507031, description = "Hb7-1-021", agencyId = "RNI")
    )
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

    val response: ResponseEntity<String> =
        restTemplate.exchange("/locations/groups/any_agency/any_location_type", HttpMethod.GET, createHeaderEntity(""))

    assertThat(response.statusCodeValue).isEqualTo(500)
    assertThat(response.body).contains("Server error")
  }


  private fun getRniHb7Locations() =
      listOf(
          aLocation(locationId = 507011, description = "Hb7-1-002", agencyId = "RNI"),
          aLocation(locationId = 507031, description = "Hb7-1-021", agencyId = "RNI"),
          aLocation(locationId = 108582, description = "S-1-001",   agencyId = "RNI"),
          aLocation(locationId = 108583, description = "S-1-002",   agencyId = "RNI")
      )

  private fun aLocation(locationId: Long, description: String, agencyId: String) =
      Location(
          locationId = locationId,
          locationType = "CELL",
          description = description,
          agencyId = agencyId,
          currentOccupancy = 0,
          locationPrefix = "$agencyId-${description.toUpperCase()}",
          operationalCapacity = 0,
          userDescription = "",
          internalLocationCode = "",
          locationUsage = "",
          parentLocationId = null
      )
}