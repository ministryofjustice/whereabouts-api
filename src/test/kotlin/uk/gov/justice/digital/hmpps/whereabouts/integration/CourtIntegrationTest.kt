package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository

class CourtIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var videoLinkAppointmentRepository: VideoLinkAppointmentRepository

  @MockBean
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

  @Test
  fun `should list all courts`() {
    webTestClient.get()
      .uri("/court/all-courts")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(loadJsonFile("courtLocations.json"))
  }

  @Test
  fun `should post an appointment to prison api`() {
    val bookingId: Long = 1

    prisonApiMockServer.stubAddAppointment(bookingId, eventId = 1)

    webTestClient.post()
      .uri("/court/add-video-link-appointment")
      .headers(setHeaders())
      .bodyValue(
        mapOf(
          "bookingId" to bookingId,
          "court" to "Test Court 1",
          "locationId" to 1,
          "comment" to "test",
          "startTime" to "2019-10-10T10:00:00",
          "endTime" to "2019-10-10T10:00:00"
        )
      )
      .exchange()
      .expectStatus().isCreated

    prisonApiMockServer.verify(
      WireMock.postRequestedFor(WireMock.urlEqualTo("/api/bookings/$bookingId/appointments"))
        .withRequestBody(
          WireMock.equalToJson(
            gson.toJson(
              mapOf(
                "appointmentType" to "VLB",
                "locationId" to 1,
                "comment" to "test",
                "startTime" to "2019-10-10T10:00",
                "endTime" to "2019-10-10T10:00"
              )
            )
          )
        )
    )
  }

  @Test
  fun `should return video link appointment by appointment id`() {
    whenever(videoLinkAppointmentRepository.findVideoLinkAppointmentByAppointmentIdIn(setOf(1L)))
      .thenReturn(
        setOf(
          VideoLinkAppointment(
            id = 1L,
            bookingId = 1L,
            appointmentId = 1L,
            hearingType = HearingType.PRE,
            court = "York"
          )
        )
      )

    webTestClient.post()
      .uri("/court/video-link-appointments")
      .headers(setHeaders())
      .bodyValue(setOf(1L))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(loadJsonFile("courtAppointments.json"))
  }

  @Test
  fun `Validate date format for start and time`() {
    webTestClient.post()
      .uri("/court/add-video-link-appointment")
      .headers(setHeaders())
      .bodyValue(
        mapOf(
          "bookingId" to 1,
          "court" to "Test Court 1",
          "locationId" to 1,
          "comment" to "test",
          "startTime" to "10-10-2029T10:00:00",
          "endTime" to "2019-10-10T10:00:00",
          "comment" to "test"
        )
      )
      .exchange()
      .expectStatus().is4xxClientError
  }

  @Test
  fun `Successful POST video-link-bookings, main appointment only`() {
    val bookingId = 1L
    val mainAppointmentId = 1L

    prisonApiMockServer.stubAddAppointment(bookingId, eventId = mainAppointmentId)

    val theVideoLinkBooking = VideoLinkBooking(
      main = VideoLinkAppointment(
        bookingId = bookingId,
        appointmentId = mainAppointmentId,
        court = "Test Court 1",
        hearingType = HearingType.MAIN
      )
    )

    whenever(videoLinkBookingRepository.save(any())).thenReturn(theVideoLinkBooking.copy(id = 1))

    webTestClient.post()
      .uri("/court/video-link-bookings")
      .headers(setHeaders())
      .bodyValue(
        mapOf(
          "bookingId" to bookingId,
          "court" to "Test Court 1",
          "main" to mapOf(
            "locationId" to 1,
            "startTime" to "2020-12-01T09:00",
            "endTime" to "2020-12-01T09:30"
          )
        )
      )
      .exchange()
      .expectStatus().isCreated
      .expectBody().json("1")

    verify(videoLinkBookingRepository).save(theVideoLinkBooking)
  }
}
