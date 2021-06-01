package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.jdbc.JdbcTestUtils
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.time.temporal.ChronoUnit

class CourtIntegrationTest(
  @Autowired val videoLinkBookingRepository: VideoLinkBookingRepository,
  @Autowired val jdbcTemplate: JdbcTemplate,
) : IntegrationTest() {

  @MockBean
  lateinit var telemetryClient: TelemetryClient

  val tomorrow: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1)
  val yesterday: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(1)
  val referenceTime: LocalDateTime = tomorrow.plusHours(9)

  @Test
  fun `should list all court names`() {
    webTestClient.get()
      .uri("/court/all-courts")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.courtLocations.[0]").hasJsonPath()
      .jsonPath("$.courtLocations.[49]").hasJsonPath()
  }

  @Test
  fun `should list all courts`() {
    webTestClient.get()
      .uri("/court/courts")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.[0].name").hasJsonPath()
      .jsonPath("$.[0].id").hasJsonPath()
      .jsonPath("$.[49].name").hasJsonPath()
      .jsonPath("$.[49].id").hasJsonPath()
  }

  @Nested
  inner class GetBooking {

    val preAppointmentId: Long = 2
    val mainAppointmentId: Long = 3
    val postAppointmentId: Long = 4

    val prePrisonAppointment = PrisonAppointment(
      bookingId = 100,
      eventId = preAppointmentId,
      startTime = referenceTime,
      endTime = referenceTime.plusMinutes(30),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 10,
      comment = "any comment"
    )

    val mainPrisonAppointment = PrisonAppointment(
      bookingId = 100,
      eventId = mainAppointmentId,
      startTime = referenceTime.plusMinutes(30),
      endTime = referenceTime.plusMinutes(60),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 9,
      comment = "any comment",
    )

    val postPrisonAppointment = PrisonAppointment(
      bookingId = 100,
      eventId = postAppointmentId,
      startTime = referenceTime.plusMinutes(60),
      endTime = referenceTime.plusMinutes(90),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 5,
      comment = "any comment"
    )

    fun makeVideoLinkBooking() = VideoLinkBooking(
      offenderBookingId = 100,
      courtName = "Test Court 1",
      courtId = "TSTCRT1"
    ).apply {
      addPreAppointment(appointmentId = preAppointmentId)
      addMainAppointment(appointmentId = mainAppointmentId)
      addPostAppointment(appointmentId = postAppointmentId)
    }

    @BeforeEach
    fun resetVideoLinkBookings() {
      JdbcTestUtils.deleteFromTables(jdbcTemplate, "VIDEO_LINK_APPOINTMENT", "VIDEO_LINK_BOOKING")
    }

    @Test
    fun `should get booking`() {

      val videoLinkBookingId = videoLinkBookingRepository.save(makeVideoLinkBooking()).id!!

      prisonApiMockServer.stubGetPrisonAppointment(
        preAppointmentId,
        objectMapper.writeValueAsString(prePrisonAppointment)
      )
      prisonApiMockServer.stubGetPrisonAppointment(
        mainAppointmentId,
        objectMapper.writeValueAsString(mainPrisonAppointment)
      )
      prisonApiMockServer.stubGetPrisonAppointment(
        postAppointmentId,
        objectMapper.writeValueAsString(postPrisonAppointment)
      )

      webTestClient.get()
        .uri("/court/video-link-bookings/$videoLinkBookingId")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          {
            "videoLinkBookingId": $videoLinkBookingId,
            "bookingId": 100,
            "agencyId" : "WWI",
            "court": "Test Court 1",
            "courtId": "TSTCRT1",
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

      val videoLinkBookingId = videoLinkBookingRepository.save(makeVideoLinkBooking()).id!!

      prisonApiMockServer.stubGetPrisonAppointment(
        mainAppointmentId,
        objectMapper.writeValueAsString(mainPrisonAppointment)
      )

      webTestClient.get()
        .uri("/court/video-link-bookings/$videoLinkBookingId")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          {
            "videoLinkBookingId": $videoLinkBookingId,
            "bookingId": 100,
            "agencyId": "WWI",
            "court": "Test Court 1",
            "courtId": "TSTCRT1",
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

      val videoLinkBookingId = videoLinkBookingRepository.save(makeVideoLinkBooking()).id!!

      prisonApiMockServer.stubGetPrisonAppointment(
        preAppointmentId,
        objectMapper.writeValueAsString(prePrisonAppointment)
      )
      prisonApiMockServer.stubGetPrisonAppointment(
        postAppointmentId,
        objectMapper.writeValueAsString(postPrisonAppointment)
      )

      webTestClient.get()
        .uri("/court/video-link-bookings/$videoLinkBookingId")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isNotFound
    }
  }

  @Nested
  inner class GetAppointments {
    @BeforeEach
    fun resetVideoLinkBookings() {
      JdbcTestUtils.deleteFromTables(jdbcTemplate, "VIDEO_LINK_APPOINTMENT", "VIDEO_LINK_BOOKING")
    }

    @Test
    fun `should return video link appointment by appointment id`() {
      val persistentBooking = videoLinkBookingRepository.save(
        VideoLinkBooking(
          offenderBookingId = 1L,
          courtName = "York",
          courtId = "TSTCRT"
        ).apply { addMainAppointment(appointmentId = 1L) }
      )

      val mainAppointmentId = persistentBooking.appointments[MAIN]?.id
      assertThat(mainAppointmentId).isNotNull

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
                "id": $mainAppointmentId,
                "bookingId": 1,
                "appointmentId": 1,
                "court": "York",
                "courtId": "TSTCRT",
                "hearingType": "MAIN"
              }
            ]
          }
      """
        )
    }
  }

  @Nested
  inner class CreateBooking {
    @BeforeEach
    fun resetVideoLinkBookings() {
      JdbcTestUtils.deleteFromTables(jdbcTemplate, "VIDEO_LINK_APPOINTMENT", "VIDEO_LINK_BOOKING")
    }

    @Test
    fun `Successful POST video-link-bookings, main appointment only`() {
      val bookingId = 1L
      val mainAppointmentId = 1L

      prisonApiMockServer.stubGetLocation(1L)
      prisonApiMockServer.stubAddAppointmentForBooking(bookingId, eventId = mainAppointmentId)

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
        .expectBody().jsonPath("$").isNumber

      val bookings = videoLinkBookingRepository.findAll()
      assertThat(bookings).hasSize(1)

      assertThat(bookings[0])
        .usingRecursiveComparison()
        .ignoringFields("id", "appointments.id", "appointments.videoLinkBooking")
        .isEqualTo(
          VideoLinkBooking(
            offenderBookingId = bookingId,
            courtName = "Test Court 1",
            courtId = "TSTCRT",
            madeByTheCourt = false,
            createdByUsername = "ITAG_USER"
          ).apply {
            addMainAppointment(mainAppointmentId)
          }
        )
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
  }

  @Nested
  inner class DeleteBooking {
    @BeforeEach
    fun resetVideoLinkBookings() {
      JdbcTestUtils.deleteFromTables(jdbcTemplate, "VIDEO_LINK_APPOINTMENT", "VIDEO_LINK_BOOKING")
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

      val preAppointmentId: Long = 2
      val mainAppointmentId: Long = 3

      val persistentBooking = videoLinkBookingRepository.save(
        VideoLinkBooking(
          offenderBookingId = 4,
          courtName = "Test Court 1",
          courtId = null
        ).apply {
          addPreAppointment(appointmentId = preAppointmentId)
          addMainAppointment(appointmentId = mainAppointmentId)
        }
      )

      assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "video_link_booking")).isEqualTo(1)
      assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "video_link_appointment")).isEqualTo(2)

      prisonApiMockServer.stubDeleteAppointment(preAppointmentId, 200)
      prisonApiMockServer.stubDeleteAppointment(mainAppointmentId, 404)

      webTestClient.delete()
        .uri("/court/video-link-bookings/${persistentBooking.id}")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isNoContent

      prisonApiMockServer.verify(
        WireMock.deleteRequestedFor(WireMock.urlEqualTo("/api/appointments/$preAppointmentId"))
      )
      prisonApiMockServer.verify(
        WireMock.deleteRequestedFor(WireMock.urlEqualTo("/api/appointments/$mainAppointmentId"))
      )

      assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "video_link_booking")).isEqualTo(0)
      assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "video_link_appointment")).isEqualTo(0)
    }
  }

  @Nested
  inner class UpdateBooking {

    @BeforeEach
    fun resetVideoLinkBookings() {
      JdbcTestUtils.deleteFromTables(jdbcTemplate, "VIDEO_LINK_APPOINTMENT", "VIDEO_LINK_BOOKING")
    }

    @Test
    fun `Updates a booking`() {
      val offenderBookingId = 1L
      val oldAppointmentId = 1L
      val newAppointmentId = 2L

      prisonApiMockServer.stubGetLocation(2)
      prisonApiMockServer.stubDeleteAppointment(oldAppointmentId, status = 204)
      prisonApiMockServer.stubAddAppointmentForBooking(offenderBookingId, eventId = newAppointmentId)

      val persistentBooking = videoLinkBookingRepository.save(
        VideoLinkBooking(
          offenderBookingId = offenderBookingId,
          courtName = "Test Court 1",
          courtId = "TSTCRT",
          madeByTheCourt = false
        ).apply {
          addMainAppointment(appointmentId = oldAppointmentId)
        }
      )

      webTestClient.put()
        .uri("/court/video-link-bookings/${persistentBooking.id}")
        .bodyValue(
          """
              {
                "comment": "New comment",
                "madeByTheCourt": false,
                "main": {
                  "locationId": 2,
                  "startTime": "${referenceTime.format(ISO_LOCAL_DATE_TIME)}",
                  "endTime": "${referenceTime.plusMinutes(30).format(ISO_LOCAL_DATE_TIME)}"
                }
              }
            """
        )
        .headers(setHeaders())
        .exchange()
        .expectStatus().isNoContent
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
      offenderBookingId = 1L,
      courtName = "Test Court 1",
      courtId = null,
      madeByTheCourt = true
    ).apply {
      addMainAppointment(10L)
    }

    @BeforeEach
    fun resetVideoLinkBookings() {
      JdbcTestUtils.deleteFromTables(jdbcTemplate, "VIDEO_LINK_APPOINTMENT", "VIDEO_LINK_BOOKING")
    }

    @Test
    fun `Updates a comment`() {
      val persistentBookingId = videoLinkBookingRepository.save(theVideoLinkBooking).id!!

      prisonApiMockServer.stubUpdateAppointmentComment(1L)

      webTestClient.put()
        .uri("/court/video-link-bookings/$persistentBookingId/comment")
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
