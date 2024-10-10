package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HealthCheckIntegrationTest : IntegrationTest() {

  @Test
  fun `Health page reports ok`() {
    subPing(200)

    webTestClient.get()
      .uri("/health")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.prisonApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.components.OAuthApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.components.caseNotesApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.components.prisonRegisterApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.components.locationApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.status").isEqualTo("UP")
  }

  @Test
  fun `Health page reports down`() {
    subPing(404)

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
      .jsonPath("$.components.prisonRegisterApiHealth.details.error")
      .isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException\$NotFound: 404 Not Found from GET http://localhost:8094/health/ping")
      .jsonPath("$.components.prisonRegisterApiHealth.details.body").isEqualTo("some error")
      .jsonPath("$.components.locationApiHealth.details.error")
      .isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException\$NotFound: 404 Not Found from GET http://localhost:8095/health/ping")
      .jsonPath("$.components.locationApiHealth.details.body").isEqualTo("some error")
      .jsonPath("$.status").isEqualTo("DOWN")
  }

  @Test
  fun `Health page reports a teapot`() {
    subPing(418)

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
      .jsonPath("$.components.prisonRegisterApiHealth.details.error")
      .isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException: 418 I'm a teapot from GET http://localhost:8094/health/ping")
      .jsonPath("$.components.prisonRegisterApiHealth.details.body").isEqualTo("some error")
      .jsonPath("$.components.locationApiHealth.details.error")
      .isEqualTo("org.springframework.web.reactive.function.client.WebClientResponseException: 418 I'm a teapot from GET http://localhost:8095/health/ping")
      .jsonPath("$.components.locationApiHealth.details.body").isEqualTo("some error")
      .jsonPath("$.status").isEqualTo("DOWN")
  }

  @Test
  fun `Health page reports a timeout`() {
    subPingWithDelay(200)

    webTestClient.get()
      .uri("/health")
      .headers(setHeaders())
      .exchange()
      .expectStatus().is5xxServerError
      .expectBody()
      .jsonPath("$.components.prisonApiHealth.details.error")
      .isEqualTo("java.lang.IllegalStateException: Timeout on blocking read for 1000000000 NANOSECONDS")
      .jsonPath("$.components.prisonRegisterApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.components.locationApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.components.OAuthApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.components.caseNotesApiHealth.details.HttpStatus").isEqualTo("OK")
      .jsonPath("$.status").isEqualTo("DOWN")
  }

  @Test
  fun `Health status is OK`() {
    subPing(200)

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

  @Test
  fun `has active prisons`() {
    prisonApiMockServer.stubGetAgencies()

    webTestClient.get().uri("/info")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("activeAgencies").value<List<String>> {
        assertThat(it.contains("IWI")).isTrue
        assertThat(it.contains("WDI")).isFalse
      }
  }

  private fun subPing(status: Int) {
    prisonApiMockServer.stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )

    oauthMockServer.stubFor(
      get("/auth/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )

    caseNotesMockServer.stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )
    prisonRegisterMockServer.stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )
    locationApiMockServer.stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )
  }

  private fun subPingWithDelay(status: Int) {
    prisonApiMockServer.stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status)
          .withFixedDelay(1000),
      ),
    )

    oauthMockServer.stubFor(
      get("/auth/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )

    caseNotesMockServer.stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )
    prisonRegisterMockServer.stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )
    locationApiMockServer.stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )
  }
}
