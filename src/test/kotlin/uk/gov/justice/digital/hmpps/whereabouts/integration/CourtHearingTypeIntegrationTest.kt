package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional

@Transactional
class CourtHearingTypeIntegrationTest : IntegrationTest() {

  @Test
  fun `Get All hearing types without user token`() {
    webTestClient.get()
      .uri(baseUrl)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `Get All hearing types`() {
    webTestClient.get()
      .uri(baseUrl)
      .headers {
        it.setBearerAuth(
          jwtAuthHelper.createJwt(
            subject = "ITAG_USER",
            roles = listOf("ROLE_VIDEO_LINK_COURT_USER", "ROLE_MAINTAIN_WHEREABOUTS"),
          ),
        )
      }
      .exchange()
      .expectBody()
      .json(loadJsonFile("court-hearing-types.json"))
  }

  companion object {
    const val baseUrl = "/court/hearing-type"
  }
}
