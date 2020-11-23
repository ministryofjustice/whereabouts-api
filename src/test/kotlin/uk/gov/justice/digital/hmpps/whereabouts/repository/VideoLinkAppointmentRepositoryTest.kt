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
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade

@ActiveProfiles("test")
@Import(TestAuditConfiguration::class)
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
        appointmentId = 2,
        bookingId = 3,
        court = "York 2",
        hearingType = HearingType.MAIN,
        createdByUsername = "username1",
        madeByTheCourt = false
      )
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val appointments = videoLinkAppointmentRepository.findAll()

    assertThat(appointments).extracting("appointmentId", "bookingId", "court", "createdByUsername", "madeByTheCourt")
      .containsExactlyInAnyOrder(
        Tuple.tuple(1L, 2L, "York", null, true),
        Tuple.tuple(2L, 3L, "York 2", "username1", false)
      )
  }
}
