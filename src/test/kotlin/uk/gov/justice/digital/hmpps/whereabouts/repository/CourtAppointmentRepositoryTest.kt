package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.model.CourtAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class CourtAppointmentRepositoryTest {
  @Autowired
  lateinit var courtAppointmentRepository: CourtAppointmentRepository

  @Test
  fun `should return all court appointments`() {
    courtAppointmentRepository.save(CourtAppointment(
        appointmentId = 1,
        bookingId = 2,
        court = "York",
        hearingType = HearingType.MAIN
    ))

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val appointments = courtAppointmentRepository.findAll()

    assertThat(appointments).extracting("appointmentId", "bookingId", "court").contains(Tuple.tuple( 1L,2L,"York"))
  }
}
