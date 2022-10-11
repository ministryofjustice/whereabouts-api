package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentsSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiServiceAuditable
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
  private val prisonApiServiceAuditable: PrisonApiServiceAuditable,
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val clock: Clock,
  private val videoLinkBookingEventListener: VideoLinkBookingEventListener
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getVideoLinkAppointments(appointmentIds: Set<Long>): List<VideoLinkAppointmentDto> =
    videoLinkAppointmentRepository
      .findVideoLinkAppointmentByAppointmentIdIn(appointmentIds)
      .map { toVideoLinkAppointmentDto(it) }

  @Transactional
  fun createVideoLinkBooking(specification: VideoLinkBookingSpecification): Long {
    specification.validate()

    val (mainEvent, preEvent, postEvent) = createPrisonAppointments(specification.bookingId!!, specification)

    val videoLinkBooking = VideoLinkBooking(
      offenderBookingId = specification.bookingId,
      courtName = specification.court,
      courtId = getCourtId(specification),
      madeByTheCourt = specification.madeByTheCourt,
      prisonId = mainEvent.agencyId,
      comment = specification.comment,
    )
    videoLinkBooking.addAppointments(mainEvent, preEvent, postEvent)

    val persistentBooking = videoLinkBookingRepository.save(videoLinkBooking)

    videoLinkBookingEventListener.bookingCreated(persistentBooking, specification)

    return persistentBooking.id!!
  }

  @Transactional
  fun updateVideoLinkBooking(
    videoBookingId: Long,
    specification: VideoLinkBookingUpdateSpecification
  ): VideoLinkBooking {
    specification.validate()
    val booking = videoLinkBookingRepository.findById(videoBookingId).orElseThrow {
      EntityNotFoundException("Video link booking with id $videoBookingId not found")
    }

    deletePrisonAppointmentsForBooking(booking)

    val (mainEvent, preEvent, postEvent) = createPrisonAppointments(booking.offenderBookingId, specification)

    with(booking) {
      courtName = null
      courtId = specification.courtId
      /**
       * Call flush() to persuade Hibernate to remove the old appointments (if any).
       * Have to do this manually because Hibernate doesn't delete the old appointments before attempting
       * to insert the new ones (and breaking unique constraint).
       */
      appointments.clear()
      videoLinkBookingRepository.flush()

      addAppointments(mainEvent, preEvent, postEvent)
    }

    /**
     * Ensure that the new VideoLinkAppointment objects are persistent
     * and so have ids before the ApplicationInsightsEventListener is called.
     */
    videoLinkBookingRepository.flush()
    videoLinkBookingEventListener.bookingUpdated(booking, specification)
    return booking
  }

  private fun VideoLinkBooking.addAppointments(mainEvent: Event, preEvent: Event?, postEvent: Event?) {
    preEvent?.let { addPreAppointment(it.eventId, it.eventLocationId, it.startTime, it.endTime) }
    addMainAppointment(mainEvent.eventId, mainEvent.eventLocationId, mainEvent.startTime, mainEvent.endTime)
    postEvent?.let { addPostAppointment(it.eventId, it.eventLocationId, it.startTime, it.endTime) }
  }

  private fun toVideoLinkAppointmentDto(appointment: VideoLinkAppointment) =
    VideoLinkAppointmentDto(
      id = appointment.id!!,
      bookingId = appointment.videoLinkBooking.offenderBookingId,
      appointmentId = appointment.appointmentId,
      videoLinkBookingId = appointment.videoLinkBooking.id!!,
      mainAppointmentId = appointment.videoLinkBooking.appointments[MAIN]?.appointmentId,
      hearingType = appointment.hearingType,
      court = courtService.chooseCourtName(appointment.videoLinkBooking),
      courtId = appointment.videoLinkBooking.courtId,
      createdByUsername = appointment.videoLinkBooking.createdByUsername,
      madeByTheCourt = appointment.videoLinkBooking.madeByTheCourt
    )

  private fun getCourtId(specification: VideoLinkBookingSpecification) =
    specification.courtId ?: specification.court?.let { courtService.getCourtIdForCourtName(it) }

  private fun createPrisonAppointments(
    offenderBookingId: Long,
    specification: VideoLinkAppointmentsSpecification
  ): Triple<Event, Event?, Event?> {
    val comment = specification.comment

    val mainEvent = savePrisonAppointment(offenderBookingId, comment, specification.main)
    val preEvent = specification.pre?.let { savePrisonAppointment(offenderBookingId, comment, it) }
    val postEvent = specification.post?.let { savePrisonAppointment(offenderBookingId, comment, it) }
    return Triple(mainEvent, preEvent, postEvent)
  }

  private fun savePrisonAppointment(
    bookingId: Long,
    comment: String?,
    appointmentSpec: VideoLinkAppointmentSpecification
  ): Event = prisonApiServiceAuditable.postAppointment(
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

  private fun deletePrisonAppointmentsForBooking(booking: VideoLinkBooking) {
    booking.appointments.values.forEach { prisonApiService.deleteAppointment(it.appointmentId) }
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
      .filter { it.appointmentTypeCode == VIDEO_LINK_APPOINTMENT_TYPE }
      .filter { hasAnEndDate(it) }

    val scheduledAppointmentIds = scheduledAppointments.map { it.id }

    // If a booking's main appointment doesn't match one of the scheduledAppointmentIds then it is excluded
    val bookings = videoLinkBookingRepository.findByAppointmentIdsAndHearingType(
      ids = scheduledAppointmentIds,
      courtName = courtName,
      courtId = courtId,
      hearingType = MAIN
    )

    val scheduledAppointmentsById = scheduledAppointments.associateBy { it.id }

    return bookings.map {
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

    (this as VideoLinkAppointmentsSpecification).validate()
  }

  private fun VideoLinkAppointmentsSpecification.validate() {
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

  fun deleteAppointments(appointmentId: Long) {
    var videoLinkAppointments = videoLinkAppointmentRepository.findAllByAppointmentId(appointmentId)
    videoLinkAppointments.forEach { videoLinkAppointment ->
      when (videoLinkAppointment.hearingType == MAIN) {
        true -> videoLinkBookingRepository.delete(videoLinkAppointment.videoLinkBooking)
        false -> videoLinkAppointmentRepository.delete(videoLinkAppointment)
      }
    }
  }
}
