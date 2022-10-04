package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.controllers.VideoLinkAppointmentMigrationResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository

@Service
@Transactional
class VideoLinkBookingMigrationService(

  val videoLinkBookingRepository: VideoLinkBookingRepository,
  val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
  val prisonApiService: PrisonApiService,
) {
  fun migrateFromNomis(batchSize: Int): VideoLinkAppointmentMigrationResponse {

    videoLinkBookingRepository.findByPrisonIdIsNull(PageRequest.of(0, batchSize)).stream()
      .forEach { v -> updateVideoLink(v) }

    return VideoLinkAppointmentMigrationResponse(
      videoLinkAppointmentRepository.countByLocationIdIsNull(),
      videoLinkBookingRepository.countByPrisonIdIsNull(),
    )
  }

  fun updateVideoLink(videoLinkBooking: VideoLinkBooking) {
    val mainAppointment = videoLinkBooking.appointments[HearingType.MAIN]
    val preAppointment = videoLinkBooking.appointments[HearingType.PRE]
    val postAppointment = videoLinkBooking.appointments[HearingType.POST]

    val nomisMainAppointment = updateAppointment(mainAppointment!!)
    if (nomisMainAppointment == null) {
      videoLinkBookingRepository.delete(videoLinkBooking)
    } else {
      videoLinkBooking.prisonId = nomisMainAppointment.agencyId
      videoLinkBooking.comment = nomisMainAppointment.comment

      if (preAppointment?.let { updateAppointment(it) } == null) {
        videoLinkBooking.appointments.remove(HearingType.PRE)
      }
      if (postAppointment?.let { updateAppointment(it) } == null) {
        videoLinkBooking.appointments.remove(HearingType.POST)
      }
      videoLinkBookingRepository.save(videoLinkBooking)
    }
  }

  private fun updateAppointment(appointment: VideoLinkAppointment): PrisonAppointment? {
    val nomisAppointment = prisonApiService.getPrisonAppointment(appointment.appointmentId)
    if (nomisAppointment == null) {
      return null
    } else {
      appointment.startDateTime = nomisAppointment.startTime
      appointment.endDateTime = nomisAppointment.endTime
      appointment.locationId = nomisAppointment.eventLocationId
      return nomisAppointment
    }
  }
}
