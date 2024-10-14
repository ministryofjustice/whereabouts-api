package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import uk.gov.justice.digital.hmpps.whereabouts.utils.loadJson

class LocationApiMockServer : WireMockServer(8095) {

  fun stubGetAllLocationForPrison(prisonId: String = "WSI") {
    stubFor(
      get(urlEqualTo("/locations/prison/$prisonId"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("WSI_location_api-response".loadJson(this))
            .withStatus(200),
        ),
    )
  }
}
