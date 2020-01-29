package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit.WireMockRule
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup
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

  fun stubGetScheduledActivitiesForDateRange(prisonId: String = "MDI", fromDate: LocalDate = LocalDate.now(), toDate: LocalDate = LocalDate.now(), period: TimePeriod? = TimePeriod.AM) {
    val periodText = period?.toString().orEmpty()

    stubFor(get(urlEqualTo("/api/schedules/$prisonId/activities-by-date-range?fromDate=$fromDate&toDate=$toDate&timeSlot=$periodText"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(listOf(
                mapOf("bookingId" to 1L, "eventId" to 1L),
                mapOf("bookingId" to 2L, "eventId" to 12L))))
            .withStatus(200))
    )
  }

  fun stubGetBooking(offenderNo: String = "AB1234C", bookingId: Long = 1) {
    stubFor(get(urlEqualTo("/api/bookings/$bookingId?basicInfo=true"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(mapOf("offenderNo" to offenderNo)))
            .withStatus(200))
    )
  }

  fun stubGetAgencyLocationGroups(agencyId: String) {
    stubFor(get(urlEqualTo("/api/agencies/$agencyId/locations/groupsNew"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(listOf(
                LocationGroup.builder().key("A").name("Block A").build()
            )))
            .withStatus(200)
        )
    )
  }

  fun stubGetAgencyLocationGroupsNotFound(agencyId: String) {
    stubFor(get(urlEqualTo("/api/agencies/$agencyId/locations/groupsNew"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(
                ErrorResponse(404, null, "Resource with id [$agencyId] not found", "Resource with id [$agencyId] not found", null)
            ))
            .withStatus(404)
        )
    )
  }

  fun stubGetAgencyLocationGroupsServerError(agencyId: String) {
    stubFor(get(urlEqualTo("/api/agencies/$agencyId/locations/groupsNew"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(
                ErrorResponse(500, null, "Server error", "Server error", null)
            ))
            .withStatus(500)
        )
    )
  }
}
