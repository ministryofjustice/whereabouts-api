package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event
import uk.gov.justice.digital.hmpps.whereabouts.model.CellAttribute
import uk.gov.justice.digital.hmpps.whereabouts.model.CellWithAttributes
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import wiremock.org.eclipse.jetty.http.HttpStatus
import java.time.LocalDate

class PrisonApiMockServer : WireMockServer(8999) {
  private val gson = getGson()

  fun stubUpdateAttendance(bookingId: Long = 1L, activityId: Long = 2L) {
    val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance"

    stubFor(
      put(urlPathEqualTo(updateAttendanceUrl))
        .willReturn(
          aResponse()
            .withStatus(200)
        )
    )
  }

  fun stubUpdateAttendanceForBookingIds() {
    val updateAttendanceUrl = "/api/bookings/activities/attendance"

    stubFor(
      put(urlPathEqualTo(updateAttendanceUrl))
        .willReturn(
          aResponse()
            .withStatus(200)
        )
    )
  }

  fun stubDeleteAppointment(appointmentId: Long, status: Int) {
    val deleteAppointmentUrl = "/api/appointments/$appointmentId"

    stubFor(
      delete(urlPathEqualTo(deleteAppointmentUrl))
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )
  }

  fun stubUpdateAppointmentComment(appointmentId: Long, status: Int = HttpStatus.NO_CONTENT_204) {
    val updateAppointmentUrl = "/api/appointments/$appointmentId/comment"

    stubFor(
      put(urlPathEqualTo(updateAppointmentUrl))
        .willReturn(
          aResponse().withStatus(status)
        )
    )
  }

  fun stubGetScheduledActivities(
    prisonId: String = "MDI",
    date: LocalDate = LocalDate.now(),
    period: TimePeriod = TimePeriod.AM
  ) {
    stubFor(
      get(urlEqualTo("/api/schedules/$prisonId/activities?date=$date&timeSlot=$period"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              gson.toJson(
                listOf(
                  mapOf("bookingId" to 1L),
                  mapOf("bookingId" to 2L)
                )
              )
            )
            .withStatus(200)
        )
    )
  }

  fun stubGetScheduledAppointmentsByAgencyAndDate(agencyId: String) {
    stubFor(
      get(urlEqualTo("/api/schedules/$agencyId/appointments?date=2020-12-25"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              gson.toJson(
                listOf(
                  mapOf(
                    "id" to 1L,
                    "agencyId" to agencyId,
                    "locationId" to 10L,
                    "appointmentTypeCode" to "VLB",
                    "startTime" to "2020-12-25T09:00:00",
                    "endTime" to "2020-12-25T09:30:00"
                  ),
                  mapOf(
                    "id" to 2L,
                    "agencyId" to agencyId,
                    "locationId" to 11L,
                    "appointmentTypeCode" to "MEH",
                    "startTime" to "2020-12-25T10:00:00",
                    "endTime" to "2020-12-25T10:30:00"
                  )
                )
              )
            )
            .withStatus(200)
        )
    )
  }

