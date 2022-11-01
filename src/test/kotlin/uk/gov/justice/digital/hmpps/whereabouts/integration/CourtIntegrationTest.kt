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

  private val tomorrow: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1)
  val yesterday: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(1)
  val referenceTime: LocalDateTime = tomorrow.plusHours(9)

  private val preAppointmentId: Long = 2
  private val mainAppointmentId: Long = 3
  private val postAppointmentId: Long = 4

  private val mainLocationId: Long = 10L
  private val preLocationId: Long = 16L
  private val postLocationId: Long = 17L

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

  @Test
  fun `get court email`() {
    webTestClient.get()
      .uri("/court/courts/AMERCC/email")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.email").hasJsonPath()
      .jsonPath("$.email").isEqualTo("test@test.gov.uk")
  }
  @Test
  fun `return 404 court email when email not exist`() {
    webTestClient.get()
      .uri("/court/courts/AMERCCX/email")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isNotFound
  }

  @Nested
  inner class GetBooking {

    private val prePrisonAppointment = PrisonAppointment(
      bookingId = 100,
      eventId = preAppointmentId,
      startTime = referenceTime,
      endTime = referenceTime.plusMinutes(30),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 10,
      comment = "any comment"
    )

    private val mainPrisonAppointment = PrisonAppointment(
      bookingId = 100,
      eventId = mainAppointmentId,
      startTime = referenceTime.plusMinutes(30),
      endTime = referenceTime.plusMinutes(60),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 9,
      comment = "any comment",
    )

    private val postPrisonAppointment = PrisonAppointment(
      bookingId = 100,
      eventId = postAppointmentId,
      startTime = referenceTime.plusMinutes(60),
      endTime = referenceTime.plusMinutes(90),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 5,
      comment = "any comment"
    )

    private fun makeVideoLinkBooking() = VideoLinkBooking(
      offenderBookingId = 100,
      courtName = "Test Court 1",
      courtId = "TSTCRT1",
      prisonId = "WWI",
      comment = "any comment"
    ).apply {
      addPreAppointment(appointmentId = preAppointmentId, locationId = preLocationId, startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0), endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0))
      addMainAppointment(appointmentId = mainAppointmentId, locationId = mainLocationId, startDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0), endDateTime = LocalDateTime.of(2022, 1, 1, 12, 0, 0))
      addPostAppointment(appointmentId = postAppointmentId, locationId = postLocationId, startDateTime = LocalDateTime.of(2022, 1, 1, 12, 0, 0), endDateTime = LocalDateTime.of(2022, 1, 1, 13, 0, 0))
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
          courtId = "TSTCRT",
          prisonId = "WWI",
          comment = "any comment"
        ).apply { addMainAppointment(appointmentId = 1L, locationId = mainLocationId, startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0), endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)) }
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
      val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
      val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)

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
        .ignoringFields("id", "appointments.MAIN.id", "appointments.MAIN.videoLinkBooking")
        .isEqualTo(
          VideoLinkBooking(
            offenderBookingId = bookingId,
            courtName = "Test Court 1",
            courtId = "TSTCRT",
            madeByTheCourt = false,
            prisonId = "WWI"
          ).apply {
            addMainAppointment(mainAppointmentId, mainLocationId, startDateTime, endDateTime)
            createdByUsername = "ITAG_USER"
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
      val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
      val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)

      val persistentBooking = videoLinkBookingRepository.save(
        VideoLinkBooking(
          offenderBookingId = 4,
          courtName = "Test Court 1",
          courtId = null,
          prisonId = "WWI",
          comment = "any comment"
        ).apply {
          addPreAppointment(preAppointmentId, preLocationId, startDateTime, endDateTime)
          addMainAppointment(mainAppointmentId, postLocationId, startDateTime, endDateTime)
        }
      )

      assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "video_link_booking")).isEqualTo(1)
      assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "video_link_appointment")).isEqualTo(2)

      prisonApiMockServer.stubDeleteAppointments(listOf(preAppointmentId.toInt(), mainAppointmentId.toInt()))

      webTestClient.delete()
        .uri("/court/video-link-bookings/${persistentBooking.id}")
        .headers(setHeaders())
        .exchange()
        .expectStatus().isNoContent

      prisonApiMockServer.verify(
        WireMock.postRequestedFor(WireMock.urlEqualTo("/api/appointments/delete"))
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
      val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
      val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)

      prisonApiMockServer.stubGetLocation(2)
      prisonApiMockServer.stubDeleteAppointments(listOf(oldAppointmentId.toInt()))
      prisonApiMockServer.stubAddAppointmentForBooking(offenderBookingId, eventId = newAppointmentId)

      val persistentBooking = videoLinkBookingRepository.save(
        VideoLinkBooking(
          offenderBookingId = offenderBookingId,
          courtName = "Test Court 1",
          courtId = "TSTCRT",
          madeByTheCourt = false,
          prisonId = "WWI",
          comment = "any comment"
        ).apply {
          addMainAppointment(oldAppointmentId, mainLocationId, startDateTime, endDateTime)
        }
      )

      webTestClient.put()
        .uri("/court/video-link-bookings/${persistentBooking.id}")
        .bodyValue(
          """
              {
                "comment": "New comment",
                "madeByTheCourt": false,
                "courtId": "CRT",
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
                "courtId": "CRT",
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
    private val appointmentId = 10L
    private val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
    private val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)

    private val theVideoLinkBooking = VideoLinkBooking(
      offenderBookingId = 1L,
      courtName = "Test Court 1",
      courtId = null,
      madeByTheCourt = true,
      prisonId = "WWI",
      comment = "any comment"
    ).apply {
      addMainAppointment(appointmentId, mainLocationId, startDateTime, endDateTime)
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

  @Nested
  inner class VideoLinkBookingCheck {
    @Test
    fun happyFlow() {
      prisonApiMockServer.stubGetScheduledAppointmentsByAgencyDateAndLocationId(agencyId = "WWI", locationId = 1L)

      webTestClient.post()
        .uri("/court/video-link-booking-check")
        .bodyValue(
          """
          {
            "agencyId" : "WWI",
            "date" : "2020-12-25",
            "mainAppointment" : {
              "locationId" : 1,
              "interval" : {
                "start" : "09:00",
                "end" : "09:30"
              }
            }
          }
        """
        )
        .headers(setHeaders())
        .exchange()
        .expectStatus().isOk
        .expectBody().json(
          """
          {
            "matched": false,
            "alternatives": [
              { "pre": null, "main": { "locationId": 1, "interval": { "start": "08:15:00", "end": "08:45:00" }}, "post": null },
              { "pre": null, "main": { "locationId": 1, "interval": { "start": "08:30:00", "end": "09:00:00" }}, "post": null },
              { "pre": null, "main": { "locationId": 1, "interval": { "start": "09:30:00", "end": "10:00:00" }}, "post": null }
            ]
          }
          """
        )
    }

    @Test
    fun `Start later than end - invalid`() {
      webTestClient.post()
        .uri("/court/video-link-booking-check")
        .bodyValue(
          """
          {
            "agencyId" : "WWI",
            "date" : "2020-12-25",
            "mainAppointment" : {
              "locationId" : 1,
              "interval" : {
                "start" : "09:30",
                "end" : "09:00"
              }
            }
          }
        """
        )
        .headers(setHeaders())
        .exchange()
        .expectStatus().isBadRequest
    }
  }
}
