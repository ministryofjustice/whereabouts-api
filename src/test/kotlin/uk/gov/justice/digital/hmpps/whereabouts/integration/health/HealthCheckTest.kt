package uk.gov.justice.digital.hmpps.whereabouts.integration.health

import com.github.tomakehurst.wiremock.client.WireMock
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.integration.IntegrationTest

class HealthCheckTest : IntegrationTest() {
  @Test
  fun `Health page reports ok`() {
    stubPing(200)

    webTestClient.get()
      .uri("/health")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.prisonApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.components.OAuthApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.components.caseNotesApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.status").isEqualTo("UP")
  }

  @Test
  fun `Health page reports down`() {
    stubPing(404)

    webTestClient.get()
      .uri("/health")
      .headers(setHeaders())
      .exchange()
      .expectStatus().is5xxServerError
      .expectBody()
      .jsonPath("$.components.prisonApiHealth.details.error")
      .isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException\$NotFound: 404 Not Found from GET http://localhost:8999/health/ping")
      .jsonPath("$.components.prisonApiHealth.details.body").isEqualTo("some error")
      .jsonPath("$.components.OAuthApiHealth.details.error")
      .isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException\$NotFound: 404 Not Found from GET http://localhost:8090/auth/health/ping")
      .jsonPath("$.components.OAuthApiHealth.details.body").isEqualTo("some error")
      .jsonPath("$.components.caseNotesApiHealth.details.error")
      .isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException\$NotFound: 404 Not Found from GET http://localhost:8093/health/ping")
      .jsonPath("$.components.caseNotesApiHealth.details.body").isEqualTo("some error")
  }

  @Test
  fun `Health page reports a teapot`() {
    stubPing(418)

    webTestClient.get()
      .uri("/health")
      .headers(setHeaders())
      .exchange()
      .expectStatus().is5xxServerError
      .expectBody()
      .jsonPath("$.components.prisonApiHealth.details.error")
      .isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException: 418 I'm a teapot from GET http://localhost:8999/health/ping")
      .jsonPath("$.components.prisonApiHealth.details.body").isEqualTo("some error")
      .jsonPath("$.components.OAuthApiHealth.details.error")
      .isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException: 418 I'm a teapot from GET http://localhost:8090/auth/health/ping")
      .jsonPath("$.components.OAuthApiHealth.details.body").isEqualTo("some error")
      .jsonPath("$.components.caseNotesApiHealth.details.error")
      .isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException: 418 I'm a teapot from GET http://localhost:8093/health/ping")
      .jsonPath("$.components.caseNotesApiHealth.details.body").isEqualTo("some error")
  }

  @Test
  fun `Health status is OK`() {
    stubPing(200)

    webTestClient.get()
      .uri("/health")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.status").isEqualTo("UP")
  }

  @Test
  fun `Health liveness page is accessible`() {
    webTestClient.get()
      .uri("/health/liveness")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.status").isEqualTo("UP")
  }

  @Test
  fun `Health readiness page is accessible`() {
    webTestClient.get()
      .uri("/health/readiness")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.status").isEqualTo("UP")
  }

  private fun stubPing(status: Int) {
    prisonApiMockServer.stubFor(
      WireMock.get("/health/ping").willReturn(
        WireMock.aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )

    oauthMockServer.stubFor(
      WireMock.get("/auth/health/ping").willReturn(
        WireMock.aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )

    caseNotesMockServer.stubFor(
      WireMock.get("/health/ping").willReturn(
        WireMock.aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )
  }
}
