package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity


class HealthCheckIntegrationTest : IntegrationTest() {
    companion object {
        @get:ClassRule
        @JvmStatic
        val elite2MockServer = WireMockRule(8999)

        @get:ClassRule
        @JvmStatic
        val oauthMockServer = WireMockRule(8090)
    }

    @Test
    fun `Health page reports ok` () {

        elite2MockServer.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/ping"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withBody("pong"))
        )

        oauthMockServer.stubFor(
                WireMock.get(WireMock.urlPathEqualTo("/auth/ping"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withBody("pong"))
        )

        val response: ResponseEntity<String> =
                restTemplate.exchange("/health", HttpMethod.GET, createHeaderEntity("headers"))

        val body = gson.fromJson(response.body, Map::class.java)

        assertThat(body["status"]).isEqualTo("UP")
    }
}
