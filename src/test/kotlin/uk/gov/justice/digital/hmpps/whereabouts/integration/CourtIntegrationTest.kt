package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import java.time.LocalDateTime
import java.util.*

class CourtIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var videoLinkAppointmentRepository: VideoLinkAppointmentRepository

  @MockBean
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

  @Autowired
  lateinit var objectMapper: ObjectMapper

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

  @Nested
  inner class GetAppointment {

    val videoBookingId: Long = 1
    val preAppointmentId: Long = 2
    val mainAppointmentId: Long = 3
    val postAppointmentId: Long = 4

    val preAppointment = PrisonAppointment(
      bookingId = 100,
      eventId = preAppointmentId,
      startTime = LocalDateTime.of(2020, 12, 2, 12, 0, 0),
      endTime = LocalDateTime.of(2020, 12, 2, 13, 0, 0),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 10,
      comment = "any comment"
    )

    val mainAppointment = PrisonAppointment(
      bookingId = 100,
      eventId = mainAppointmentId,
      startTime = LocalDateTime.of(2020, 12, 2, 13, 0, 0),
      endTime = LocalDateTime.of(2020, 12, 2, 14, 0, 0),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 9,
      comment = "any comment",
    )

    val postAppointment = PrisonAppointment(
      bookingId = 100,
      eventId = postAppointmentId,
      startTime = LocalDateTime.of(2020, 12, 2, 14, 0, 0),
      endTime = LocalDateTime.of(2020, 12, 2, 15, 0, 0),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 5,
      comment = "any comment"
    )

    val theVideoLinkBooking = VideoLinkBooking(
      id = videoBookingId,
      pre = VideoLinkAppointment(
        id = 10,
        bookingId = 100,
        appointmentId = preAppointmentId,
        court = "Test Court 1",
        hearingType = HearingType.PRE
      ),
      main = VideoLinkAppointment(
        id = 11,
        bookingId = 100,
        appointmentId = mainAppointmentId,
        court = "Test Court 2",
        hearingType = HearingType.MAIN
      ),
      post = VideoLinkAppointment(
        id = 12,
        bookingId = 100,
        appointmentId = postAppointmentId,
        court = "Test Court 1",
        hearingType = HearingType.POST
      ),
    )

    @Test
    fun `should get booking`() {

      whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(theVideoLinkBooking))

      prisonApiMockServer.stubGetPrisonAppointment(
        preAppointmentId,
        objectMapper.writeValueAsString(
          preAppointment
        )
      )

      prisonApiMockServer.stubGetPrisonAppointment(
        mainAppointmentId,
        objectMapper.writeValueAsString(
          mainAppointment
        )
      )

      prisonApiMockServer.stubGetPrisonAppointment(
        postAppointmentId,
        objectMapper.writeValueAsString(
          postAppointment
        )
      )

      webTestClient.get()
        .uri("/court/video-link-bookings/$videoBookingId")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(loadJsonFile("videoBooking.json"))
    }

    @Test
    fun `should get booking when only main appointment exists`() {

      whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(theVideoLinkBooking))


      prisonApiMockServer.stubGetPrisonAppointment(
        mainAppointmentId,
        objectMapper.writeValueAsString(
          mainAppointment
        )
      )

      webTestClient.get()
        .uri("/court/video-link-bookings/$videoBookingId")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(loadJsonFile("videoBooking-mainOnly.json"))
    }


    @Test
    fun `should get booking when only pre and post appointments exist`() {

      whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(theVideoLinkBooking))

      prisonApiMockServer.stubGetPrisonAppointment(
        preAppointmentId,
        objectMapper.writeValueAsString(
          preAppointment
        )
      )

      prisonApiMockServer.stubGetPrisonAppointment(
        postAppointmentId,
        objectMapper.writeValueAsString(
          postAppointment
        )
      )

      webTestClient.get()
        .uri("/court/video-link-bookings/$videoBookingId")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isNotFound
    }
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
        madeByTheCourt = false,
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
          "madeByTheCourt" to false,
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

  @Test
  fun `Returns 404 when trying to delete a non-existent video link booking`() {
    val bookingId: Long = 1

    webTestClient.delete()
      .uri("/court/video-link-bookings/$bookingId")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.developerMessage").isEqualTo("Video link booking with id $bookingId not found")

  }

  @Test
  fun `Returns 204 when successfully deleting a booking`() {
    val videoBookingId: Long = 1
    val preAppointmentId: Long = 2
    val mainAppointmentId: Long = 3

    val theVideoLinkBooking = VideoLinkBooking(
      id = videoBookingId,
      pre = VideoLinkAppointment(
        id = 10,
        bookingId = 4,
        appointmentId = preAppointmentId,
        court = "Test Court 1",
        hearingType = HearingType.PRE
      ),
      main = VideoLinkAppointment(
        id = 11,
        bookingId = 4,
        appointmentId = mainAppointmentId,
        court = "Test Court 2",
        hearingType = HearingType.MAIN
      )
    )

    whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(theVideoLinkBooking))

    prisonApiMockServer.stubDeleteAppointment(preAppointmentId, 200)
    prisonApiMockServer.stubDeleteAppointment(mainAppointmentId, 404)

    webTestClient.delete()
      .uri("/court/video-link-bookings/$videoBookingId")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isNoContent

    prisonApiMockServer.verify(
      WireMock.deleteRequestedFor(WireMock.urlEqualTo("/api/appointments/${preAppointmentId}"))
    )
    prisonApiMockServer.verify(
      WireMock.deleteRequestedFor(WireMock.urlEqualTo("/api/appointments/${mainAppointmentId}"))
    )

    verify(videoLinkBookingRepository).deleteById(videoBookingId)
  }
}
