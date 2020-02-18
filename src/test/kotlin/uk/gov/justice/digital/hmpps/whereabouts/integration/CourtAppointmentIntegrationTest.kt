package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateCourtAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.CourtAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.repository.CourtAppointmentRepository

class CourtAppointmentIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var courtAppointmentRepository: CourtAppointmentRepository

  @Test
  fun `should list all courts`() {
    val response: ResponseEntity<String> =
        restTemplate.exchange("/court/all-courts", HttpMethod.GET, createHeaderEntity(""))

    assertThatJsonFileAndStatus(response, 200, "courtLocations.json")
  }

  @Test
  fun `should post an appointment to elite2`() {
    val bookingId: Long = 1

    elite2MockServer.stubAddAppointment(bookingId, eventId = 1)

    val response: ResponseEntity<String> =
        restTemplate.exchange("/court/add-court-appointment", HttpMethod.POST, createHeaderEntity(CreateCourtAppointment(
            bookingId = bookingId,
            court = "York Crown Court",
            startTime = "2019-10-10T10:00:00",
            endTime = "2019-10-10T10:00:00",
            locationId = 1,
            comment = "test"
        )))

    assertThat(response.statusCode.value()).isEqualTo(201)

    elite2MockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/api/bookings/$bookingId/appointments"))
        .withRequestBody(WireMock.equalToJson(gson.toJson(mapOf(
            "appointmentType" to "VLB",
            "startTime" to "2019-10-10T10:00:00",
            "endTime" to "2019-10-10T10:00:00",
            "locationId" to 1,
            "comment" to "test"
        )))))
  }

  @Test
  fun `should return court appointment by appointment id`() {
    whenever(courtAppointmentRepository.findCourtAppointmentByAppointmentIdIn(setOf(1L)))
        .thenReturn(setOf (
            CourtAppointment(
                id = 1L,
                bookingId = 1L,
                appointmentId = 1L,
                hearingType = HearingType.PRE,
                court = "York"
            )
        ))

    val response: ResponseEntity<String> =
        restTemplate.exchange("/court/court-appointments", HttpMethod.POST,
            createHeaderEntity(setOf(1L))
        )

    assertThatJsonFileAndStatus(response, 200, "courtAppointments.json")
  }
}
