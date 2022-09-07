package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.config.AuditConfiguration
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.time.LocalDateTime

@ActiveProfiles("test")
@Import(AuditConfiguration::class)
@DataJpaTest
@Transactional
class VideoLinkAppointmentRepositoryTest {
  @Autowired
  lateinit var videoLinkAppointmentRepository: VideoLinkAppointmentRepository

  @Autowired
  lateinit var videoLinkBookingRepository: VideoLinkBookingRepository

  @MockBean
  lateinit var authenticationFacade: AuthenticationFacade

  @Test
  fun `should return all video link appointments`() {
    whenever(authenticationFacade.currentUsername).thenReturn("username1")

    val preAppointments = videoLinkAppointmentRepository.findAll()
    val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
    val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)
    val prisonId = "WWI"
    videoLinkBookingRepository.save(
      VideoLinkBooking(offenderBookingId = 2, courtName = "York", prisonId = prisonId).apply {
        addMainAppointment(1, 20L, startDateTime, endDateTime)
      }
    )

    videoLinkBookingRepository.save(
      VideoLinkBooking(
        offenderBookingId = 4,
        courtName = null,
        courtId = "TSTCRT",
        madeByTheCourt = false,
        prisonId = prisonId
      ).apply {
        addMainAppointment(3, 20L, startDateTime, endDateTime)
        createdByUsername = "username2"
      }
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    val appointments = videoLinkAppointmentRepository.findAll().minus(preAppointments)

    assertThat(appointments).extracting("appointmentId")
      .containsExactlyInAnyOrder(1L, 3L)
  }
}
