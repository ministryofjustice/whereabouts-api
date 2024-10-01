package uk.gov.justice.digital.hmpps.whereabouts.services.court

import jakarta.persistence.EntityNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentLocationTimeSlot
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoBookingMigrateEvent
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoBookingMigrateResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.OutboundEvent
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.OutboundEventsService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis

@Service
@Transactional(readOnly = true)
class VideoLinkMigrationService(
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val videoLinkBookingEventRepository: VideoLinkBookingEventRepository,
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
  private val outboundEventsService: OutboundEventsService,
  private val courtService: CourtService,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  /**
   * This service is only used in the migration process for video link bookings and not used in any user flow.
   * It can be removed along with other video link booking features when that migration has been completed.
   */
  @Async("asyncExecutor")
  fun migrateVideoLinkBookingsSinceDate(
    fromDate: LocalDate,
    pageSize: Int,
    waitMillis: Long,
  ): CompletableFuture<MigrationSummary> {
    val task = CompletableFuture<MigrationSummary>()
    var numberOfEventsRaised = 0
    var pageRequest = PageRequest.of(0, pageSize)

    val elapsed = measureTimeMillis {
      var page = videoLinkBookingEventRepository.findAllByMainStartTimeGreaterThanAndEventTypeEquals(
        fromDate.atStartOfDay(),
        VideoLinkBookingEventType.CREATE,
        pageRequest,
      )

      val numberOfBookings = page.totalElements
      val numberOfPages = page.totalPages

      log.info("Starting migration of video link bookings from $fromDate at ${LocalDateTime.now()}")
      log.info("Estimated rows $numberOfBookings in $numberOfPages pages")

      while (page.hasContent()) {
        page.content.map {
          outboundEventsService.send(OutboundEvent.VIDEO_LINK_BOOKING_MIGRATE, it.videoLinkBookingId)
          numberOfEventsRaised++
        }

        if (page.isLast) {
          break
        }

        if (waitMillis > 0) {
          Thread.sleep(waitMillis)
        }

        pageRequest = pageRequest.next()

        page = videoLinkBookingEventRepository.findAllByMainStartTimeGreaterThanAndEventTypeEquals(
          fromDate.atStartOfDay(),
          VideoLinkBookingEventType.CREATE,
          pageRequest,
        )
      }

      task.complete(MigrationSummary(numberOfBookings, numberOfPages, numberOfEventsRaised))
    }

    log.info("End migration of video link bookings - took ${elapsed}ms")

    return task
  }

  fun getVideoLinkBookingToMigrate(videoBookingId: Long): VideoBookingMigrateResponse {
    // Get the events for this booking - deleted bookings have no video_link_booking or video_link_appointments
    val eventEntities = videoLinkBookingEventRepository.findEventsByVideoLinkBookingId(videoBookingId)
    require(eventEntities.isNotEmpty()) { "Video link booking ID $videoBookingId has no events" }

    val cancelledBooking = eventEntities.any { it.eventType == VideoLinkBookingEventType.DELETE }

    return if (cancelledBooking) {
      cancelledVideoLinkBooking(videoBookingId, eventEntities)
    } else {
      activeVideoLinkBooking(videoBookingId, eventEntities)
    }
  }

  private fun cancelledVideoLinkBooking(
    videoBookingId: Long,
    eventEntities: List<VideoLinkBookingEvent>,
  ): VideoBookingMigrateResponse {
    val createEvent = eventEntities.first { it.eventType == VideoLinkBookingEventType.CREATE }
    val lastChangeEvent = eventEntities.sortedBy { it.timestamp }.last { it.eventType != VideoLinkBookingEventType.DELETE }

    // Reconstruct the appointment detail from the latest CREATE or UPDATE event
    val appointmentsFromEvent = extractAppointmentsFromEvent(lastChangeEvent)

    // Reconstruct the booking details from the latest CREATE and UPDATE events (create can contain more detail)
    val bookingDetails = extractBookingFromEvents(createEvent, lastChangeEvent)

    return VideoBookingMigrateResponse(
      videoBookingId = videoBookingId,
      offenderBookingId = bookingDetails.offenderBookingId,
      courtCode = bookingDetails.courtId ?: "UNKNOWN",
      courtName = bookingDetails.courtName,
      madeByTheCourt = bookingDetails.madeByTheCourt,
      createdByUsername = bookingDetails.createdByUsername,
      prisonCode = bookingDetails.prisonId,
      probation = bookingDetails.isProbationBooking(),
      cancelled = true,
      comment = bookingDetails.comment,
      pre = appointmentsFromEvent.pre,
      main = appointmentsFromEvent.main!!,
      post = appointmentsFromEvent.post,
      events = mapEvents(eventEntities),
    )
  }

  private fun BookingDetails.isProbationBooking() =
    courtName?.uppercase()?.contains("PROBATION") == true ||
      courtService.getCourtNameForCourtId(courtId)?.uppercase()?.contains("PROBATION") == true

  private fun activeVideoLinkBooking(
    videoBookingId: Long,
    eventEntities: List<VideoLinkBookingEvent>,
  ): VideoBookingMigrateResponse {
    val videoLinkBooking = videoLinkBookingRepository.findById(videoBookingId)
      .orElseThrow { EntityNotFoundException("Video link booking ID $videoBookingId was not found") }

    val appointments = videoLinkAppointmentRepository.findAllByVideoLinkBooking(videoLinkBooking)
    require(appointments.isNotEmpty()) { "Video link booking ID $videoBookingId has no appointments" }
    require((appointments.size <= 3)) { "Video link booking ID $videoBookingId has more than 3 appointments" }

    // Extract the related appointments, if they exist - but pre and post may be null
    val main = appointments.find { it.hearingType == HearingType.MAIN }
    val pre = appointments.find { it.hearingType == HearingType.PRE }
    val post = appointments.find { it.hearingType == HearingType.POST }

    require(main != null) { "Video link booking ID $videoBookingId has no main appointment" }

    return VideoBookingMigrateResponse(
      videoBookingId = videoBookingId,
      offenderBookingId = videoLinkBooking.offenderBookingId,
      courtCode = videoLinkBooking.courtId ?: "UNKNOWN",
      courtName = videoLinkBooking.courtName,
      madeByTheCourt = videoLinkBooking.madeByTheCourt == true,
      createdByUsername = videoLinkBooking.createdByUsername ?: "MIGRATED",
      prisonCode = videoLinkBooking.prisonId,
      probation = videoLinkBooking.isProbationBooking(),
      cancelled = false,
      comment = videoLinkBooking.comment,
      pre = pre?.let { mapAppointment(pre) },
      main = mapAppointment(main),
      post = post?.let { mapAppointment(post) },
      events = mapEvents(eventEntities),
    )
  }

  private fun VideoLinkBooking.isProbationBooking() =
    courtName?.uppercase()?.contains("PROBATION") == true ||
      courtService.getCourtNameForCourtId(courtId)?.uppercase()?.contains("PROBATION") == true

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
    val lastChangeEvent = events.sortedBy { it.timestamp }.last { it.eventType != VideoLinkBookingEventType.DELETE }

    return events.map { event ->
      VideoBookingMigrateEvent(
        eventId = event.eventId!!,
        eventTime = event.timestamp,
        eventType = event.eventType,
        createdByUsername = event.userId ?: "MIGRATED",
        prisonCode = event.agencyId ?: "UNKNOWN",
        courtCode = event.courtId ?: "UNKNOWN",
        courtName = event.court,
        madeByTheCourt = event.madeByTheCourt == true,
        comment = event.comment,
        pre = event.preLocationId?.let {
          mapEventToLocationTimeSlot(
            locationId = event.preLocationId!!,
            startTime = event.preStartTime!!,
            endTime = event.preEndTime!!,
          )
        },
        main = mapEventToLocationTimeSlot(
          locationId = event.mainLocationId ?: lastChangeEvent.mainLocationId!!,
          startTime = event.mainStartTime ?: lastChangeEvent.mainStartTime!!,
          endTime = event.mainEndTime ?: lastChangeEvent.mainEndTime!!,
        ),
        post = event.postLocationId?.let {
          mapEventToLocationTimeSlot(
            locationId = event.postLocationId!!,
            startTime = event.postStartTime!!,
            endTime = event.postEndTime!!,
          )
        },
      )
    }
  }

  private fun extractAppointmentsFromEvent(latestEvent: VideoLinkBookingEvent): AppointmentsFromEvent {
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

  private fun extractBookingFromEvents(createEvent: VideoLinkBookingEvent, lastChangeEvent: VideoLinkBookingEvent) =
    BookingDetails(
      offenderBookingId = createEvent.offenderBookingId!!,
      courtId = lastChangeEvent.courtId,
      courtName = lastChangeEvent.court,
      madeByTheCourt = lastChangeEvent.madeByTheCourt!!,
      prisonId = lastChangeEvent.agencyId!!,
      createdByUsername = createEvent.userId!!,
      comment = lastChangeEvent.comment,
    )
}

data class AppointmentsFromEvent(
  val pre: AppointmentLocationTimeSlot? = null,
  val main: AppointmentLocationTimeSlot? = null,
  val post: AppointmentLocationTimeSlot? = null,
)

data class BookingDetails(
  val offenderBookingId: Long,
  val courtId: String? = null,
  val courtName: String? = null,
  val madeByTheCourt: Boolean = true,
  val prisonId: String,
  val createdByUsername: String,
  val comment: String? = null,
)

data class MigrationSummary(
  val numberOfBookings: Long,
  val numberOfPages: Int,
  val numberOfEventsRaised: Int,
)
