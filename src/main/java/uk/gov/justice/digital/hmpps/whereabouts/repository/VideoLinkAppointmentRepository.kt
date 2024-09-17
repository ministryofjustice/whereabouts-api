package uk.gov.justice.digital.hmpps.whereabouts.repository

import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import java.time.LocalDateTime

interface VideoLinkAppointmentRepository : ReadOnlyRepository<VideoLinkAppointment, Long> {
  override fun findAll(): Set<VideoLinkAppointment>

  fun findVideoLinkAppointmentByAppointmentIdIn(appointmentIds: Set<Long>): Set<VideoLinkAppointment>
  fun findOneByAppointmentId(appointmentId: Long): VideoLinkAppointment?

  fun findAllByStartDateTimeBetweenAndHearingTypeIsAndVideoLinkBookingCourtIdIsAndVideoLinkBookingPrisonIdIn(
    startDateTime: LocalDateTime,
    endDateTime: LocalDateTime,
    hearingType: HearingType,
    courtId: String,
    prisonIds: List<String>,
  ): Set<VideoLinkAppointment>

  fun findAllByHearingTypeIsAndStartDateTimeIsAfterAndVideoLinkBookingOffenderBookingIdIsAndVideoLinkBookingPrisonIdIs(
    hearingType: HearingType,
    startDateTime: LocalDateTime,
    offenderBookingId: Long,
    prisonId: String,
  ): Set<VideoLinkAppointment>

  fun findAllByVideoLinkBooking(videoLinkBooking: VideoLinkBooking): List<VideoLinkAppointment>
}
