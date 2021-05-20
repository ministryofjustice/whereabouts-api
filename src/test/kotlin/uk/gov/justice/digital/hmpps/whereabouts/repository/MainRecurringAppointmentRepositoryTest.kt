package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import uk.gov.justice.digital.hmpps.whereabouts.model.MainRecurringAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RecurringAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RepeatPeriod

@DataJpaTest
class MainRecurringAppointmentRepositoryTest {

  @Autowired
  lateinit var entityManager: TestEntityManager

  @Autowired
  lateinit var recurringAppointmentRepository: RecurringAppointmentRepository

  @Test
  fun `can retrieve recurring appointment booking`() {

    val recurringAppointmentBookingId = entityManager.persist(
      MainRecurringAppointment(
        repeatPeriod = RepeatPeriod.Fortnightly, count = 1,
        recurringAppointments = listOf(
          RecurringAppointment(1)
        )
      )
    ).id

    val latest = recurringAppointmentRepository.findById(recurringAppointmentBookingId).orElseThrow()

    assertThat(latest)
      .extracting("id", "repeatPeriod", "count")
      .contains(recurringAppointmentBookingId, RepeatPeriod.Fortnightly, 1L)

    assertThat(latest.recurringAppointments).containsExactlyElementsOf(listOf(RecurringAppointment(1L)))
  }
}