  fun stubGetScheduledActivitiesForDateRange(
    prisonId: String = "MDI",
    fromDate: LocalDate = LocalDate.now(),
    toDate: LocalDate = LocalDate.now(),
    period: TimePeriod? = TimePeriod.AM,
    suspended: Boolean = false
  ) {
    val periodText = period?.toString().orEmpty()
    var testUrl =
      "/api/schedules/$prisonId/activities-by-date-range?fromDate=$fromDate&toDate=$toDate&timeSlot=$periodText"
    if (suspended) {
      testUrl += "&includeSuspended=true"
    }

    stubFor(
      get(urlEqualTo(testUrl))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              gson.toJson(
                listOf(
                  mapOf("bookingId" to 1L, "eventId" to 1L),
                  mapOf("bookingId" to 2L, "eventId" to 12L)
                )
              )
            )
            .withStatus(200)
        )
    )
  }

  fun stubGetBooking(offenderNo: String = "AB1234C", bookingId: Long = 1) {
    stubFor(
      get(urlEqualTo("/api/bookings/$bookingId?basicInfo=true"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(mapOf("offenderNo" to offenderNo)))
            .withStatus(200)
        )
    )
  }

  fun stubGetAgencyLocationGroups(agencyId: String) {
    stubFor(
      get(urlEqualTo("/api/agencies/$agencyId/locations/groups"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              gson.toJson(
                listOf(
                  LocationGroup(key = "A", name = "Block A")
                )
              )
            )
            .withStatus(200)
        )
    )
  }

  fun stubGetAgencyLocationGroupsNotFound(agencyId: String) {
    stubFor(
      get(urlEqualTo("/api/agencies/$agencyId/locations/groups"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              gson.toJson(
                ErrorResponse(
                  404,
                  null,
                  "Resource with id [$agencyId] not found",
                  "Resource with id [$agencyId] not found",
                  null
                )
              )
            )
            .withStatus(404)
        )
    )
  }

  fun stubGetAgencyLocationGroupsServerError(agencyId: String) {
    stubFor(
      get(urlEqualTo("/api/agencies/$agencyId/locations/groups"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              gson.toJson(
                ErrorResponse(500, null, "Server error", "Server error", null)
              )
            )
            .withStatus(500)
        )
    )
  }

  fun stubAgencyLocationsByType(agencyId: String, locationType: String, locations: List<Location>) {
    stubFor(
      get(urlEqualTo("/api/agencies/$agencyId/locations/type/$locationType"))
        .willReturn(
          aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(gson.toJson(locations.map { it.toMap() }))
            .withStatus(200)
        )
    )
  }

  fun stubCellsWithCapacityNoAttribute(agencyId: String, cells: List<CellWithAttributes>) {
    stubFor(
      get(urlEqualTo("/api/agencies/$agencyId/cellsWithCapacity"))
        .willReturn(
          aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(gson.toJson(cells.map { it.toMap() }))
            .withStatus(200)
        )
    )
  }

  fun stubCellsWithCapacityWithAttribute(agencyId: String, cells: List<CellWithAttributes>, attribute: String) {
    stubFor(
      get(urlEqualTo("/api/agencies/$agencyId/cellsWithCapacity?attribute=$attribute"))
        .willReturn(
          aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(gson.toJson(cells.map { it.toMap() }))
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
      "internalLocationCode" to this.internalLocationCode
    )
    if (this.userDescription != null) locationMap["userDescription"] = this.userDescription as String
    if (this.locationUsage != null) locationMap["locationUsage"] = this.locationUsage as String
    if (this.parentLocationId != null) locationMap["parentLocationId"] = "${this.parentLocationId}"
    return locationMap.toMap()
  }

  private fun CellWithAttributes.toMap(): Map<String, Any?> {
    val cellMap = mutableMapOf(
      "id" to this.id,
      "description" to this.description,
      "userDescription" to this.userDescription,
      "noOfOccupants" to "${this.noOfOccupants}",
      "capacity" to "${this.capacity}",
      "attributes" to listOf<CellAttribute>()
    )
    return cellMap.toMap()
  }

  fun stubGetAgencyLocationsByTypeNotFound(agencyId: String, locationType: String) {
    stubFor(
      get(urlEqualTo("/api/agencies/$agencyId/locations/type/$locationType"))
        .willReturn(
          aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(
              gson.toJson(
                listOf(
                  ErrorResponse(
                    404,
                    null,
                    "Locations of type [$locationType] in agency [$agencyId] not found",
                    "Locations of type [$locationType] in agency [$agencyId] not found",
                    null
                  )
                )
              )
            )
            .withStatus(404)
        )
    )
  }

  fun stubGetAgencyLocationsByTypeServerError(agencyId: String, locationType: String) {
    stubFor(
      get(urlEqualTo("/api/agencies/$agencyId/locations/type/$locationType"))
        .willReturn(
          aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(
              gson.toJson(
                listOf(
                  ErrorResponse(500, null, "Server error", "Server error", null)
                )
              )
            )
            .withStatus(500)
        )
    )
  }

  fun stubAddAppointment(bookingId: Long, eventId: Long = 1) {
    stubFor(
      post(urlEqualTo("/api/bookings/$bookingId/appointments"))
        .willReturn(
          aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(
              gson.toJson(
                Event(eventId = eventId, agencyId = "WWI")
              )
            )
            .withStatus(201)
        )
    )
  }

  fun stubMakeCellMove(
    bookingId: Long,
    internalLocationDescription: String,
    assignedLivingUnitId: Long,
    agencyId: String,
    bedAssignmentHistorySequence: Int
  ) {
    stubFor(
      put(urlPathEqualTo("/api/bookings/$bookingId/living-unit/$internalLocationDescription"))
        .willReturn(
          aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(
              gson.toJson(
                mapOf(
                  "bookingId" to bookingId,
                  "agencyId" to agencyId,
                  "assignedLivingUnitDesc" to internalLocationDescription,
                  "assignedLivingUnitId" to assignedLivingUnitId,
                  "bedAssignmentHistorySequence" to bedAssignmentHistorySequence
                )
              )
            )
            .withStatus(200)
        )
    )
  }

  fun stubGetPrisonAppointment(appointmentId: Long, responseJson: String) {
    stubFor(
      get(urlPathEqualTo("/api/appointments/$appointmentId"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson)
            .withStatus(200)
        )
    )
  }

  fun stubGetPrisonAppointmentNotFound(appointmentId: Long) {
    stubFor(get(urlPathEqualTo("/api/appointments/$appointmentId")).willReturn(aResponse().withStatus(404)))
  }

  fun stubGetLocation(locationId: Long, locationType: String = "VIDE") {
    stubFor(
      get(urlPathEqualTo("/api/locations/$locationId"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(
              gson.toJson(
                mapOf(
                  "locationId" to locationId,
                  "description" to "Location for id $locationId",
                  "locationType" to locationType,
                  "agencyId" to "WWI"
                )
              )
            )
        )
    )
  }
}
