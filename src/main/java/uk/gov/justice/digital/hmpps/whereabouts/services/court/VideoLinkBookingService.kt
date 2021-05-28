package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
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
class VideoLinkBookingService(
  private val courtService: CourtService,
  private val prisonApiService: PrisonApiService,
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val clock: Clock,
  private val videoLinkBookingEventListener: VideoLinkBookingEventListener
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getVideoLinkAppointments(appointmentIds: Set<Long>): List<VideoLinkAppointmentDto> {
    return videoLinkAppointmentRepository
      .findVideoLinkAppointmentByAppointmentIdIn(appointmentIds)
      .map {
        VideoLinkAppointmentDto(
          id = it.id!!,
          bookingId = it.videoLinkBooking.offenderBookingId,
          appointmentId = it.appointmentId,
          hearingType = it.hearingType,
          court = courtService.chooseCourtName(it.videoLinkBooking),
          courtId = it.videoLinkBooking.courtId,
          createdByUsername = it.videoLinkBooking.createdByUsername,
          madeByTheCourt = it.videoLinkBooking.madeByTheCourt
        )
      }
  }

  @Transactional
  fun createVideoLinkBooking(specification: VideoLinkBookingSpecification): Long {
    specification.validate()
    val bookingId = specification.bookingId!!
    val comment = specification.comment
    val mainEvent = savePrisonAppointment(bookingId, comment, specification.main)
    val preEvent = specification.pre?.let { savePrisonAppointment(bookingId, comment, it) }
    val postEvent = specification.post?.let { savePrisonAppointment(bookingId, comment, it) }

    val courtId = specification.courtId ?: specification.court?.let { courtService.findId(it) }

    val videoLinkBooking = VideoLinkBooking(
      offenderBookingId = bookingId,
      courtName = specification.court,
      courtId = courtId,
      madeByTheCourt = specification.madeByTheCourt
    )
    preEvent?.let { videoLinkBooking.addPreAppointment(it.eventId) }
    videoLinkBooking.addMainAppointment(mainEvent.eventId)
    postEvent?.let { videoLinkBooking.addPostAppointment(it.eventId) }

    val persistentBooking = videoLinkBookingRepository.save(videoLinkBooking)!!

    videoLinkBookingEventListener.bookingCreated(persistentBooking, specification, mainEvent.agencyId)

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

    booking.appointments.values.forEach { prisonApiService.deleteAppointment(it.appointmentId) }

    val bookingId = booking.offenderBookingId
    val comment = specification.comment

    val mainEvent = savePrisonAppointment(bookingId, comment, specification.main)
    val preEvent = specification.pre?.let { savePrisonAppointment(bookingId, comment, it) }
    val postEvent = specification.post?.let { savePrisonAppointment(bookingId, comment, it) }

    with(booking) {

      /**
       * Yuk. flush() forces Hibernate to remove the old appointments (if any) before inserting the new ones.
       * Without this the old appointments aren't removed and we get constraint violations.
       * If VideoLinkBooking was a composite. ie VideoLinkAppointment was a component not an entity this wouldn't
       * be a problem.
       */
      appointments.clear()
      videoLinkBookingRepository.flush()

      addMainAppointment(mainEvent.eventId)
      preEvent?.let { addPreAppointment(it.eventId) }
      postEvent?.let { addPostAppointment(it.eventId) }
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

  @Transactional(readOnly = true)
  fun getVideoLinkBooking(videoBookingId: Long): VideoLinkBookingResponse {
    val booking = videoLinkBookingRepository.findById(videoBookingId).orElseThrow {
      EntityNotFoundException("Video link booking with id $videoBookingId not found")
    }

    val events =
      booking.appointments.values.associate { it.hearingType to prisonApiService.getPrisonAppointment(it.appointmentId) }

    val mainEvent = events[MAIN]
      ?: throw EntityNotFoundException("main appointment for video link booking id  ${booking.id} not found in NOMIS")

    return VideoLinkBookingResponse(
      videoLinkBookingId = videoBookingId,
      bookingId = booking.offenderBookingId,
      agencyId = mainEvent.agencyId,
      court = courtService.chooseCourtName(booking),
      courtId = booking.courtId,
      comment = mainEvent.comment,
      pre = events[PRE]?.let { toLocationTimeslot(it) },
      main = toLocationTimeslot(mainEvent),
      post = events[POST]?.let { toLocationTimeslot(it) }
    )
  }

  private fun toLocationTimeslot(it: PrisonAppointment) =
    VideoLinkBookingResponse.LocationTimeslot(
      locationId = it.eventLocationId,
      startTime = it.startTime,
      endTime = it.endTime!!
    )

  @Transactional
  fun deleteVideoLinkBooking(videoBookingId: Long): VideoLinkBooking {
    val booking = videoLinkBookingRepository.findById(videoBookingId).orElseThrow {
      EntityNotFoundException("Video link booking with id $videoBookingId not found")
    }

    booking.appointments.values.forEach { prisonApiService.deleteAppointment(it.appointmentId) }
    videoLinkBookingRepository.deleteById(booking.id!!)
    videoLinkBookingEventListener.bookingDeleted(booking)
    return booking
  }

  @Transactional(readOnly = true)
  fun getVideoLinkBookingsForPrisonAndDateAndCourt(
    agencyId: String,
    date: LocalDate,
    courtName: String?,
    courtId: String?
  ): List<VideoLinkBookingResponse> {
    val scheduledAppointments = prisonApiService
      .getScheduledAppointments(agencyId, date)
      .filter { it.appointmentTypeCode == "VLB" }
      .filter { hasAnEndDate(it) }

    val scheduledAppointmentIds = scheduledAppointments.map { it.id }

    // If a booking's main appointment doesn't match one of the scheduledAppointmentIds then it is excluded
    val bookings = videoLinkBookingRepository.findByAppointmentIdsAndHearingType(scheduledAppointmentIds, MAIN)

    val scheduledAppointmentsById = scheduledAppointments.associateBy { it.id }

    return bookings
      .filter { it.matchesCourt(courtName, courtId) }
      .map {
        val mainPrisonAppointment = scheduledAppointmentsById[it.appointments[MAIN]?.appointmentId]!!
        VideoLinkBookingResponse(
          videoLinkBookingId = it.id!!,
          bookingId = it.offenderBookingId,
          agencyId = mainPrisonAppointment.agencyId,
          court = courtService.chooseCourtName(it),
          courtId = it.courtId,
          main = toVideoLinkAppointmentDto(mainPrisonAppointment)!!,
          pre = toVideoLinkAppointmentDto(scheduledAppointmentsById[it.appointments[PRE]?.appointmentId]),
          post = toVideoLinkAppointmentDto(scheduledAppointmentsById[it.appointments[POST]?.appointmentId])
        )
      }
  }

  fun updateVideoLinkBookingComment(videoLinkBookingId: Long, comment: String?) {
    val booking = videoLinkBookingRepository.findById(videoLinkBookingId).orElseThrow {
      EntityNotFoundException("Video link booking with id $videoLinkBookingId not found")
    }
    booking.appointments.values.forEach { prisonApiService.updateAppointmentComment(it.appointmentId, comment) }
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
    if ((court.isNullOrBlank()) && (courtId.isNullOrBlank()))
      throw ValidationException("One of court or courtId must be specified")

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
