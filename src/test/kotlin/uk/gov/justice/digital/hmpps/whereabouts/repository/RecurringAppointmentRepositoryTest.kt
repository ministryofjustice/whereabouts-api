package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import uk.gov.justice.digital.hmpps.whereabouts.model.RecurringAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RelatedAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RepeatPeriod
import java.time.LocalDateTime

@DataJpaTest
class RecurringAppointmentRepositoryTest {

  @Autowired
  lateinit var entityManager: TestEntityManager

  @Autowired
  lateinit var recurringAppointmentRepository: RecurringAppointmentRepository

  @Test
  fun `find main appointment using child recurring appointment id`() {
    val recurringAppointmentBookingId = entityManager.persistAndFlush(
      RecurringAppointment(
        repeatPeriod = RepeatPeriod.FORTNIGHTLY, count = 1,
        startTime = LocalDateTime.of(2021, 1, 21, 0, 0, 0),
        relatedAppointments = listOf(
          RelatedAppointment(2), RelatedAppointment(3)
        )
      )
    ).id

    val mainAppointment = recurringAppointmentRepository.findRecurringAppointmentByRelatedAppointmentsContains(
      RelatedAppointment(3)
    ).orElseThrow()

    assertThat(mainAppointment)
      .extracting("id", "repeatPeriod", "count")
      .contains(recurringAppointmentBookingId, RepeatPeriod.FORTNIGHTLY, 1L)
  }
}
