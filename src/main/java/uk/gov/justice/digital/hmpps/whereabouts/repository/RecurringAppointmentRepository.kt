package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.whereabouts.model.RecurringAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RelatedAppointment
import java.util.Optional

interface RecurringAppointmentRepository : CrudRepository<RecurringAppointment, Long> {
  fun findRecurringAppointmentByRelatedAppointmentsContains(relatedAppointment: RelatedAppointment): Optional<RecurringAppointment>
}
