package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson

class OAuthMockServer : WireMockServer(8090) {
  private val gson = getGson()
  val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun stubGrantToken() {
    stubFor(
      WireMock.post(WireMock.urlEqualTo("/auth/oauth/token"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(gson.toJson(mapOf("access_token" to "ABCDE", "token_type" to "bearer"))),
        ),
    )
  }
  fun requestReceived(request: Request, response: Response) {
    log.info("WireMock request url: {}", request.url)
    log.info("WireMock request body: {}", request.bodyAsString)
    log.info("WireMock request path params: {}", request.pathParameters)
    log.info("WireMock request with headers: {}", request.headers)
  }
}
