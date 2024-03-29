package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional

@Transactional
class CourtHearingTypeIntegrationTest : IntegrationTest() {

  @Test
  fun `Get All hearing types without user token`() {
    webTestClient.get()
      .uri(BASE_URL)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `Get All hearing types`() {
    webTestClient.get()
      .uri(BASE_URL)
      .headers {
        it.setBearerAuth(
          jwtAuthHelper.createJwt(
            subject = "ITAG_USER",
            roles = listOf("ROLE_VIDEO_LINK_COURT_USER"),
          ),
        )
      }
      .exchange()
      .expectBody()
      .json(loadJsonFile("court-hearing-types.json"))
  }

  companion object {
    const val BASE_URL = "/court/hearing-type"
  }
}
