package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class HealthCheckIntegrationTest : IntegrationTest() {
  @Test
  fun `Health page reports ok`() {
    subPing(200)

    val response = restTemplate.getForEntity("/health", String::class.java)

    System.out.println(response.body)
    assertThatJson(response.body).node("components.elite2ApiHealth.details.HttpStatus").isEqualTo("OK")
    assertThatJson(response.body).node("components.OAuthApiHealth.details.HttpStatus").isEqualTo("OK")
    assertThatJson(response.body).node("components.caseNotesApiHealth.details.HttpStatus").isEqualTo("OK")
    assertThatJson(response.body).node("status").isEqualTo("UP")
    assertThat(response.statusCodeValue).isEqualTo(200)
  }

  @Test
  fun `Health page reports down`() {
    subPing(404)

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("components.elite2ApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException\$NotFound: 404 Not Found: [some error]")
    assertThatJson(response.body).node("components.OAuthApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException\$NotFound: 404 Not Found: [some error]")
    assertThatJson(response.body).node("components.caseNotesApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException\$NotFound: 404 Not Found: [some error]")
    assertThatJson(response.body).node("status").isEqualTo("DOWN")
    assertThat(response.statusCodeValue).isEqualTo(503)
  }

  @Test
  fun `Health page reports a teapot`() {
    subPing(418)

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("components.elite2ApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException: 418 418: [some error]")
    assertThatJson(response.body).node("components.OAuthApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException: 418 418: [some error]")
    assertThatJson(response.body).node("components.caseNotesApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException: 418 418: [some error]")
    assertThatJson(response.body).node("status").isEqualTo("DOWN")
    assertThat(response.statusCodeValue).isEqualTo(503)
  }

  @Test
  fun `Health page reports a timeout`() {
    subPingWithDelay(200)

    val response = restTemplate.getForEntity("/health", String::class.java)

    assertThatJson(response.body).node("components.elite2ApiHealth.details.error").isEqualTo("org.springframework.web.client.ResourceAccessException: I/O error on GET request for \\\"http://localhost:8999/ping\\\": Read timed out; nested exception is java.net.SocketTimeoutException: Read timed out")
    assertThatJson(response.body).node("components.OAuthApiHealth.details.HttpStatus").isEqualTo("OK")
    assertThatJson(response.body).node("components.caseNotesApiHealth.details.HttpStatus").isEqualTo("OK")
    assertThatJson(response.body).node("status").isEqualTo("DOWN")
    assertThat(response.statusCodeValue).isEqualTo(503)
  }

  private fun subPing(status: Int) {
    elite2MockServer.stubFor(get("/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))

    oauthMockServer.stubFor(get("/auth/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))

    caseNotesMockServer.stubFor(get("/ping").willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBody(if (status == 200) "pong" else "some error")
        .withStatus(status)))
  }

  private fun subPingWithDelay(status: Int) {
    elite2MockServer.stubFor(get("/ping").willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(if (status == 200) "pong" else "some error")
            .withStatus(status)
            .withFixedDelay(1000)))

    oauthMockServer.stubFor(get("/auth/ping").willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(if (status == 200) "pong" else "some error")
            .withStatus(status)))

    caseNotesMockServer.stubFor(get("/ping").willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(if (status == 200) "pong" else "some error")
            .withStatus(status)))
  }
}
