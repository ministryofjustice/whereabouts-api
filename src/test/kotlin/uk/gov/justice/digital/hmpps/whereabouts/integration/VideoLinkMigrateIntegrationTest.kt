package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoBookingMigrateResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import java.time.LocalDateTime

@Transactional
class VideoLinkMigrateIntegrationTest : IntegrationTest() {
  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  @BeforeEach
  fun prepare() {
    deleteAllTestRows()
    TestTransaction.flagForCommit()
    TestTransaction.end()
  }

  @Test
  fun `Will return a simple booking to migrate`() {
    makeABooking()
    val result = webTestClient.get()
      .uri("/migrate/video-link-booking/1")
      .headers {
        it.setBearerAuth(
          jwtAuthHelper.createJwt(
            subject = "TEST-USER",
            roles = listOf("ROLE_BOOK_A_VIDEO_LINK_ADMIN"),
            clientId = "hmpps-book-a-video-link-client",
          ),
        )
      }
      .exchange()
      .expectStatus().isOk
      .expectBody(VideoBookingMigrateResponse::class.java)
      .returnResult()
      .responseBody

    assertThat(result).isNotNull

    with(result!!) {
      assertThat(cancelled).isFalse
      assertThat(events.size).isEqualTo(1)
      assertThat(events).extracting("eventType").containsOnly(VideoLinkBookingEventType.CREATE)
      assertThat(courtCode).isEqualTo("CVNTCC")
      assertThat(madeByTheCourt).isTrue
      assertThat(probation).isFalse
      assertThat(prisonCode).isEqualTo("MDI")

      // This is populated from the appointment data - 2023-11-10 10:30-11:00am
      with(main) {
        assertThat(locationId).isEqualTo(1)
        assertThat(date).isEqualTo("2023-11-10")
        assertThat(startTime).isEqualTo("10:30")
        assertThat(endTime).isEqualTo("11:00")
      }

      assertThat(result.pre).isNull()
      assertThat(result.post).isNull()
    }
  }

  @Test
  fun `Will return a cancelled booking to migrate`() {
    makeABooking(cancelled = true)
    val result = webTestClient.get()
      .uri("/migrate/video-link-booking/1")
      .headers {
        it.setBearerAuth(
          jwtAuthHelper.createJwt(
            subject = "TEST-USER",
            roles = listOf("ROLE_BOOK_A_VIDEO_LINK_ADMIN"),
            clientId = "hmpps-book-a-video-link-client",
          ),
        )
      }
      .exchange()
      .expectStatus().isOk
      .expectBody(VideoBookingMigrateResponse::class.java)
      .returnResult()
      .responseBody

    assertThat(result).isNotNull

    with(result!!) {
      assertThat(cancelled).isTrue
      assertThat(events.size).isEqualTo(2)
      assertThat(events).extracting("eventType").containsExactly(
        VideoLinkBookingEventType.CREATE,
        VideoLinkBookingEventType.DELETE,
      )
      assertThat(courtCode).isEqualTo("UNKNOWN")
      assertThat(madeByTheCourt).isTrue
      assertThat(probation).isFalse
      assertThat(prisonCode).isEqualTo("MDI")

      // This is populated from the CREATE event, main hearing should be 2023-11-11 10:30-11:00am
      with(main) {
        assertThat(locationId).isEqualTo(1)
        assertThat(date).isEqualTo("2023-11-11")
        assertThat(startTime).isEqualTo("10:30")
        assertThat(endTime).isEqualTo("11:00")
      }
    }
  }

  @Test
  fun `Will return an updated and cancelled booking to migrate`() {
    makeABooking(cancelled = true, updated = true)
    val result = webTestClient.get()
      .uri("/migrate/video-link-booking/1")
      .headers {
        it.setBearerAuth(
          jwtAuthHelper.createJwt(
            subject = "TEST-USER",
            roles = listOf("ROLE_BOOK_A_VIDEO_LINK_ADMIN"),
            clientId = "hmpps-book-a-video-link-client",
          ),
        )
      }
      .exchange()
      .expectStatus().isOk
      .expectBody(VideoBookingMigrateResponse::class.java)
      .returnResult()
      .responseBody

    assertThat(result).isNotNull

    with(result!!) {
      assertThat(cancelled).isTrue

      assertThat(events.size).isEqualTo(3)
      assertThat(events).extracting("eventType").containsExactly(
        VideoLinkBookingEventType.CREATE,
        VideoLinkBookingEventType.UPDATE,
        VideoLinkBookingEventType.DELETE,
      )

      // This is populated from the UPDATE event, main hearing should be 2023-11-12 10:40-11:10am
      with(main) {
        assertThat(locationId).isEqualTo(1)
        assertThat(date).isEqualTo("2023-11-12")
        assertThat(startTime).isEqualTo("10:30")
        assertThat(endTime).isEqualTo("11:00")
      }
    }
  }

  @Test
  fun `Will return a booking to migrate when missing booking and delete event`() {
    makeABooking(cancelled = false, updated = true, missing = true)

    val result = webTestClient.get()
      .uri("/migrate/video-link-booking/1")
      .headers {
        it.setBearerAuth(
          jwtAuthHelper.createJwt(
            subject = "TEST-USER",
            roles = listOf("ROLE_BOOK_A_VIDEO_LINK_ADMIN"),
            clientId = "hmpps-book-a-video-link-client",
          ),
        )
      }
      .exchange()
      .expectStatus().isOk
      .expectBody(VideoBookingMigrateResponse::class.java)
      .returnResult()
      .responseBody

    assertThat(result).isNotNull

    with(result!!) {
      assertThat(cancelled).isTrue

      assertThat(events.size).isEqualTo(3)
      assertThat(events).extracting("eventType").containsExactly(
        VideoLinkBookingEventType.CREATE,
        VideoLinkBookingEventType.UPDATE,
        VideoLinkBookingEventType.DELETE,
      )

      // This is populated from the UPDATE event, main hearing should be 2023-11-12 10:40-11:10am
      with(main) {
        assertThat(locationId).isEqualTo(1)
        assertThat(date).isEqualTo("2023-11-12")
        assertThat(startTime).isEqualTo("10:30")
        assertThat(endTime).isEqualTo("11:00")
      }
    }
  }

