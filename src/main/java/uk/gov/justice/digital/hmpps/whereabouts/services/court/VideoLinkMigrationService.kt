package uk.gov.justice.digital.hmpps.whereabouts.services.court

import jakarta.persistence.EntityNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentLocationTimeSlot
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoBookingMigrateEvent
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoBookingMigrateResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class VideoLinkMigrationService(
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val videoLinkBookingEventRepository: VideoLinkBookingEventRepository,
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getVideoLinkBookingToMigrate(videoBookingId: Long): VideoBookingMigrateResponse {
    val videoLinkBooking = videoLinkBookingRepository.findById(videoBookingId)
      .orElseThrow { EntityNotFoundException("Video link booking ID {videoBookingId} was not found") }

    log.info("Migrating video link booking ID $videoBookingId")

    // Get the history of events for this booking
    val eventEntities = videoLinkBookingEventRepository.findEventsByVideoLinkBookingId(videoBookingId)
    require(eventEntities.isNotEmpty()) { "Video link booking ID $videoBookingId has no events" }

    // Is this a cancelled booking? i.e. containing a DELETE event.
    val cancelledBooking = eventEntities.any { it.eventType == VideoLinkBookingEventType.DELETE }

    // Get the appointments linked to the booking - cancelled bookings will have none (they get removed)
    val appointments = videoLinkAppointmentRepository.findAllByVideoLinkBooking(videoLinkBooking)

    if (!cancelledBooking) {
      require(appointments.isNotEmpty()) { "Video link booking ID $videoBookingId has no appointments" }
      require((appointments.size <= 3)) { "Video link booking ID $videoBookingId has more than 3 appointments" }
    }

    // Extract the related appointments - will be null if a cancelled booking otherwise reflect the appointments
    val main = appointments.find { it.hearingType == HearingType.MAIN }
    val pre = appointments.find { it.hearingType == HearingType.PRE }
    val post = appointments.find { it.hearingType == HearingType.POST }

    // Extract appointment details from the latest CREATE or UPDATE event if this booking is cancelled
    val appointmentsFromEvent = extractAppointmentsFromEvent(eventEntities, cancelledBooking)

    if (!cancelledBooking) {
      require(main != null) { "Video link booking ID $videoBookingId has no main appointment" }
    }

    return VideoBookingMigrateResponse(
      videoBookingId = videoLinkBooking.id!!,
      offenderBookingId = videoLinkBooking.offenderBookingId,
      courtCode = videoLinkBooking.courtId?.let { videoLinkBooking.courtId } ?: "UNKNOWN",
      courtName = videoLinkBooking.courtName,
      madeByTheCourt = videoLinkBooking.madeByTheCourt?.let { videoLinkBooking.madeByTheCourt } ?: false,
      createdByUsername = videoLinkBooking.createdByUsername?.let { videoLinkBooking.createdByUsername } ?: "MIGRATED",
      prisonCode = videoLinkBooking.prisonId,
      probation = videoLinkBooking.courtName?.contains("Probation") ?: false,
      cancelled = cancelledBooking,
      comment = videoLinkBooking.comment,
      pre = if (!cancelledBooking) pre?.let { mapAppointment(pre) } else appointmentsFromEvent.pre,
      main = if (!cancelledBooking) mapAppointment(main!!) else appointmentsFromEvent.main!!,
      post = if (!cancelledBooking) post?.let { mapAppointment(post) } else appointmentsFromEvent.post,
      events = mapEvents(eventEntities),
    )
  }

  fun mapAppointment(appointment: VideoLinkAppointment) =
    AppointmentLocationTimeSlot(
      locationId = appointment.locationId,
      date = LocalDate.from(appointment.startDateTime),
      startTime = LocalTime.from(appointment.startDateTime).withSecond(0).withNano(0),
      endTime = LocalTime.from(appointment.endDateTime).withSecond(0).withNano(0),
    )

  fun mapEventToLocationTimeSlot(locationId: Long, startTime: LocalDateTime, endTime: LocalDateTime) =
    AppointmentLocationTimeSlot(
      locationId = locationId,
      date = LocalDate.of(startTime.year, startTime.month, startTime.dayOfMonth),
      startTime = LocalTime.of(startTime.hour, startTime.minute),
      endTime = LocalTime.of(endTime.hour, endTime.minute),
    )

  private fun mapEvents(events: List<VideoLinkBookingEvent>): List<VideoBookingMigrateEvent> {
    val response = events.map { event ->
      VideoBookingMigrateEvent(
        eventId = event.eventId!!,
        eventTime = event.timestamp,
        eventType = event.eventType,
        createdByUsername = event.userId?.let { event.userId } ?: "MIGRATED",
        prisonCode = event.agencyId?.let { event.agencyId } ?: "UNKNOWN",
        courtCode = event.courtId?.let { event.courtId } ?: "UNKNOWN",
        courtName = event.court,
        madeByTheCourt = event.madeByTheCourt?.let { event.madeByTheCourt } ?: false,
        comment = event.comment,
        pre = event.preLocationId?.let { mapEventToLocationTimeSlot(event.preLocationId!!, event.preStartTime!!, event.preEndTime!!) },
        main = mapEventToLocationTimeSlot(event.mainLocationId!!, event.mainStartTime!!, event.mainEndTime!!),
        post = event.postLocationId?.let { mapEventToLocationTimeSlot(event.postLocationId!!, event.postStartTime!!, event.postEndTime!!) },
      )
    }

    return response
  }

  private fun extractAppointmentsFromEvent(
    eventEntities: List<VideoLinkBookingEvent>,
    cancelled: Boolean,
  ): AppointmentsFromEvent {
    val latestEvent = eventEntities.sortedBy { it.timestamp }.last { it.eventType != VideoLinkBookingEventType.DELETE }
    if (!cancelled) {
      return AppointmentsFromEvent()
    }
    val pre = latestEvent.preLocationId?.let {
      mapEventToLocationTimeSlot(it, latestEvent.preStartTime!!, latestEvent.preEndTime!!)
    }
    val main = latestEvent.mainLocationId?.let {
      mapEventToLocationTimeSlot(latestEvent.mainLocationId!!, latestEvent.mainStartTime!!, latestEvent.mainEndTime!!)
    }
    val post = latestEvent.postLocationId?.let {
      mapEventToLocationTimeSlot(it, latestEvent.postStartTime!!, latestEvent.postEndTime!!)
    }
    return AppointmentsFromEvent(pre, main, post)
  }
}

data class AppointmentsFromEvent(
  val pre: AppointmentLocationTimeSlot? = null,
  val main: AppointmentLocationTimeSlot? = null,
  val post: AppointmentLocationTimeSlot? = null,
)
