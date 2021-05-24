package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.microsoft.applicationinsights.TelemetryClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.time.temporal.ChronoUnit
import java.util.Optional

class CourtIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var videoLinkAppointmentRepository: VideoLinkAppointmentRepository

  @MockBean
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

  @MockBean
  lateinit var telemetryClient: TelemetryClient

  val tomorrow: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1)
  val yesterday: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(1)
  val referenceTime: LocalDateTime = tomorrow.plusHours(9)

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
      startTime = referenceTime,
      endTime = referenceTime.plusMinutes(30),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 10,
      comment = "any comment"
    )

    val mainAppointment = PrisonAppointment(
      bookingId = 100,
      eventId = mainAppointmentId,
      startTime = referenceTime.plusMinutes(30),
      endTime = referenceTime.plusMinutes(60),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 9,
      comment = "any comment",
    )

    val postAppointment = PrisonAppointment(
      bookingId = 100,
      eventId = postAppointmentId,
      startTime = referenceTime.plusMinutes(60),
      endTime = referenceTime.plusMinutes(90),
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
        courtId = "TSTCRT1",
        hearingType = HearingType.PRE
      ),
      main = VideoLinkAppointment(
        id = 11,
        bookingId = 100,
        appointmentId = mainAppointmentId,
        court = "Test Court 2",
        courtId = "TSTCRT2",
        hearingType = HearingType.MAIN
      ),
      post = VideoLinkAppointment(
        id = 12,
        bookingId = 100,
        appointmentId = postAppointmentId,
        court = "Test Court 1",
        courtId = "TSTCRT1",
        hearingType = HearingType.POST
      ),
    )

    @Test
    fun `should get booking`() {

      whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(theVideoLinkBooking))

      prisonApiMockServer.stubGetPrisonAppointment(preAppointmentId, objectMapper.writeValueAsString(preAppointment))
      prisonApiMockServer.stubGetPrisonAppointment(mainAppointmentId, objectMapper.writeValueAsString(mainAppointment))
      prisonApiMockServer.stubGetPrisonAppointment(postAppointmentId, objectMapper.writeValueAsString(postAppointment))

      webTestClient.get()
        .uri("/court/video-link-bookings/$videoBookingId")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          {
            "videoLinkBookingId": 1,
            "bookingId": 100,
            "agencyId" : "WWI",
            "court": "Test Court 2",
            "courtId": "TSTCRT2",
            "comment": "any comment",
            "pre": {
              "locationId": 10,
              "startTime": "${referenceTime.format(ISO_LOCAL_DATE_TIME)}",
              "endTime": "${referenceTime.plusMinutes(30).format(ISO_LOCAL_DATE_TIME)}"
            },
            "main": {
              "locationId": 9,
              "startTime": "${referenceTime.plusMinutes(30).format(ISO_LOCAL_DATE_TIME)}",
              "endTime": "${referenceTime.plusMinutes(60).format(ISO_LOCAL_DATE_TIME)}"
            },
            "post": {
              "locationId": 5,
              "startTime": "${referenceTime.plusMinutes(60).format(ISO_LOCAL_DATE_TIME)}",
              "endTime": "${referenceTime.plusMinutes(90).format(ISO_LOCAL_DATE_TIME)}"
            }
          }
        """
        )
    }

    @Test
    fun `should get booking when only main appointment exists`() {

      whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(theVideoLinkBooking))

      prisonApiMockServer.stubGetPrisonAppointment(mainAppointmentId, objectMapper.writeValueAsString(mainAppointment))

      webTestClient.get()
        .uri("/court/video-link-bookings/$videoBookingId")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          {
            "videoLinkBookingId": 1,
            "bookingId": 100,
            "agencyId": "WWI",
            "court": "Test Court 2",
            "courtId": "TSTCRT2",
            "comment": "any comment",
            "main": {
              "locationId": 9,
              "startTime": "${referenceTime.plusMinutes(30).format(ISO_LOCAL_DATE_TIME)}",
              "endTime": "${referenceTime.plusMinutes(60).format(ISO_LOCAL_DATE_TIME)}"
            }
          }
        """
        )
    }

    @Test
    fun `should not find booking when only pre and post appointments exist`() {

      whenever(videoLinkBookingRepository.findById(videoBookingId)).thenReturn(Optional.of(theVideoLinkBooking))

      prisonApiMockServer.stubGetPrisonAppointment(preAppointmentId, objectMapper.writeValueAsString(preAppointment))
      prisonApiMockServer.stubGetPrisonAppointment(postAppointmentId, objectMapper.writeValueAsString(postAppointment))

      webTestClient.get()
        .uri("/court/video-link-bookings/$videoBookingId")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isNotFound
    }
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
            court = "York",
            courtId = "TSTCRT"
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
      .json(
        """
          {
            "appointments": [
              {
                "id": 1,
                "bookingId": 1,
                "appointmentId": 1,
                "court": "York",
                "courtId": "TSTCRT",
                "hearingType": "PRE"
              }
            ]
          }
      """
      )
  }

  @Test
  fun `Successful POST video-link-bookings, main appointment only`() {
    val bookingId = 1L
    val mainAppointmentId = 1L

    prisonApiMockServer.stubGetLocation(1L)
    prisonApiMockServer.stubAddAppointmentForBooking(bookingId, eventId = mainAppointmentId)

    val theVideoLinkBooking = VideoLinkBooking(
      main = VideoLinkAppointment(
        bookingId = bookingId,
        appointmentId = mainAppointmentId,
        court = "Test Court 1",
        courtId = "TSTCRT",
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
          "courtId" to "TSTCRT",
          "madeByTheCourt" to false,
          "main" to mapOf(
            "locationId" to 1,
            "startTime" to referenceTime.plusMinutes(30).format(ISO_LOCAL_DATE_TIME),
            "endTime" to referenceTime.plusMinutes(60).format(ISO_LOCAL_DATE_TIME)
          )
        )
      )
      .exchange()
      .expectStatus().isCreated
      .expectBody().json("1")

    verify(videoLinkBookingRepository).save(theVideoLinkBooking)
  }

  @Test
  fun `Failed POST video-link-bookings, invalid main start time`() {
    val bookingId = 1L

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
            "startTime" to yesterday.plusMinutes(30).format(ISO_LOCAL_DATE_TIME),
            "endTime" to yesterday.plusMinutes(60).format(ISO_LOCAL_DATE_TIME)
          )
        )
      )
      .exchange()
      .expectStatus().isBadRequest
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
        courtId = null,
        hearingType = HearingType.PRE
      ),
      main = VideoLinkAppointment(
        id = 11,
        bookingId = 4,
        appointmentId = mainAppointmentId,
        court = "Test Court 2",
        courtId = null,
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
      WireMock.deleteRequestedFor(WireMock.urlEqualTo("/api/appointments/$preAppointmentId"))
    )
    prisonApiMockServer.verify(
      WireMock.deleteRequestedFor(WireMock.urlEqualTo("/api/appointments/$mainAppointmentId"))
    )

    verify(videoLinkBookingRepository).deleteById(videoBookingId)
  }

  @Nested
  inner class UpdateBooking {
    @Test
    fun `Updates a booking`() {
      val offenderBookingId = 1L
      val oldAppointmentId = 1L
      val newAppointmentId = 2L

      prisonApiMockServer.stubGetLocation(2)
      prisonApiMockServer.stubDeleteAppointment(oldAppointmentId, status = 204)
      prisonApiMockServer.stubAddAppointmentForBooking(offenderBookingId, eventId = newAppointmentId)

      val theVideoLinkBooking = VideoLinkBooking(
        main = VideoLinkAppointment(
          bookingId = offenderBookingId,
          appointmentId = newAppointmentId,
          court = "Test Court 1",
          courtId = "TSTCRT",
          madeByTheCourt = false,
          hearingType = HearingType.MAIN
        )
      )

      whenever(videoLinkBookingRepository.findById(1L))
        .thenReturn(Optional.of(theVideoLinkBooking.copy(id = 1)))

      webTestClient.put()
        .uri("/court/video-link-bookings/1")
        .bodyValue(
          """
              {
                "comment": "New comment",
                "madeByTheCourt": false,
                "main": {
                  "locationId" : 2,
                  "startTime" : "${referenceTime.format(ISO_LOCAL_DATE_TIME)}",
                  "endTime": "${referenceTime.plusMinutes(30).format(ISO_LOCAL_DATE_TIME)}"
                }
              }
            """
        )
        .headers(setHeaders())
        .exchange()
        .expectStatus().isNoContent

      verify(videoLinkBookingRepository).findById(1L)
    }

    @Test
    fun `Rejects invalid end time`() {

      webTestClient.put()
        .uri("/court/video-link-bookings/1")
        .bodyValue(
          """
              {
                "comment": "New comment",
                "madeByTheCourt": false,
                "main": {
                  "locationId" : 2,
                  "startTime" : "${referenceTime.format(ISO_LOCAL_DATE_TIME)}",
                  "endTime": "${referenceTime.minusSeconds(1).format(ISO_LOCAL_DATE_TIME)}"
                }
              }
            """
        )
        .headers(setHeaders())
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().json(
          """
            {
              "status":400,
              "userMessage":"Main appointment start time must precede end time.",
              "developerMessage":"Main appointment start time must precede end time."
            }
          """
        )
    }
  }

  @Nested
  inner class UpdateBookingComment {

    val theVideoLinkBooking = VideoLinkBooking(
      main = VideoLinkAppointment(
        bookingId = 1L,
        appointmentId = 10L,
        court = "Test Court 1",
        courtId = null,
        madeByTheCourt = true,
        hearingType = HearingType.MAIN
      )
    )

    @Test
    fun `Updates a comment`() {

      whenever(videoLinkBookingRepository.findById(1L))
        .thenReturn(Optional.of(theVideoLinkBooking.copy(id = 1)))

      prisonApiMockServer.stubUpdateAppointmentComment(1L)

      webTestClient.put()
        .uri("/court/video-link-bookings/1/comment")
        .bodyValue("New Comment")
        .headers(setHeaders(contentType = MediaType.TEXT_PLAIN))
        .exchange()
        .expectStatus().isNoContent

      prisonApiMockServer.verify(
        WireMock.putRequestedFor(WireMock.urlEqualTo("/api/appointments/10/comment"))
      )
    }
  }
}
