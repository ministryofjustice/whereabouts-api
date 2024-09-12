package uk.gov.justice.digital.hmpps.whereabouts.services.court

import jakarta.persistence.EntityNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoBookingMigrateResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse.LocationTimeslot
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import java.time.LocalDateTime

@Service
class VideoLinkMigrationService(
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val videoLinkBookingEventRepository: VideoLinkBookingEventRepository,
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getVideoLinkBookingToMigrate(videoBookingId: Long): VideoBookingMigrateResponse {
    val videoLinkBooking = videoLinkBookingRepository.findById(videoBookingId)
      .orElseThrow { EntityNotFoundException("Video link booking ID {videoBookingId} was not found") }

    val appointments = videoLinkAppointmentRepository.findAllByVideoLinkBooking(videoLinkBooking)
    require(appointments.isNotEmpty()) { "Video link booking ID $videoBookingId has no appointments" }

    // Extract the different appointments
    val main = appointments.find { it.hearingType == HearingType.MAIN }
    val pre = appointments.find { it.hearingType == HearingType.PRE }
    val post = appointments.find { it.hearingType == HearingType.POST }

    // Fail if the main appointment is not present
    require(main != null) { "Video link booking ID $videoBookingId has no main appointment" }

    val events = videoLinkBookingEventRepository.findEventsByVideoLinkBookingId(videoBookingId)
    require(events.isNotEmpty()) { "Video link booking ID $videoBookingId has no events" }

    return VideoBookingMigrateResponse(
      videoBookingId = videoLinkBooking.id!!,
      offenderBookingId = videoLinkBooking.offenderBookingId,
      courtCode = videoLinkBooking.courtId,
      courtName = videoLinkBooking.courtName,
      madeByTheCourt = videoLinkBooking.madeByTheCourt ?: false,
      prisonCode = videoLinkBooking.prisonId,
      isProbation = videoLinkBooking.courtName?.contains("Probation") ?: false,
      comment = videoLinkBooking.comment,
      pre = pre?.let { mapAppointment(pre) },
      main = mapAppointment(main),
      post = post?.let { mapAppointment(post) },
      events = mapEvents(events),
    )
  }

  private fun mapAppointment(appointment: VideoLinkAppointment) =
    LocationTimeslot(
      locationId = appointment.locationId,
      startTime = appointment.startDateTime,
      endTime = appointment.endDateTime,
    )

  private fun mapEventToLocationTimeSlot(locationId: Long, startTime: LocalDateTime, endTime: LocalDateTime) =
    LocationTimeslot(
      locationId = locationId,
      startTime = startTime,
      endTime = endTime,
    )

  private fun mapEvents(events: List<VideoLinkBookingEvent>): List<VideoBookingEvent> {
    val response = events.map { event ->
      VideoBookingEvent(
        eventId = event.eventId!!,
        eventTime = event.timestamp,
        eventType = event.eventType.toString(),
        createdByUsername = event.userId?.let { event.userId } ?: "MIGRATED",
        prisonCode = event.agencyId?.let { event.agencyId } ?: "UNKNOWN",
        courtCode = event.courtId,
        courtName = event.court,
        madeByTheCourt = event.madeByTheCourt ?: false,
        comment = event.comment,
        pre = event.preLocationId?.let { mapEventToLocationTimeSlot(event.preLocationId!!, event.preStartTime!!, event.preEndTime!!) },
        main = mapEventToLocationTimeSlot(event.mainLocationId!!, event.mainStartTime!!, event.mainEndTime!!),
        post = event.postLocationId?.let { mapEventToLocationTimeSlot(event.postLocationId!!, event.postStartTime!!, event.postEndTime!!) },
      )
    }

    return response
  }
}
