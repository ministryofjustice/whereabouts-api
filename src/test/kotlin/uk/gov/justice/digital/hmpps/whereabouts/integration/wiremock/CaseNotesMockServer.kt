package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.CaseNoteDto

class CaseNotesMockServer : WireMockServer(8093) {
  private val gson = getGson()

  fun stubCreateCaseNote(offenderNo: String = "AB1234C", caseNoteId: Long = 100L) {
    val createCaseNote = "/case-notes/$offenderNo"
    stubFor(
      post(urlPathEqualTo(createCaseNote))
        .withHeader("CaseloadId", equalTo("***"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(CaseNoteDto.builder().legacyId(caseNoteId).build()))
            .withStatus(201),
        ),
    )
  }

  fun stubCaseNoteAmendment(offenderNo: String = "AB1234C", caseNoteId: Long = 3) {
    val updateCaseNote = "/case-notes/$offenderNo/$caseNoteId"
    stubFor(
      put(urlPathEqualTo(updateCaseNote))
        .withHeader("CaseloadId", equalTo("***"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(201),
        ),
    )
  }
}
