package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.junit.WireMockRule
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson

class OAuthMockServer : WireMockRule(8090) {
  private val gson = getGson()

  fun stubGrantToken() {
    stubFor(
        WireMock.post(WireMock.urlEqualTo("/auth/oauth/token"))
            .willReturn(WireMock.aResponse()
                .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
                .withBody(gson.toJson(mapOf("access_token" to "ABCDE"))))
    )
  }
}
