package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson
import uk.gov.justice.digital.hmpps.whereabouts.dto.elite.CaseNoteDto

class Elite2MockServer : WireMockRule(8999) {
    private val gson = getGson()

    fun stubUpdateAttendance(bookingId: Long = 1L, activityId: Long = 2L) {
        val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"

        this.stubFor(
                WireMock.put(WireMock.urlPathEqualTo(updateAttendanceUrl))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200))
        )
    }

    fun stubUpdateAttendanceForBookingIds() {
        val updateAttendanceUrl = "/api/bookings/activities/attendance"

        this.stubFor(
                WireMock.put(WireMock.urlPathEqualTo(updateAttendanceUrl))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200))
        )
    }

    fun stubCreateCaseNote(bookingId: Long = 1L, caseNoteId: Long = 100L) {
        val createCaseNote = "/api/bookings/$bookingId/caseNotes"
        this.stubFor(
                WireMock.post(WireMock.urlPathEqualTo(createCaseNote))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(gson.toJson(CaseNoteDto.builder().caseNoteId(caseNoteId).build()))
                                .withStatus(201))
        )
    }

    fun stubCaseNoteAmendment(bookingId: Long  = 1L, caseNoteId: Long = 3) {
        val updateCaseNote = "/api/bookings/$bookingId/caseNotes/$caseNoteId"
        this.stubFor(
                WireMock.put(WireMock.urlPathEqualTo(updateCaseNote))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(201))
        )
    }
}
