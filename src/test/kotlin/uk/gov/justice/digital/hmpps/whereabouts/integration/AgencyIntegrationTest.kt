package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup

class AgencyIntegrationTest: IntegrationTest() {

  @Test
  fun `location groups should retrieve groups from elite2api`() {
    val agencyId = "LEI"
    elite2MockServer.stubGetAgencyLocationGroups(agencyId)

    val response: ResponseEntity<List<LocationGroup>> =
        restTemplate.exchange("/agencies/$agencyId/locations/groups", HttpMethod.GET, createHeaderEntity(""))
    val locationGroups = response.body

    assertThat(response.statusCodeValue).isEqualTo(200)
    assertThat(locationGroups).containsExactly(LocationGroup(key="A", name="Block A"))
  }

  @Test
  fun `agency not found should be returned to caller`() {
    val notAnAgencyId = "NON"
    elite2MockServer.stubGetAgencyLocationGroupsNotFound(notAnAgencyId)

    val response: ResponseEntity<ErrorResponse> =
        restTemplate.exchange("/agencies/$notAnAgencyId/locations/groups", HttpMethod.GET, createHeaderEntity(""))

    assertThat(response.statusCodeValue).isEqualTo(404)
    assertThat(response.body.userMessage).contains(notAnAgencyId).contains("not found")
  }

  @Test
  fun `elite server error should be returned to caller`() {
    elite2MockServer.stubGetAgencyLocationGroupsServerError("LEI")

    val response: ResponseEntity<ErrorResponse> =
        restTemplate.exchange("/agencies/LEI/locations/groups", HttpMethod.GET, createHeaderEntity(""))

    assertThat(response.statusCodeValue).isEqualTo(500)
  }
}