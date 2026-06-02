package uk.gov.justice.digital.hmpps.whereabouts.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import uk.gov.justice.digital.hmpps.whereabouts.integration.IntegrationTest

class InfoTest(
  @Autowired
  private val buildProperties: BuildProperties,
) : IntegrationTest() {

  @BeforeEach
  fun setUp() {
    prisonApiMockServer.stubGetAgencies()
  }

  @Test
  fun `info page is accessible`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("build.name").isEqualTo("whereabouts-api")
  }

  @Test
  fun `info page reports version`() {
    webTestClient.get().uri("/info")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("build.version").isEqualTo(buildProperties.version)
  }

  @Test
  fun `has active prisons`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("activeAgencies").value<List<String>> {
        assertThat(it.contains("IWI")).isTrue
        assertThat(it.contains("WDI")).isFalse
      }
  }
}
