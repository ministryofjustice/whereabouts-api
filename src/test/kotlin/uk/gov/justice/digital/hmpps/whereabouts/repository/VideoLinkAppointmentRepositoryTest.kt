package uk.gov.justice.digital.hmpps.whereabouts.repository

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.config.AuditConfiguration
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade

@ActiveProfiles("test")
@Import(AuditConfiguration::class)
@DataJpaTest
@Transactional
class VideoLinkAppointmentRepositoryTest {
  @Autowired
  lateinit var videoLinkAppointmentRepository: VideoLinkAppointmentRepository

  @MockBean
  lateinit var authenticationFacade: AuthenticationFacade

  @Test
  fun `should return all video link appointments`() {
    whenever(authenticationFacade.currentUsername).thenReturn("username1")

    val preAppointments = videoLinkAppointmentRepository.findAll()

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        appointmentId = 1,
        bookingId = 2,
        court = "York",
        hearingType = HearingType.MAIN
      )
    )

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        appointmentId = 3,
        bookingId = 4,
        court = null,
        courtId = "TSTCRT",
        hearingType = HearingType.MAIN,
        createdByUsername = "username2",
        madeByTheCourt = false
      )
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val appointments = videoLinkAppointmentRepository.findAll().minus(preAppointments)

    assertThat(appointments).extracting(
      "appointmentId",
      "bookingId",
      "court",
      "courtId",
      "createdByUsername",
      "madeByTheCourt"
    )
      .containsExactlyInAnyOrder(
        Tuple.tuple(1L, 2L, "York", null, "username1", true),
        Tuple.tuple(3L, 4L, null, "TSTCRT", "username1", false)
      )
  }
}
