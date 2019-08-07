package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson
import uk.gov.justice.digital.hmpps.whereabouts.dto.elite.CaseNoteDto
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate

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

    fun stubGetScheduledActivities(prisonId: String = "MDI", date: LocalDate = LocalDate.now(), period: TimePeriod = TimePeriod.AM) {
        this.stubFor(
                WireMock.get(
                        WireMock.urlEqualTo("/api/schedules/$prisonId/activities?date=$date&timeSlot=$period"))
                                .willReturn(WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(gson.toJson(listOf(
                                                mapOf("bookingId" to 1L),
                                                mapOf("bookingId" to 2L)))
                                        )
                                        .withStatus(200))
        )
    }
}
