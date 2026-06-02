package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import uk.gov.justice.digital.hmpps.whereabouts.common.getGson
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApi
import java.time.LocalDate

class PrisonApiMockServer : WireMockServer(8999) {
  private val gson = getGson()

  fun stubUpdateAttendance(bookingId: Long = 1L, activityId: Long = 2L, status: Int = 201) {
    val updateAttendanceUrl = "/api/bookings/$bookingId/activities/$activityId/attendance?lockTimeout=true"

    stubFor(
      put(urlEqualTo(updateAttendanceUrl))
        .willReturn(
          aResponse()
            .withStatus(status),
        ),
    )
  }

  fun stubUpdateAttendanceForBookingIds() {
    val updateAttendanceUrl = "/api/bookings/activities/attendance"

    stubFor(
      put(urlPathEqualTo(updateAttendanceUrl))
        .willReturn(
          aResponse()
            .withStatus(201),
        ),
    )
  }

  fun stubDeleteAppointment(appointmentId: Long, status: Int) {
    val deleteAppointmentUrl = "/api/appointments/$appointmentId"

    stubFor(
      delete(urlPathEqualTo(deleteAppointmentUrl))
        .willReturn(
          aResponse()
            .withStatus(status),
        ),
    )
  }

  fun stubGetScheduledActivities(
    prisonId: String = "MDI",
    date: LocalDate = LocalDate.now(),
    period: TimePeriod = TimePeriod.AM,
  ) {
    stubFor(
      get(urlEqualTo("/api/schedules/$prisonId/activities?date=$date&timeSlot=$period"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              gson.toJson(
                listOf(
                  mapOf("bookingId" to 1L, "eventId" to 2L, "offenderNo" to "A123B"),
                  mapOf("bookingId" to 2L, "eventId" to 3L, "offenderNo" to "B123C"),
                ),
              ),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubGetScheduledAppointmentsByAgencyAndDate(agencyId: String, offenderNo: String = "A1234AA") {
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
                    "locationDescription" to "Res 1 Education",
                    "appointmentTypeCode" to "VLB",
                    "appointmentTypeDescription" to "Video Link Nooking",
                    "startTime" to "2020-12-25T09:00:00",
                    "endTime" to "2020-12-25T09:30:00",
                    "offenderNo" to offenderNo,
                    "firstName" to "BRUNO",
                    "lastName" to "BEYETTE",
                    "createUserId" to "ASMITH",
                  ),
                  mapOf(
                    "id" to 2L,
                    "agencyId" to agencyId,
                    "locationId" to 11L,
                    "locationDescription" to "Res 1 Education",
                    "appointmentTypeCode" to "MEH",
                    "appointmentTypeDescription" to "Video Link Nooking",
                    "startTime" to "2020-12-25T10:00:00",
                    "endTime" to "2020-12-25T10:30:00",
                    "offenderNo" to "B2345BB",
                    "firstName" to "BILL",
                    "lastName" to "BENN",
                    "createUserId" to "BSMITH",
                  ),
                ),
              ),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubGetScheduledActivitiesForEventIds(prisonId: String = "MDI") {
    stubFor(
      post(urlEqualTo("/api/schedules/$prisonId/activities-by-event-ids"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              gson.toJson(
                listOf(
                  mapOf("bookingId" to 1L, "eventId" to 1L),
                ),
              ),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubScheduleActivityCount(
    prisonId: String = "MDI",
  ) {
    stubFor(
      post(urlPathEqualTo("/api/schedules/$prisonId/count-activities"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              gson.toJson(
                mapOf("total" to 13L, "suspended" to 2L, "notRecorded" to 5L),
              ),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubGetAttendanceForOffender(
    offenderNo: String,
    fromDate: LocalDate = LocalDate.now().minusYears(1),
    toDate: LocalDate = LocalDate.now(),
    outcome: String? = null,
    page: String? = "0",
    size: String? = "10000",
  ) {
    val outcomeParam = if (outcome != null) "&outcome=$outcome" else ""
    val testUrl =
      "/api/offender-activities/$offenderNo/attendance-history?fromDate=$fromDate&toDate=$toDate$outcomeParam&page=$page&size=$size"

    stubFor(
      get(urlEqualTo(testUrl))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              """{
"content":[
  {"eventDate": "2021-05-04", "outcome": "ATT",    "prisonId":"MDI", "activity": "act 1", "description":"desc 1", "comment": "comment 1"},
  {"eventDate": "2021-05-04", "outcome": "ACCAB",  "prisonId":"MDI", "activity": "act 2", "description":"desc 2", "comment": "comment 2"},
  {"eventDate": "2021-06-04", "outcome": "UNACAB", "prisonId":"MDI",                      "description":"desc 3", "comment": "comment 3"},
  {"eventDate": "2021-06-05", "outcome": "ATT",    "prisonId":"WWI", "activity": "act 4", "description":"desc 4"},
  {"eventDate": "2021-07-14", "outcome": "UNACAB", "prisonId":"WWI", "activity": "act 5", "description":"desc 5", "comment": "comment 5"},
  {"eventDate": "2021-08-14",                      "prisonId":"WWI", "activity": "act 6", "description":"desc 6", "comment": "comment 6"}
], 
"pageable":{"pageNumber":0,"pageSize":10000},
"totalPages": 1
}""",
            )
            .withStatus(200),
        ),
    )
  }

  fun stubGetAttendanceForOffenderEmpty(
    offenderNo: String,
    fromDate: LocalDate = LocalDate.now().minusYears(1),
    toDate: LocalDate = LocalDate.now(),
    outcome: String? = null,
    page: String? = "0",
    size: String? = "10000",
  ) {
    val outcomeParam = if (outcome != null) "&outcome=$outcome" else ""
    val testUrl =
      "/api/offender-activities/$offenderNo/attendance-history?fromDate=$fromDate&toDate=$toDate$outcomeParam&page=$page&size=$size"

    stubFor(
      get(urlEqualTo(testUrl))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              """{
"content":[],
"pageable":{"pageNumber":0,"pageSize":10000},
"totalPages": 0
}""",
            )
            .withStatus(200),
        ),
    )
  }

  fun stubGetBooking(offenderNo: String = "AB1234C", bookingId: Long = 1) {
    stubFor(
      get(urlEqualTo("/api/bookings/$bookingId?basicInfo=true"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(gson.toJson(mapOf("offenderNo" to offenderNo)))
            .withStatus(200),
        ),
    )
  }

  fun stubGetOffenderBookings(offenderNo: String = "A1234AA", offenderLocationDescription: String = "MDI-1-2", isActive: Boolean) {
    stubFor(
      post(urlEqualTo("/api/bookings/offenders?activeOnly=$isActive"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              gson.toJson(
                listOf(
                  mapOf(
                    "bookingId" to 22L,
                    "bookingNo" to "123",
                    "offenderNo" to offenderNo,
                    "firstName" to "A",
                    "lastName" to "Name",
                    "agencyId" to "MDI",
                    "dateOfBirth" to "2000-01-01",
                    "assignedLivingUnitId" to 44L,
                    "assignedLivingUnitDesc" to offenderLocationDescription,
                  ),
                ),
              ),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubAddAppointment(response: List<Map<String, Any>>? = null) {
    stubFor(
      post(urlEqualTo("/api/appointments"))
        .willReturn(
          aResponse()
            .withHeader("Content-type", "application/json")
            .withBody(
              gson.toJson(response),
            )
            .withStatus(201),
        ),
    )
  }

  fun stubMakeCellMove(
    bookingId: Long,
    internalLocationDescription: String,
    assignedLivingUnitId: Long,
    agencyId: String,
    bedAssignmentHistorySequence: Int,
    lockTimeout: Boolean,
  ) {
    stubFor(
      put(urlEqualTo("/api/bookings/$bookingId/living-unit/$internalLocationDescription?lockTimeout=$lockTimeout&reasonCode=ADM"))
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
                  "bedAssignmentHistorySequence" to bedAssignmentHistorySequence,
                ),
              ),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubGetPrisonAppointment(appointmentId: Long, responseJson: String) {
    stubFor(
      get(urlPathEqualTo("/api/appointments/$appointmentId"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson)
            .withStatus(200),
        ),
    )
  }

  fun stubGetAgencies() {
    val agency = PrisonApi.Agency()
    agency.agencyId = "IWI"
    val agency2 = PrisonApi.Agency()
    agency2.agencyId = "WDI"
    val response = listOf(agency, agency2)

    stubFor(
      get(urlPathEqualTo("/api/agencies/prisons"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(ObjectMapper().writeValueAsString(response))
            .withStatus(200),
        ),
    )
  }

  fun stubDeleteAppointments(appointmentIds: List<Int>) {
    val deleteAppointmentUrl = "/api/appointments/delete"

    stubFor(
      post(urlPathEqualTo(deleteAppointmentUrl))
        .withRequestBody(equalToJson(gson.toJson(appointmentIds)))
        .willReturn(
          aResponse()
            .withStatus(204),
        ),
    )
  }

  fun stubGetEvents(json: String, httpStatus: Int = 200) {
    stubFor(
      get(urlPathMatching("/api/offenders/.*/scheduled-events"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(httpStatus)
            .withBody(json),
        ),
    )
  }
}
