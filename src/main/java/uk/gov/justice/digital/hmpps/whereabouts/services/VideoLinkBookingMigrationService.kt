package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.controllers.VideoLinkAppointmentMigrationResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository

@Service
class VideoLinkBookingMigrationService(

  val videoLinkBookingRepository: VideoLinkBookingRepository,
  val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
  val prisonApiService: PrisonApiService,
) {
  fun migrateFromNomis(batchSize: Int): VideoLinkAppointmentMigrationResponse {

    videoLinkBookingRepository.findByPrisonIdisNull(PageRequest.of(0, batchSize)).stream().forEach { v -> updateVideoLink(v) }

    return VideoLinkAppointmentMigrationResponse(
      videoLinkAppointmentRepository.countByLocationIdisNull(),
      videoLinkBookingRepository.countByPrisonIdisNull(),
    )
  }

  fun updateVideoLink(videoLinkBooking: VideoLinkBooking) {
    val mainAppointment = videoLinkBooking.appointments.get(HearingType.MAIN)
    val perAppointment = videoLinkBooking.appointments.get(HearingType.PRE)
    val postAppointment = videoLinkBooking.appointments.get(HearingType.POST)
    if (mainAppointment == null) {
      videoLinkAppointmentRepository.deleteAll(videoLinkBooking.appointments.values)
      videoLinkBookingRepository.delete(videoLinkBooking)
    } else {
      val nomisMainAppointment = updateAppointment(mainAppointment)
      if (nomisMainAppointment == null) {
        videoLinkAppointmentRepository.deleteAll(videoLinkBooking.appointments.values)
        videoLinkBookingRepository.delete(videoLinkBooking)
      } else {
        videoLinkBooking.prisonId = nomisMainAppointment.agencyId
        videoLinkBooking.comment = nomisMainAppointment.comment
        videoLinkBookingRepository.save(videoLinkBooking)
        perAppointment?.let { updateAppointment(it) }
        postAppointment?.let { updateAppointment(it) }
      }
    }
  }

  private fun updateAppointment(appointment: VideoLinkAppointment): PrisonAppointment? {
    val nomisAppointment = prisonApiService.getPrisonAppointment(appointment.appointmentId)
    if (nomisAppointment == null) {
      videoLinkAppointmentRepository.delete(appointment)
      return null
    } else {
      appointment.startDateTime = nomisAppointment.startTime
      appointment.endDateTime = nomisAppointment.endTime
      appointment.locationId = nomisAppointment.eventLocationId
      videoLinkAppointmentRepository.save(appointment)
      return nomisAppointment
    }
  }
}
