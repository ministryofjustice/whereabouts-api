package uk.gov.justice.digital.hmpps.whereabouts.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.controllers.VideoLinkAppointmentMigrationResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkBookingMigrationService.Outcome.BOOKING_DELETED
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkBookingMigrationService.Outcome.MIGRATED_NO_MODIFICATIONS
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkBookingMigrationService.Outcome.POST_DELETED
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkBookingMigrationService.Outcome.PRE_DELETED
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional
class VideoLinkBookingMigrationService(

  val videoLinkBookingRepository: VideoLinkBookingRepository,
  val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
  val prisonApiService: PrisonApiService,
) {
  enum class Outcome { MIGRATED_NO_MODIFICATIONS, BOOKING_DELETED, PRE_DELETED, POST_DELETED }

  fun migrateFromNomis(fromDate: LocalDate, toDate: LocalDate): VideoLinkAppointmentMigrationResponse {
    var videoLinkAppointments = videoLinkAppointmentRepository.findAllByStartDateTimeBetweenAndHearingTypeIs(
      LocalDateTime.of(fromDate, LocalTime.MIN),
      LocalDateTime.of(toDate, LocalTime.MAX),
      HearingType.MAIN
    )
    val outcomes = videoLinkAppointments.flatMap { v -> updateVideoLink(v.videoLinkBooking) }

    return VideoLinkAppointmentMigrationResponse(
      videoLinkAppointments.size,
      outcomes.groupingBy { it }.eachCount()
    )
  }

  fun updateVideoLink(videoLinkBooking: VideoLinkBooking): List<Outcome> {
    val mainAppointment = videoLinkBooking.appointments[HearingType.MAIN]
    val preAppointment = videoLinkBooking.appointments[HearingType.PRE]
    val postAppointment = videoLinkBooking.appointments[HearingType.POST]

    val outcomes = mutableListOf<Outcome>()
    val nomisMainAppointment = updateAppointment(mainAppointment!!)
    if (nomisMainAppointment == null) {
      log.info("Deleting booking: ${videoLinkBooking.id}")
      videoLinkBookingRepository.delete(videoLinkBooking)
      outcomes.add(BOOKING_DELETED)
    } else {
      videoLinkBooking.prisonId = nomisMainAppointment.agencyId
      videoLinkBooking.comment = nomisMainAppointment.comment

      if (preAppointment != null && updateAppointment(preAppointment) == null) {
        videoLinkBooking.appointments.remove(HearingType.PRE)
        outcomes.add(PRE_DELETED)
      }
      if (postAppointment != null && updateAppointment(postAppointment) == null) {
        videoLinkBooking.appointments.remove(HearingType.POST)
        outcomes.add(POST_DELETED)
      }
      videoLinkBookingRepository.save(videoLinkBooking)
    }
    return if (outcomes.isEmpty()) listOf(MIGRATED_NO_MODIFICATIONS) else outcomes
  }

  private fun updateAppointment(appointment: VideoLinkAppointment): PrisonAppointment? {
    val nomisAppointment = prisonApiService.getPrisonAppointment(appointment.appointmentId)
    if (nomisAppointment == null) {
      log.info("Deleting appointment: ${appointment.appointmentId}")
      return null
    } else {
      appointment.startDateTime = nomisAppointment.startTime
      appointment.endDateTime = nomisAppointment.endTime!!
      appointment.locationId = nomisAppointment.eventLocationId
      return nomisAppointment
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
