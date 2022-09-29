package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers
import java.time.LocalDateTime

@Transactional
class VideoLinkBookingMigrationIntegrationTest : IntegrationTest() {

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate
  @Autowired
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

  @BeforeEach
  fun prepare() {
    deleteAll()
    makeSomeBookings()
    TestTransaction.flagForCommit()
    TestTransaction.end()
  }

  @Test
  fun `Update existing booking when main appointment exist`() {

    prisonApiMockServer.stubGetPrisonAppointment(
      438577488,
      objectMapper.writeValueAsString(DataHelpers.makePrisonAppointment(eventId = 1, startTime = startTime))
    )
    prisonApiMockServer.stubGetPrisonAppointment404(438577489)
    prisonApiMockServer.stubGetPrisonAppointment404(438577490)

    val uri = "$baseUrl/1"
    webTestClient.get()
      .uri(uri)
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("videoLinkAppointmentRemaining").isEqualTo(0)
      .jsonPath("videoLinkBookingRemaining").isEqualTo(0)

    val vlb = videoLinkBookingRepository.findById(1)
    assertThat(vlb.get().prisonId).isEqualTo("MDI")
    assertThat(vlb.get().comment).isEqualTo("test")
    assertThat(vlb.get().appointments.get(HearingType.MAIN)?.startDateTime).isEqualTo(startTime)
    assertThat(vlb.get().appointments.get(HearingType.PRE)).isEqualTo(null)
    assertThat(vlb.get().appointments.get(HearingType.POST)).isEqualTo(null)
  }

  @Test
  fun `Update existing booking when pre and main appointment exist`() {

    prisonApiMockServer.stubGetPrisonAppointment(
      438577488,
      objectMapper.writeValueAsString(DataHelpers.makePrisonAppointment(eventId = 1, startTime = startTime))
    )
    prisonApiMockServer.stubGetPrisonAppointment(
      438577489,
      objectMapper.writeValueAsString(DataHelpers.makePrisonAppointment(eventId = 1, startTime = startTime))
    )

    prisonApiMockServer.stubGetPrisonAppointment404(438577490)

    val uri = "$baseUrl/1"
    webTestClient.get()
      .uri(uri)
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("videoLinkAppointmentRemaining").isEqualTo(0)
      .jsonPath("videoLinkBookingRemaining").isEqualTo(0)

    val vlb = videoLinkBookingRepository.findById(1)
    assertThat(vlb.get().prisonId).isEqualTo("MDI")
    assertThat(vlb.get().comment).isEqualTo("test")
    assertThat(vlb.get().appointments.get(HearingType.MAIN)?.startDateTime).isEqualTo(startTime)
    assertThat(vlb.get().appointments.get(HearingType.PRE)?.startDateTime).isEqualTo(startTime)
    assertThat(vlb.get().appointments.get(HearingType.POST)).isEqualTo(null)
  }

  @Test
  fun `delete existing booking when main appointment not exist`() {

    prisonApiMockServer.stubGetPrisonAppointment404(438577488)
    prisonApiMockServer.stubGetPrisonAppointment404(438577489)
    prisonApiMockServer.stubGetPrisonAppointment404(438577490)

    val uri = "$baseUrl/1"
    webTestClient.get()
      .uri(uri)
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("videoLinkAppointmentRemaining").isEqualTo(0)
      .jsonPath("videoLinkBookingRemaining").isEqualTo(0)

    val vlb = videoLinkBookingRepository.findById(1)
    assertThat(vlb).isEmpty
  }

  fun deleteAll() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "video_link_appointment")
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "video_link_booking")
  }

  fun makeSomeBookings() {

    jdbcTemplate.update(
      """INSERT INTO video_link_booking (id, offender_booking_id, court_name, court_id, made_by_the_court,
                                       created_by_username, prison_id, comment)
VALUES (1, 1182546, 'Wimbledon', 'WMBLMC', false, null, null, null);"""
    )
    jdbcTemplate.update(
      """INSERT INTO video_link_appointment (id, appointment_id, hearing_type, video_link_booking_id, location_id,
                                           start_date_time, end_date_time)
VALUES (1, 438577488, 'MAIN',1, null, null, null);"""
    )
    jdbcTemplate.update(
      """INSERT INTO video_link_appointment (id, appointment_id, hearing_type, video_link_booking_id, location_id,
                                           start_date_time, end_date_time)
VALUES (2, 438577489, 'PRE', 1, null, null, null);"""
    )
    jdbcTemplate.update(
      """INSERT INTO video_link_appointment (id, appointment_id, hearing_type, video_link_booking_id, location_id,
                                           start_date_time, end_date_time)
VALUES (3, 438577490, 'POST',1, null, null, null);"""
    )
  }

  companion object {
    val startTime: LocalDateTime = LocalDateTime.now()
    const val baseUrl = "/court/migrate-existing-bookings"
  }
}
