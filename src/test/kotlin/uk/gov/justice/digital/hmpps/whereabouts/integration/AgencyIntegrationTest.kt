package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.junit.jupiter.api.Test

class AgencyIntegrationTest: IntegrationTest() {

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
        .jsonPath("$.[0].name").isEqualTo("Block A")
        .jsonPath("$.[0].key").isEqualTo("A")
        .jsonPath("$.[0].children").isEmpty

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
        .jsonPath("$.[0].name").isEqualTo("Casu")
        .jsonPath("$.[0].key").isEqualTo("Casu")
        .jsonPath("$.[0].children").isEmpty
        .jsonPath("$.[1].name").isEqualTo("Houseblock 1")
        .jsonPath("$.[1].key").isEqualTo("Houseblock 1")
        .jsonPath("$.[1].children.[0].name").isEqualTo("A-Wing")
        .jsonPath("$.[1].children.[0].key").isEqualTo("A-Wing")
        .jsonPath("$.[1].children.[0].children").isEmpty
        .jsonPath("$.[1].children.[1].name").isEqualTo("B-Wing")
        .jsonPath("$.[1].children.[1].key").isEqualTo("B-Wing")
        .jsonPath("$.[1].children.[1].children").isEmpty
        .jsonPath("$.[1].children.[2].name").isEqualTo("C-Wing")
        .jsonPath("$.[1].children.[2].key").isEqualTo("C-Wing")
        .jsonPath("$.[1].children.[2].children").isEmpty
        .jsonPath("$.[2].name").isEqualTo("Houseblock 2")
        .jsonPath("$.[2].key").isEqualTo("Houseblock 2")
        .jsonPath("$.[2].children.[0].name").isEqualTo("A-Wing")
        .jsonPath("$.[2].children.[0].key").isEqualTo("A-Wing")
        .jsonPath("$.[2].children.[0].children").isEmpty
        .jsonPath("$.[2].children.[1].name").isEqualTo("B-Wing")
        .jsonPath("$.[2].children.[1].key").isEqualTo("B-Wing")
        .jsonPath("$.[2].children.[1].children").isEmpty
        .jsonPath("$.[2].children.[2].name").isEqualTo("C-Wing")
        .jsonPath("$.[2].children.[2].key").isEqualTo("C-Wing")
        .jsonPath("$.[2].children.[2].children").isEmpty
        .jsonPath("$.[3].name").isEqualTo("Houseblock 3")
        .jsonPath("$.[3].key").isEqualTo("Houseblock 3")
        .jsonPath("$.[3].children.[0].name").isEqualTo("A-Wing")
        .jsonPath("$.[3].children.[0].key").isEqualTo("A-Wing")
        .jsonPath("$.[3].children.[0].children").isEmpty
        .jsonPath("$.[3].children.[1].name").isEqualTo("B-Wing")
        .jsonPath("$.[3].children.[1].key").isEqualTo("B-Wing")
        .jsonPath("$.[3].children.[1].children").isEmpty
        .jsonPath("$.[3].children.[2].name").isEqualTo("C-Wing")
        .jsonPath("$.[3].children.[2].key").isEqualTo("C-Wing")
        .jsonPath("$.[3].children.[2].children").isEmpty
        .jsonPath("$.[4].name").isEqualTo("Houseblock 4")
        .jsonPath("$.[4].key").isEqualTo("Houseblock 4")
        .jsonPath("$.[4].children.[0].name").isEqualTo("A-Wing")
        .jsonPath("$.[4].children.[0].key").isEqualTo("A-Wing")
        .jsonPath("$.[4].children.[0].children").isEmpty
        .jsonPath("$.[4].children.[1].name").isEqualTo("B-Wing")
        .jsonPath("$.[4].children.[1].key").isEqualTo("B-Wing")
        .jsonPath("$.[4].children.[1].children").isEmpty
        .jsonPath("$.[4].children.[2].name").isEqualTo("C-Wing")
        .jsonPath("$.[4].children.[2].key").isEqualTo("C-Wing")
        .jsonPath("$.[4].children.[2].children").isEmpty
        .jsonPath("$.[5].name").isEqualTo("Houseblock 5")
        .jsonPath("$.[5].key").isEqualTo("Houseblock 5")
        .jsonPath("$.[5].children.[0].name").isEqualTo("A-Wing")
        .jsonPath("$.[5].children.[0].key").isEqualTo("A-Wing")
        .jsonPath("$.[5].children.[0].children").isEmpty
        .jsonPath("$.[5].children.[1].name").isEqualTo("B-Wing")
        .jsonPath("$.[5].children.[1].key").isEqualTo("B-Wing")
        .jsonPath("$.[5].children.[1].children").isEmpty
        .jsonPath("$.[6].name").isEqualTo("Houseblock 6")
        .jsonPath("$.[6].key").isEqualTo("Houseblock 6")
        .jsonPath("$.[6].children.[0].name").isEqualTo("A-Wing")
        .jsonPath("$.[6].children.[0].key").isEqualTo("A-Wing")
        .jsonPath("$.[6].children.[0].children").isEmpty
        .jsonPath("$.[6].children.[1].name").isEqualTo("B-Wing")
        .jsonPath("$.[6].children.[1].key").isEqualTo("B-Wing")
        .jsonPath("$.[6].children.[1].children").isEmpty
        .jsonPath("$.[7].name").isEqualTo("Houseblock 7")
        .jsonPath("$.[7].key").isEqualTo("Houseblock 7")
        .jsonPath("$.[7].children").isEmpty
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
