package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.nhaarman.mockito_kotlin.whenever
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.assertj.JsonAssertions.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository

class CourtIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var videoLinkAppointmentRepository: VideoLinkAppointmentRepository

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
        restTemplate.exchange("/court/add-video-link-appointment", HttpMethod.POST, createHeaderEntity(mapOf(
            "bookingId" to bookingId,
            "court" to "Test Court 1",
            "locationId" to 1,
            "comment" to "test",
            "startTime" to "2019-10-10T10:00:00",
            "endTime" to "2019-10-10T10:00:00"
        )))

    assertThat(response.statusCode.value()).isEqualTo(201)

    elite2MockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/api/bookings/$bookingId/appointments"))
        .withRequestBody(WireMock.equalToJson(gson.toJson(mapOf(
            "appointmentType" to "VLB",
            "locationId" to 1,
            "comment" to "test",
            "startTime" to "2019-10-10T10:00",
            "endTime" to "2019-10-10T10:00"
        )))))
  }

  @Test
  fun `should return video link appointment by appointment id`() {
    whenever(videoLinkAppointmentRepository.findVideoLinkAppointmentByAppointmentIdIn(setOf(1L)))
        .thenReturn(setOf (
            VideoLinkAppointment(
                id = 1L,
                bookingId = 1L,
                appointmentId = 1L,
                hearingType = HearingType.PRE,
                court = "York"
            )
        ))

    val response: ResponseEntity<String> =
        restTemplate.exchange("/court/video-link-appointments", HttpMethod.POST,
            createHeaderEntity(setOf(1L))
        )

    assertThatJsonFileAndStatus(response, 200, "courtAppointments.json")
  }

  @Test
  fun `should return a bad request on invalid court`() {
    val response: ResponseEntity<String> =
        restTemplate.exchange("/court/add-video-link-appointment", HttpMethod.POST, createHeaderEntity(mapOf(
            "bookingId" to 1,
            "court" to "Marks",
            "startTime" to "2019-10-10T10:00:00",
            "endTime" to "2019-10-10T10:00:00",
            "locationId" to 1,
            "comment" to "test"
        )))

    assertThat(response.statusCode.value()).isEqualTo(400)
    assertThatJson(response.body).node("userMessage").isEqualTo("Invalid court location")
  }

  @Test
  fun `Validate date format for start and time`() {
    val response: ResponseEntity<String> =
        restTemplate.exchange("/court/add-video-link-appointment", HttpMethod.POST, createHeaderEntity(mapOf(
            "bookingId" to 1,
            "court" to "Test Court 1",
            "startTime" to "10-10-2029T10:00:00",
            "endTime" to "10-10-2019T10:00:00",
            "locationId" to 1,
            "comment" to "test"
        )))

    assertThat(response.statusCode.value()).isEqualTo(400)
  }
}
