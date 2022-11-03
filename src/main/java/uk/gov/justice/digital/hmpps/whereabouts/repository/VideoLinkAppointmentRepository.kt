package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import java.time.LocalDateTime

interface VideoLinkAppointmentRepository : CrudRepository<VideoLinkAppointment, Long> {
  override fun findAll(): Set<VideoLinkAppointment>

  fun findVideoLinkAppointmentByAppointmentIdIn(appointmentIds: Set<Long>): Set<VideoLinkAppointment>
  fun findOneByAppointmentId(appointmentId: Long): VideoLinkAppointment?

  fun findAllByStartDateTimeBetweenAndHearingTypeIsAndVideoLinkBookingCourtIdIsAndVideoLinkBookingPrisonIdIn(
    startDateTime: LocalDateTime,
    endDateTime: LocalDateTime,
    hearingType: HearingType,
    courtId: String,
    prisonIds: List<String>
  ): Set<VideoLinkAppointment>
}
