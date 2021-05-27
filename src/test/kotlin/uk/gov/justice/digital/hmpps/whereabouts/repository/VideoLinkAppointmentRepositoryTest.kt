package uk.gov.justice.digital.hmpps.whereabouts.repository

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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

    videoLinkBookingRepository.save(
      VideoLinkBooking(offenderBookingId = 2, courtName = "York").apply {
        addMainAppointment(appointmentId = 1)
      }
    )

    videoLinkBookingRepository.save(
      VideoLinkBooking(
        offenderBookingId = 4,
        courtName = null,
        courtId = "TSTCRT",
        createdByUsername = "username2",
        madeByTheCourt = false
      ).apply {
        addMainAppointment(appointmentId = 3)
      }
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val appointments = videoLinkAppointmentRepository.findAll().minus(preAppointments)

    assertThat(appointments).extracting("appointmentId")
      .containsExactlyInAnyOrder(1L, 3L)
  }
}
