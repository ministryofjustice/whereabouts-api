package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.whereabouts.model.CourtAppointment

@Repository
interface CourtAppointmentRepository : CrudRepository<CourtAppointment, Long> {
  override fun findAll(): Set<CourtAppointment>
  fun findCourtAppointmentByAppointmentIdIn(appointmentIds: Set<Long>): Set<CourtAppointment>
}



