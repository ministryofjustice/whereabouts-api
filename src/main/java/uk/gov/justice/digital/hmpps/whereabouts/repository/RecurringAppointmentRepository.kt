package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.whereabouts.model.MainRecurringAppointment

interface RecurringAppointmentRepository : CrudRepository<MainRecurringAppointment, Long>
