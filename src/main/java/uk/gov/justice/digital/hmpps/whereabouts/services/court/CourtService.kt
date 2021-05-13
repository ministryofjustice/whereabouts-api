package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.ValidationException
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException

const val VIDEO_LINK_APPOINTMENT_TYPE = "VLB"

@Service
class CourtService(
  private val prisonApiService: PrisonApiService,
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val clock: Clock,
  private val videoLinkBookingEventListener: VideoLinkBookingEventListener,
  @Value("\${courts}") private val courts: String,
  @Value("\${courtIds}") private val courtIds: String,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getCourtLocations() = courts.split(",").toSet()

  fun getCourtIds() = courtIds.split(",").toSet()

  @Transactional(readOnly = true)
  fun getVideoLinkAppointments(appointmentIds: Set<Long>): Set<VideoLinkAppointmentDto> {
    return videoLinkAppointmentRepository
      .findVideoLinkAppointmentByAppointmentIdIn(appointmentIds)
      .asSequence()
      .map {
        VideoLinkAppointmentDto(
          id = it.id!!,
          bookingId = it.bookingId,
          appointmentId = it.appointmentId,
          hearingType = it.hearingType,
          court = it.court,
          createdByUsername = it.createdByUsername,
          madeByTheCourt = it.madeByTheCourt
        )
      }.toSet()
  }

  @Transactional
  fun createVideoLinkBooking(specification: VideoLinkBookingSpecification): Long {
    specification.validate()
    val bookingId = specification.bookingId!!
    val comment = specification.comment
    val mainEvent = savePrisonAppointment(bookingId, comment, specification.main)
    val preEvent = specification.pre?.let { savePrisonAppointment(bookingId, comment, it) }
    val postEvent = specification.post?.let { savePrisonAppointment(bookingId, comment, it) }

    val videoLinkBooking = VideoLinkBooking(
      pre = preEvent?.let { toAppointment(preEvent.eventId, HearingType.PRE, specification) },
      main = toAppointment(mainEvent.eventId, HearingType.MAIN, specification),
      post = postEvent?.let { toAppointment(postEvent.eventId, HearingType.POST, specification) }
    )
    val agencyId = mainEvent.agencyId

    val persistentBooking = videoLinkBookingRepository.save(videoLinkBooking)!!

    videoLinkBookingEventListener.bookingCreated(persistentBooking, specification, agencyId)

    return persistentBooking.id!!
  }

  @Transactional
  fun updateVideoLinkBooking(
    videoBookingId: Long,
    specification: VideoLinkBookingUpdateSpecification
  ): VideoLinkBooking {
    specification.validate()
    val booking = videoLinkBookingRepository
      .findById(videoBookingId)
      .orElseThrow {
        EntityNotFoundException("Video link booking with id $videoBookingId not found")
      }

    booking.pre?.let { prisonApiService.deleteAppointment(it.appointmentId) }
    booking.main.let { prisonApiService.deleteAppointment(it.appointmentId) }
    booking.post?.let { prisonApiService.deleteAppointment(it.appointmentId) }

    val bookingId = booking.main.bookingId
    val comment = specification.comment

    val mainEvent = savePrisonAppointment(bookingId, comment, specification.main)
    val preEvent = specification.pre?.let { savePrisonAppointment(bookingId, comment, it) }
    val postEvent = specification.post?.let { savePrisonAppointment(bookingId, comment, it) }

    val court = booking.main.court
    val madeByTheCourt = booking.main.madeByTheCourt

    booking.main = VideoLinkAppointment(
      bookingId = bookingId,
      appointmentId = mainEvent.eventId,
      court = court,
      hearingType = HearingType.MAIN,
      madeByTheCourt = madeByTheCourt
    )

    booking.pre = preEvent?.let {
      VideoLinkAppointment(
        bookingId = bookingId,
        appointmentId = it.eventId,
        court = court,
        hearingType = HearingType.PRE,
        madeByTheCourt = madeByTheCourt
      )
    }

    booking.post = postEvent?.let {
      VideoLinkAppointment(
        bookingId = bookingId,
        appointmentId = it.eventId,
        court = court,
        hearingType = HearingType.POST,
        madeByTheCourt = madeByTheCourt
      )
    }
    /**
     * Ensure that the new VideoLinkAppointment objects are persistent
     * and so have ids before the ApplicationInsightsEventListener is called.
     */
    videoLinkBookingRepository.flush()
    videoLinkBookingEventListener.bookingUpdated(booking, specification)
    return booking
  }

  private fun savePrisonAppointment(
    bookingId: Long,
    comment: String?,
    appointmentSpec: VideoLinkAppointmentSpecification
  ): Event = prisonApiService.postAppointment(
    bookingId,
    CreateBookingAppointment(
      appointmentType = VIDEO_LINK_APPOINTMENT_TYPE,
      locationId = appointmentSpec.locationId!!,
      startTime = appointmentSpec.startTime.toString(),
      endTime = appointmentSpec.endTime.toString(),
      comment = comment
    )
  )

  private fun toAppointment(id: Long, type: HearingType, specification: VideoLinkBookingSpecification) =
    VideoLinkAppointment(
      bookingId = specification.bookingId!!,
      appointmentId = id,
      court = specification.court,
      hearingType = type,
      madeByTheCourt = specification.madeByTheCourt
    )

  @Transactional(readOnly = true)
  fun getVideoLinkBooking(videoBookingId: Long): VideoLinkBookingResponse {
    val booking = videoLinkBookingRepository.findById(videoBookingId).orElseThrow {
      EntityNotFoundException("Video link booking with id $videoBookingId not found")
    }
    val mainEvent = prisonApiService.getPrisonAppointment(booking.main.appointmentId)
      ?: throw EntityNotFoundException("main appointment with id ${booking.main.appointmentId} not found in NOMIS")
    val preEvent = booking.pre?.let { prisonApiService.getPrisonAppointment(it.appointmentId) }
    val postEvent = booking.post?.let { prisonApiService.getPrisonAppointment(it.appointmentId) }

    return VideoLinkBookingResponse(
      videoLinkBookingId = videoBookingId,
      bookingId = booking.main.bookingId,
      agencyId = mainEvent.agencyId,
      court = booking.main.court,
      comment = mainEvent.comment,
      pre = preEvent?.let {
        VideoLinkBookingResponse.LocationTimeslot(
          locationId = it.eventLocationId,
          startTime = it.startTime,
          endTime = it.endTime
        )
      },
      main = VideoLinkBookingResponse.LocationTimeslot(
        locationId = mainEvent.eventLocationId,
        startTime = mainEvent.startTime,
        endTime = mainEvent.endTime
      ),
      post = postEvent?.let {
        VideoLinkBookingResponse.LocationTimeslot(
          locationId = it.eventLocationId,
          startTime = it.startTime,
          endTime = it.endTime
        )
      }
    )
  }

  @Transactional
  fun deleteVideoLinkBooking(videoBookingId: Long): VideoLinkBooking {
    val booking = videoLinkBookingRepository.findById(videoBookingId).orElseThrow {
      EntityNotFoundException("Video link booking with id $videoBookingId not found")
    }

    booking.toAppointments().forEach { prisonApiService.deleteAppointment(it.appointmentId) }
    videoLinkBookingRepository.deleteById(booking.id!!)
    videoLinkBookingEventListener.bookingDeleted(booking)
    return booking
  }

  @Transactional(readOnly = true)
  fun getVideoLinkBookingsForPrisonAndDateAndCourt(
    agencyId: String,
    date: LocalDate,
    court: String?
  ): List<VideoLinkBookingResponse> {
    val scheduledAppointments = prisonApiService
      .getScheduledAppointmentsByAgencyAndDate(agencyId, date, null, null)
      .filter { it.appointmentTypeCode == "VLB" }

    val scheduledAppointmentIds = scheduledAppointments.map { it.id }

    val bookings = videoLinkBookingRepository.findByMainAppointmentIds(scheduledAppointmentIds)

    val scheduledAppointmentsById = scheduledAppointments.associateBy { it.id }

    return bookings
      .filter { scheduledAppointmentsById.containsKey(it.main.appointmentId) }
      .filter { if (court == null) true else it.main.court == court }
      .filter { hasAnEndDate(scheduledAppointmentsById[it.main.appointmentId]!!) }
      .map { b ->
        val prisonMain = scheduledAppointmentsById[b.main.appointmentId]!!
        VideoLinkBookingResponse(
          videoLinkBookingId = b.id!!,
          bookingId = b.main.bookingId,
          agencyId = prisonMain.agencyId,
          court = b.main.court,
          main = toVideoLinkAppointmentDto(prisonMain)!!,
          pre = toVideoLinkAppointmentDto(scheduledAppointmentsById[b.pre?.appointmentId]),
          post = toVideoLinkAppointmentDto(scheduledAppointmentsById[b.post?.appointmentId])
        )
      }
  }

  fun updateVideoLinkBookingComment(videoLinkBookingId: Long, comment: String?) {
    val booking = videoLinkBookingRepository.findById(videoLinkBookingId).orElseThrow {
      EntityNotFoundException("Video link booking with id $videoLinkBookingId not found")
    }
    booking.main.apply { prisonApiService.updateAppointmentComment(appointmentId, comment) }
    booking.pre?.apply { prisonApiService.updateAppointmentComment(appointmentId, comment) }
    booking.post?.apply { prisonApiService.updateAppointmentComment(appointmentId, comment) }
  }

  private fun toVideoLinkAppointmentDto(scheduledAppointment: ScheduledAppointmentDto?) =
    scheduledAppointment?.takeIf { hasAnEndDate(it) }?.let {
      VideoLinkBookingResponse.LocationTimeslot(
        locationId = it.locationId,
        startTime = it.startTime,
        endTime = it.endTime!!
      )
    }

  private fun hasAnEndDate(it: ScheduledAppointmentDto): Boolean {
    val hasEndDate = it.endTime != null
    if (!hasEndDate) log.error("Appointment with id ${it.id} has no end date")
    return hasEndDate
  }

  private fun VideoLinkBookingSpecification.validate() {
    main.validate("Main")
    pre?.validate("Pre")
    post?.validate("Post")
  }

  private fun VideoLinkBookingUpdateSpecification.validate() {
    main.validate("Main")
    pre?.validate("Pre")
    post?.validate("Post")
  }

  private fun VideoLinkAppointmentSpecification.validate(prefix: String) {
    if (startTime.isBefore(LocalDateTime.now(clock))) {
      throw ValidationException("$prefix appointment start time must be in the future.")
    }
    if (!startTime.isBefore(endTime)) {
      throw ValidationException("$prefix appointment start time must precede end time.")
    }
    locationId?.let {
      prisonApiService.getLocation(it) ?: throw ValidationException("$prefix locationId $it not found in NOMIS.")
    }
  }
}
