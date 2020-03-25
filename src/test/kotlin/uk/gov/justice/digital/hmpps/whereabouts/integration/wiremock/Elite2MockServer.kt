package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate

class Elite2MockServer : WireMockServer(8999) {
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

    stubFor(get(urlEqualTo("/api/schedules/$prisonId/activities-by-date-range?fromDate=$fromDate&toDate=$toDate&timeSlot=$periodText&includeSuspended=true"))
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
    stubFor(get(urlEqualTo("/api/agencies/$agencyId/locations/groups"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(listOf(
                LocationGroup(key = "A", name = "Block A")
            )))
            .withStatus(200)
        )
    )
  }

  fun stubGetAgencyLocationGroupsNotFound(agencyId: String) {
    stubFor(get(urlEqualTo("/api/agencies/$agencyId/locations/groups"))
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
    stubFor(get(urlEqualTo("/api/agencies/$agencyId/locations/groups"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(
                ErrorResponse(500, null, "Server error", "Server error", null)
            ))
            .withStatus(500)
        )
    )
  }

  fun stubAgencyLocationsByType(agencyId: String, locationType: String, locations: List<Location>) {
    stubFor(get(urlEqualTo("/api/agencies/$agencyId/locations/type/$locationType"))
        .willReturn(aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(gson.toJson(locations.map { it.toMap() }))
            .withStatus(200)
        )
    )
  }

  // Null values are included by gson marshalled to JSON, so use a map to emulate to omit them
  private fun Location.toMap(): Map<String, String> {
    val locationMap = mutableMapOf(
        "agencyId" to this.agencyId,
        "currentOccupancy" to "${this.currentOccupancy}",
        "description" to this.description,
        "locationId" to "${this.locationId}",
        "locationPrefix" to this.locationPrefix,
        "locationType" to this.locationType,
        "operationalCapacity" to "${this.operationalCapacity}",
        "internalLocationCode" to this.internalLocationCode)
    if (this.userDescription != null) locationMap["userDescription"] = this.userDescription as String
    if (this.locationUsage != null) locationMap["locationUsage"] = this.locationUsage as String
    if (this.parentLocationId != null) locationMap["parentLocationId"] = "${this.parentLocationId}"
    return locationMap.toMap()
  }

  fun stubGetAgencyLocationsByTypeNotFound(agencyId: String, locationType: String) {
    stubFor(get(urlEqualTo("/api/agencies/$agencyId/locations/type/$locationType"))
        .willReturn(aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(gson.toJson(listOf(
                ErrorResponse(404, null, "Locations of type [$locationType] in agency [$agencyId] not found", "Locations of type [$locationType] in agency [$agencyId] not found", null)
            )))
            .withStatus(404)
        )
    )
  }

  fun stubGetAgencyLocationsByTypeServerError(agencyId: String, locationType: String) {
    stubFor(get(urlEqualTo("/api/agencies/$agencyId/locations/type/$locationType"))
        .willReturn(aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(gson.toJson(listOf(
                ErrorResponse(500, null, "Server error", "Server error", null)
            )))
            .withStatus(500)
        )
    )
  }

  fun stubAddAppointment(bookingId: Long, eventId: Long = 1) {
    stubFor(post(urlEqualTo("/api/bookings/$bookingId/appointments"))
        .willReturn(aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(gson.toJson(
                Event(eventId = eventId)
            ))
            .withStatus(201)
        ))
  }
}