  @Test
  fun `Will fail when ROLE_BOOK_A_VIDEO_LINK_ADMIN is missing`() {
    makeABooking()
    webTestClient.get()
      .uri("/migrate/video-link-booking/1")
      .headers {
        it.setBearerAuth(
          jwtAuthHelper.createJwt(
            subject = "TEST-USER",
            roles = listOf("ROLE_INVALID"),
            clientId = "hmpps-book-a-video-link-client",
          ),
        )
      }
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `Will fail when no roles are provided`() {
    makeABooking(cancelled = true)
    webTestClient.get()
      .uri("/migrate/video-link-booking/1")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `Will raise migration event`() {
    makeABooking()

    webTestClient.put()
      .uri("/migrate/video-link-bookings?fromDate=2023-10-01&pageSize=10&waitMillis=0")
      .headers {
        it.setBearerAuth(
          jwtAuthHelper.createJwt(
            subject = "TEST-USER",
            roles = listOf("ROLE_BOOK_A_VIDEO_LINK_ADMIN"),
            clientId = "hmpps-book-a-video-link-client",
          ),
        )
      }
      .exchange()
      .expectStatus()
      .is2xxSuccessful()
  }

  private fun deleteAllTestRows() {
    JdbcTestUtils.deleteFromTables(
      jdbcTemplate,
      "VIDEO_LINK_BOOKING_EVENT",
      "VIDEO_LINK_APPOINTMENT",
      "VIDEO_LINK_BOOKING",
    )
  }

  private fun makeABooking(cancelled: Boolean = false, updated: Boolean = false, missing: Boolean = false) {
    // Cancelled bookings only have events - no video_link_booking or video_link_appointment rows
    makeEvents(cancelled, updated)

    if (!cancelled && !missing) {
      jdbcTemplate.update(
        """
      INSERT INTO video_link_booking (id, offender_booking_id, court_name, court_id, court_hearing_type, made_by_the_court, prison_id, comment)             
      VALUES (1, 1, 'CVNTCC', 'CVNTCC', 'APPEAL', true, 'MDI', 'Comment')
      """,
      )

      val appointmentTime = LocalDateTime.of(2023, 11, 10, 10, 30)
      makeAppointment(appointmentTime)
    }
  }

  private fun makeEvents(cancelled: Boolean = false, updated: Boolean = false) {
    val createdTime = LocalDateTime.of(2023, 10, 9, 0, 0)
    val appointmentTime = LocalDateTime.of(2023, 11, 10, 10, 30)

    // The CREATE event - present on all bookings
    jdbcTemplate.update(
      """
      INSERT INTO video_link_booking_event (
        event_id, timestamp, event_type, user_id, video_link_booking_id, agency_id, offender_booking_id, court,                     
        made_by_the_court, comment, main_nomis_appointment_id, main_location_id, main_start_time, main_end_time)             
        VALUES (1, ?, 'CREATE', 'TEST_USER', 1, 'MDI', 1, 'COURT', true, 'created', 1, 1, ?, ?)
      """,
      createdTime,
      appointmentTime.plusDays(1),
      appointmentTime.plusDays(1).plusMinutes(30),
    )

    if (updated) {
      // The UPDATE event
      jdbcTemplate.update(
        """
        INSERT INTO video_link_booking_event (
          event_id, timestamp, event_type, user_id, video_link_booking_id, agency_id, offender_booking_id, court,                     
          made_by_the_court, comment, main_nomis_appointment_id, main_location_id, main_start_time, main_end_time)             
          VALUES (2, ?, 'UPDATE', 'TEST_USER', 1, 'MDI', 1, 'COURT', true, 'Updated', 1, 1, ?, ?)
        """,
        createdTime.plusMinutes(5),
        appointmentTime.plusDays(2),
        appointmentTime.plusDays(2).plusMinutes(30),
      )
    }

    if (cancelled) {
      // The DELETE event
      jdbcTemplate.update(
        """
        INSERT INTO video_link_booking_event (
          event_id, timestamp, event_type, user_id, video_link_booking_id, agency_id, offender_booking_id, court,                     
          made_by_the_court, comment, main_nomis_appointment_id, main_location_id, main_start_time, main_end_time)             
          VALUES (3, ?, 'DELETE', 'TEST_USER', 1, 'MDI', 1, 'COURT', true, 'Updated', 1, 1, ?, ?)
        """,
        createdTime.plusMinutes(10),
        appointmentTime.plusDays(3),
        appointmentTime.plusDays(3).plusMinutes(30),
      )
    }
  }

  private fun makeAppointment(startTime: LocalDateTime) {
    jdbcTemplate.update(
      """
        INSERT INTO video_link_appointment (
          id, video_link_booking_id, appointment_id, location_id, start_date_time, end_date_time, hearing_type
        ) VALUES (1, 1, 1, 1, ?, ?, 'MAIN')
        """,
      startTime,
      startTime.plusMinutes(30),
    )
  }
}
