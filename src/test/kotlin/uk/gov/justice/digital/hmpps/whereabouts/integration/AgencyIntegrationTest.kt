package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AgencyIntegrationTest : IntegrationTest() {

  @Test
  fun `location groups - none in properties - should retrieve groups from prison API`() {
    val agencyId = "LEI"
    prisonApiMockServer.stubGetAgencyLocationGroups(agencyId)

    webTestClient.get()
      .uri("/agencies/$agencyId/locations/groups")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(loadJsonFile("LEI_location_groups.json"))
  }

  @Test
  fun `location groups - exist in properties - should retrieve groups from properties`() {
    val agencyId = "MDI"

    webTestClient.get()
      .uri("/agencies/$agencyId/locations/groups")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("[0].name").isEqualTo("Houseblock 1")
      .jsonPath("[1].name").isEqualTo("Houseblock 2")
      .jsonPath("[1].key").isEqualTo("Houseblock 2")
      .jsonPath("[1].children[0].name").isEqualTo("A-Wing")
      .jsonPath("[1].children[0].key").isEqualTo("A-Wing")
      .jsonPath("[1].children[0].children").isEmpty
      .jsonPath("$.length()").value<Int> { assertThat(it).isGreaterThan(2) }
  }

  @Test
  fun `location groups - agency not found - should be returned to caller`() {
    val notAnAgencyId = "NON"
    prisonApiMockServer.stubGetAgencyLocationGroupsNotFound(notAnAgencyId)

    webTestClient.get()
      .uri("/agencies/$notAnAgencyId/locations/groups")
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .isNotFound
      .expectBody()
      .jsonPath("$.developerMessage").isEqualTo("Locations not found for agency NON")
  }

  @Test
  fun `location groups - prison API server error - should be returned to caller`() {
    prisonApiMockServer.stubGetAgencyLocationGroupsServerError("LEI")

    webTestClient.get()
      .uri("/agencies/LEI/locations/groups")
      .headers(setHeaders())
      .exchange()
      .expectStatus()
      .is5xxServerError
  }
}
