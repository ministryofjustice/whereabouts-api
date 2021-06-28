package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Transactional
class VideoLinkBookingEventIntegrationTest : IntegrationTest() {

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate

  @BeforeEach
  fun prepare() {
    deleteAll()
    makeSomeBookings(startDate, bookingCount)
    TestTransaction.flagForCommit()
    TestTransaction.end()
  }

  @Test
  fun `Happy flow`() {

    val uri = "$baseUrl?start-date=${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}"
    webTestClient.get()
      .uri(uri)
      .accept(MediaType("text", "csv"))
      .headers {
        it.setBearerAuth(
          jwtAuthHelper.createJwt(
            subject = "ITAG_USER",
            roles = listOf("ROLE_VIDEO_LINK_COURT_USER"),
            clientId = "elite2apiclient"
          )
        )
      }
      .exchange()
      .expectStatus().isOk
      .expectBody().consumeWith {
        val csv = String(it.responseBody)
        assertThat(csv).startsWith("eventId,timestamp,videoLinkBookingId,eventType,agencyId,court,courtId,madeByTheCourt,mainStartTime,mainEndTime,preStartTime,preEndTime,postStartTime,postEndTime")
        assertThat(csv).hasLineCount((bookingCount + 1).toInt())
      }
  }

  fun deleteAll() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "VIDEO_LINK_BOOKING_EVENT")
  }

  fun makeSomeBookings(startDate: LocalDate, eventCount: Long) {
    val referenceTime = startDate.atTime(0, 0)

    (1..eventCount).forEach { i ->
      val bookingTime = referenceTime.plusMinutes(i)

      jdbcTemplate.update(
        """
      INSERT INTO video_link_booking_event (
        event_id,
        timestamp,                 
        event_type,                
        user_id,                   
        video_link_booking_id,     
        agency_id,                 
        offender_booking_id,       
        court,                     
        made_by_the_court,         
        comment,                   
        main_nomis_appointment_id, 
        main_location_id,          
        main_start_time,           
        main_end_time             
      ) values (DEFAULT, ?, 'CREATE', 'ITAG_USER',  ?, 'MDI', ?, 'The Court', true, 'A comment', ?, ?, ?, ?)
    """,
        LocalDateTime.now(), i, i, i, i, bookingTime, bookingTime.plusMinutes(30)
      )
    }
  }

  companion object {
    const val bookingCount = 20L
    val startDate: LocalDate = LocalDate.now().plusDays(30)
    const val baseUrl = "/events/video-link-booking-events"
  }
}
