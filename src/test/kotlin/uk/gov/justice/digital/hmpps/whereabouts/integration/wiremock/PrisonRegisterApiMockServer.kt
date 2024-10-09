package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

class PrisonRegisterApiMockServer : WireMockServer(8094) {

  fun stubGetPrisonEmailAddress(agencyId: String) {
    stubFor(
      get(urlEqualTo("/secure/prisons/id/$agencyId/department/contact-details?departmentType=VIDEOLINK_CONFERENCING_CENTRE"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "type": "VIDEOLINK_CONFERENCING_CENTRE",
                "emailAddress": "someEmailAddress@gov.uk",
                "phoneNumber": null,
                "webAddress": null
               }
              """,
            )
            .withStatus(200),
        ),
    )
  }

  fun stubGetPrisonEmailAddressReturnsNotFound(agencyId: String) {
    stubFor(
      get(urlEqualTo("/secure/prisons/id/$agencyId/department/contact-details?departmentType=VIDEOLINK_CONFERENCING_CENTRE"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(404),
        ),
    )
  }
}
