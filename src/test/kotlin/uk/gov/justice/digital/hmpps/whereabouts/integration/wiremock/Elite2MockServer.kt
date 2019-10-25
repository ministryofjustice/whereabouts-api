package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit.WireMockRule
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate

class Elite2MockServer : WireMockRule(8999) {
  private val gson = getGson()

  fun stubUpdateAttendance(bookingId: Long = 1L, activityId: Long = 2L) {
    val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"

    stubFor(put(urlPathEqualTo(updateAttendanceUrl))
        .willReturn(aResponse()
            .withStatus(200))
    )
  }

  fun stubUpdateAttendanceForBookingIds() {
    val updateAttendanceUrl = "/api/bookings/activities/attendance"

    stubFor(put(urlPathEqualTo(updateAttendanceUrl))
        .willReturn(aResponse()
            .withStatus(200))
    )
  }

  fun stubGetScheduledActivities(prisonId: String = "MDI", date: LocalDate = LocalDate.now(), period: TimePeriod = TimePeriod.AM) {
    stubFor(get(urlEqualTo("/api/schedules/$prisonId/activities?date=$date&timeSlot=$period"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(listOf(
                mapOf("bookingId" to 1L),
                mapOf("bookingId" to 2L)))
            )
            .withStatus(200))
    )
  }

  fun stubGetScheduledActivitiesForDateRange(prisonId: String = "MDI", fromDate: LocalDate = LocalDate.now(), toDate: LocalDate = LocalDate.now(), period: TimePeriod = TimePeriod.AM) {
    stubFor(get(urlEqualTo("/api/schedules/$prisonId/activities-by-date-range?fromDate=$fromDate&toDate=$toDate&timeSlot=$period"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(listOf(
                mapOf("bookingId" to 1L),
                mapOf("bookingId" to 2L)))
            )
            .withStatus(200))
    )
  }

  fun stubGetBooking(offenderNo: String = "AB1234C") {
    stubFor(get(urlEqualTo("/api/bookings/1?basicInfo=true"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(mapOf("offenderNo" to offenderNo)))
            .withStatus(200))
    )
  }
}
