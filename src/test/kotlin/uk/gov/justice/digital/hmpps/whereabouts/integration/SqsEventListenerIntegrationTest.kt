package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.SqsEventListener
import wiremock.org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

@Transactional
class SqsEventListenerIntegrationTest : IntegrationTest() {

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate
  @Autowired
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

  @Autowired
  lateinit var videoLinkAppointmentRepository: VideoLinkAppointmentRepository

  @Autowired
  lateinit var sqsEventListener: SqsEventListener

  @BeforeEach
  fun prepare() {
    deleteAll()
    makeSomeBookings()
    TestTransaction.flagForCommit()
    TestTransaction.end()
  }

  @Test
  fun `delete pre appointment`() {
    sqsEventListener.handleEvents(getJson("/services/pre-appointment-deleted-request.json"))
    Assertions.assertThat(videoLinkAppointmentRepository.findAll().size).isEqualTo(2)
  }
  @Test
  fun `delete main appointment`() {
    prisonApiMockServer.stubDeleteAppointments(listOf(438577489, 438577490))
    sqsEventListener.handleEvents(getJson("/services/main-appointment-deleted-request.json"))
    Assertions.assertThat(videoLinkAppointmentRepository.findAll().size).isEqualTo(0)
    Assertions.assertThat(videoLinkBookingRepository.findAll().size).isEqualTo(0)
  }

  fun deleteAll() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "video_link_appointment")
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "video_link_booking")
  }

  private fun getJson(filename: String): String {
    return IOUtils.toString(javaClass.getResourceAsStream(filename), StandardCharsets.UTF_8.toString())
  }

  fun makeSomeBookings() {

    jdbcTemplate.update(
      """INSERT INTO video_link_booking (id, offender_booking_id, court_name, court_id, made_by_the_court,
                                       created_by_username, prison_id, comment)
VALUES (1, 1182546, 'Wimbledon', 'WMBLMC', false, null, 'P2', null);"""
    )
    jdbcTemplate.update(
      """INSERT INTO video_link_appointment (id, appointment_id, hearing_type, video_link_booking_id, location_id,
                                           start_date_time, end_date_time)
VALUES (1, 438577488, 'MAIN',1, 1234, '2022-01-01 10:00:00', '2022-01-01 11:00:00');"""
    )
    jdbcTemplate.update(
      """INSERT INTO video_link_appointment (id, appointment_id, hearing_type, video_link_booking_id, location_id,
                                           start_date_time, end_date_time)
VALUES (2, 438577489, 'PRE', 1, 1234, '2022-01-01 09:00:00', '2022-01-01 10:00:00');"""
    )
    jdbcTemplate.update(
      """INSERT INTO video_link_appointment (id, appointment_id, hearing_type, video_link_booking_id, location_id,
                                           start_date_time, end_date_time)
VALUES (3, 438577490, 'POST',1, 1234,'2022-01-01 11:00:00', '2022-01-01 12:00:00');"""
    )
  }
}
