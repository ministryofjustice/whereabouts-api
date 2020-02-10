package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse

class AgencyIntegrationTest: IntegrationTest() {

  @Test
  fun `location groups - none in properties - should retrieve groups from elite2api`() {
    val agencyId = "LEI"
    elite2MockServer.stubGetAgencyLocationGroups(agencyId)

    val response: ResponseEntity<String> =
        restTemplate.exchange("/agencies/$agencyId/locations/groups", HttpMethod.GET, createHeaderEntity(""))

    assertThatJsonFileAndStatus(response, 200, "LEI_location_groups.json");
  }

  @Test
  fun `location groups - exist in properties - should retrieve groups from properties`() {
    val agencyId = "MDI"

    val response: ResponseEntity<String> =
        restTemplate.exchange("/agencies/$agencyId/locations/groups", HttpMethod.GET, createHeaderEntity(""))

    assertThatJsonFileAndStatus(response, 200, "MDI_location_groups.json");
  }

  @Test
  fun `location groups - agency not found - should be returned to caller`() {
    val notAnAgencyId = "NON"
    elite2MockServer.stubGetAgencyLocationGroupsNotFound(notAnAgencyId)

    val response: ResponseEntity<ErrorResponse> =
        restTemplate.exchange("/agencies/$notAnAgencyId/locations/groups", HttpMethod.GET, createHeaderEntity(""))

    assertThat(response.statusCodeValue).isEqualTo(404)
    assertThat(response.body.userMessage).contains(notAnAgencyId).contains("not found")
  }

  @Test
  fun `location groups - elite server error - should be returned to caller`() {
    elite2MockServer.stubGetAgencyLocationGroupsServerError("LEI")

    val response: ResponseEntity<ErrorResponse> =
        restTemplate.exchange("/agencies/LEI/locations/groups", HttpMethod.GET, createHeaderEntity(""))

    assertThat(response.statusCodeValue).isEqualTo(500)
  }
}