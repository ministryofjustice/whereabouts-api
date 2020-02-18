package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment

@Repository
interface VideoLinkAppointmentRepository : CrudRepository<VideoLinkAppointment, Long> {
  override fun findAll(): Set<VideoLinkAppointment>
  fun findVideoLinkAppointmentByAppointmentIdIn(appointmentIds: Set<Long>): Set<VideoLinkAppointment>
}



