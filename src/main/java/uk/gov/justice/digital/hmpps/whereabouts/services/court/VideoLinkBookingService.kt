package uk.gov.justice.digital.hmpps.whereabouts.services.court

import jakarta.persistence.EntityNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderBooking
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentsSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSearchDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.listeners.AppointmentChangedEventMessage
import uk.gov.justice.digital.hmpps.whereabouts.listeners.Reason
import uk.gov.justice.digital.hmpps.whereabouts.listeners.ReleasedOffenderEventMessage
import uk.gov.justice.digital.hmpps.whereabouts.listeners.ScheduleEventStatus
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import uk.gov.justice.digital.hmpps.whereabouts.model.NotifyRequest
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.NotifyService
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApi.EventPropagation
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiServiceAuditable
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonRegisterClient
import uk.gov.justice.digital.hmpps.whereabouts.services.ValidationException
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

const val VIDEO_LINK_APPOINTMENT_TYPE = "VLB"

@Service
class VideoLinkBookingService(
  @Value("\${notify.enabled}") private val enabled: Boolean,
  private val courtService: CourtService,
  private val prisonApiService: PrisonApiService,
  private val prisonApiServiceAuditable: PrisonApiServiceAuditable,
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val clock: Clock,
  private val videoLinkBookingEventListener: VideoLinkBookingEventListener,
  private val notifyService: NotifyService,
  private val prisonRegisterClient: PrisonRegisterClient,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional(readOnly = true)
  fun getVideoLinkAppointments(appointmentIds: Set<Long>): List<VideoLinkAppointmentDto> =
    videoLinkAppointmentRepository.findVideoLinkAppointmentByAppointmentIdIn(appointmentIds)
      .map { toVideoLinkAppointmentDto(it) }

  @Transactional
  fun createVideoLinkBooking(specification: VideoLinkBookingSpecification): Long {
    specification.validate()

    val (mainEvent, preEvent, postEvent) = createPrisonAppointments(specification.bookingId!!, specification)

    val videoLinkBooking = VideoLinkBooking(
      offenderBookingId = specification.bookingId,
      courtName = specification.court,
      courtId = getCourtId(specification),
      courtHearingType = specification.courtHearingType,
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
    specification: VideoLinkBookingUpdateSpecification,
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

  private fun toVideoLinkAppointmentDto(appointment: VideoLinkAppointment) = VideoLinkAppointmentDto(
    id = appointment.id!!,
    bookingId = appointment.videoLinkBooking.offenderBookingId,
    appointmentId = appointment.appointmentId,
    videoLinkBookingId = appointment.videoLinkBooking.id!!,
    mainAppointmentId = appointment.videoLinkBooking.appointments[MAIN]?.appointmentId,
    hearingType = appointment.hearingType,
    court = courtService.chooseCourtName(appointment.videoLinkBooking),
    courtId = appointment.videoLinkBooking.courtId,
    createdByUsername = appointment.videoLinkBooking.createdByUsername,
    madeByTheCourt = appointment.videoLinkBooking.madeByTheCourt,
  )

  private fun getCourtId(specification: VideoLinkBookingSpecification) =
    specification.courtId ?: specification.court?.let { courtService.getCourtIdForCourtName(it) }

  private fun createPrisonAppointments(
    offenderBookingId: Long,
    specification: VideoLinkAppointmentsSpecification,
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
    appointmentSpec: VideoLinkAppointmentSpecification,
  ): Event = prisonApiServiceAuditable.postAppointment(
    bookingId,
    CreateBookingAppointment(
      appointmentType = VIDEO_LINK_APPOINTMENT_TYPE,
      locationId = appointmentSpec.locationId!!,
      startTime = appointmentSpec.startTime.toString(),
      endTime = appointmentSpec.endTime.toString(),
      comment = comment,
    ),
    EventPropagation.DENY,
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
      courtHearingType = booking.courtHearingType,
      courtId = booking.courtId,
      comment = mainEvent.comment,
      pre = events[PRE]?.let { toLocationTimeslot(it) },
      main = toLocationTimeslot(mainEvent),
      post = events[POST]?.let { toLocationTimeslot(it) },
    )
  }

  private fun toLocationTimeslot(it: PrisonAppointment) = VideoLinkBookingResponse.LocationTimeslot(
    locationId = it.eventLocationId,
    startTime = it.startTime,
    endTime = it.endTime!!,
  )

  @Transactional
  fun deleteVideoLinkBooking(videoBookingId: Long): VideoLinkBooking {
    val booking = videoLinkBookingRepository.findById(videoBookingId).orElseThrow {
      EntityNotFoundException("Video link booking with id $videoBookingId not found")
    }

    val appointmentsToDelete = booking.appointments.values.sortedBy { it.appointmentId }.map { it.appointmentId }
    if (appointmentsToDelete.isNotEmpty()) {
      prisonApiService.deleteAppointments(appointmentsToDelete, EventPropagation.DENY)
      log.debug("Video link appointments deleted from Nomis: {}", appointmentsToDelete)
    }

    videoLinkBookingRepository.deleteById(booking.id!!)
    videoLinkBookingEventListener.bookingDeleted(booking)
    log.info("Video link booking with id {} deleted", videoBookingId)
    return booking
  }

  private fun deletePrisonAppointmentsForBooking(booking: VideoLinkBooking) {
    val appointmentsToDelete = booking.appointments.values.sortedBy { it.appointmentId }.map { it.appointmentId }
    if (appointmentsToDelete.isNotEmpty()) {
      prisonApiService.deleteAppointments(appointmentsToDelete, EventPropagation.DENY)
    }
  }

  @Transactional(readOnly = true)
  fun getVideoLinkBookingsBySearchDetails(
    searchDetails: VideoLinkBookingSearchDetails,
    date: LocalDate,
  ): List<VideoLinkBookingResponse> {
    val videoLinkAppointments =
      videoLinkAppointmentRepository.findAllByStartDateTimeBetweenAndHearingTypeIsAndVideoLinkBookingCourtIdIsAndVideoLinkBookingPrisonIdIn(
        date.atTime(LocalTime.MIN),
        date.atTime(LocalTime.MAX),
        MAIN,
        searchDetails.courtId,
        searchDetails.prisonIds,
      )

    return videoLinkAppointments.map {
      VideoLinkBookingResponse(
        videoLinkBookingId = it.videoLinkBooking.id!!,
        bookingId = it.videoLinkBooking.offenderBookingId,
        agencyId = it.videoLinkBooking.prisonId,
        court = it.videoLinkBooking.courtName,
        courtId = it.videoLinkBooking.courtId,
        courtHearingType = it.videoLinkBooking.courtHearingType,
        main = toVideoLinkAppointmentDto(it.videoLinkBooking.appointments[MAIN])!!,
        pre = toVideoLinkAppointmentDto(it.videoLinkBooking.appointments[PRE]),
        post = toVideoLinkAppointmentDto(it.videoLinkBooking.appointments[POST]),
      )
    }
  }

  fun updateVideoLinkBookingComment(videoLinkBookingId: Long, comment: String?) {
    val booking = videoLinkBookingRepository.findById(videoLinkBookingId).orElseThrow {
      EntityNotFoundException("Video link booking with id $videoLinkBookingId not found")
    }
    booking.appointments.values.forEach {
      prisonApiService.updateAppointmentComment(
        it.appointmentId,
        UpdateComment(comment),
        EventPropagation.DENY,
      )
    }
  }

  private fun toVideoLinkAppointmentDto(videoLinkAppointment: VideoLinkAppointment?) = videoLinkAppointment?.let {
    VideoLinkBookingResponse.LocationTimeslot(
      locationId = it.locationId,
      startTime = it.startDateTime,
      endTime = it.endDateTime,
    )
  }

  private fun toVideoLinkAppointmentDto(scheduledAppointment: ScheduledAppointmentDto?) =
    scheduledAppointment?.takeIf { hasAnEndDate(it) }?.let {
      VideoLinkBookingResponse.LocationTimeslot(
        locationId = it.locationId,
        startTime = it.startTime,
        endTime = it.endTime!!,
      )
    }

  private fun hasAnEndDate(it: ScheduledAppointmentDto): Boolean {
    val hasEndDate = it.endTime != null
    if (!hasEndDate) log.error("Appointment with id ${it.id} has no end date")
    return hasEndDate
  }

  private fun VideoLinkBookingSpecification.validate() {
    if ((court.isNullOrBlank()) && (courtId.isNullOrBlank())) {
      throw ValidationException("One of court or courtId must be specified")
    }

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

  @Transactional
  fun processNomisUpdate(appointmentChangedEventMessage: AppointmentChangedEventMessage) {
    val videoLinkAppointment =
      videoLinkAppointmentRepository.findOneByAppointmentId(appointmentChangedEventMessage.scheduleEventId) ?: return
    log.debug("Processing video link appointment: {}", videoLinkAppointment)
    if (appointmentChangedEventMessage.recordDeleted || appointmentChangedEventMessage.scheduleEventStatus == ScheduleEventStatus.CANC) {
      if (videoLinkAppointment.hearingType != MAIN) {
        val vb = videoLinkAppointment.videoLinkBooking
        vb.appointments.remove(videoLinkAppointment.hearingType)
        videoLinkBookingRepository.save(vb)
        videoLinkBookingEventListener.appointmentRemovedFromBooking(vb)
        log.debug("Appointment from video link booking deleted: {}", vb)
      } else {
        try {
          this.deleteVideoLinkBooking(videoLinkAppointment.videoLinkBooking.id!!)
        } catch (e: EntityNotFoundException) {
          log.info(
            "Video link appointment for offenderBookingId already deleted: {}",
            videoLinkAppointment.videoLinkBooking.id,
          )
        }

        if (!enabled) {
          log.info(
            "Email notification is not enabled so emails not sent to either court/prison when offender " +
              "with bookingId {} was transferred or released",
            videoLinkAppointment.videoLinkBooking.id,
          )
          return
        }

        var courtEmail = courtService.getCourtEmailForCourtId(videoLinkAppointment.videoLinkBooking.courtId)
        var courtName =
          courtService.getCourtNameForCourtId(videoLinkAppointment.videoLinkBooking.courtId)
            ?: videoLinkAppointment.videoLinkBooking.courtName ?: ""

        val prisonEmail = getPrisonEmail(videoLinkAppointment.videoLinkBooking.prisonId)
        val prisonName = getPrisonName(videoLinkAppointment.videoLinkBooking.prisonId)

        if (prisonEmail == null || prisonName == null) {
          log.info("Prison name or email address for {} not found", videoLinkAppointment.videoLinkBooking.id)
          return
        }

        val offenderBooking = prisonApiService.getOffenderBookingDetails(appointmentChangedEventMessage.bookingId)
        val notifyRequestData =
          getNotifyRequestData(offenderBooking, videoLinkAppointment.videoLinkBooking, prisonName, courtName)

        if (courtEmail == null) {
          notifyService.sendAppointmentCanceledEmailToPrisonOnly(notifyRequestData, prisonEmail)
          log.info("Email about appointment cancellation send to prison {}", notifyRequestData.prisonName)
        } else {
          notifyService.sendAppointmentCanceledEmailToCourtAndPrison(notifyRequestData, courtEmail, prisonEmail)
          log.info(
            "Email about appointment cancellation send to prison: {} and court: {}",
            notifyRequestData.prisonName,
            notifyRequestData.courtName,
          )
        }

        return
      }
    } else {
      videoLinkBookingEventListener.appointmentUpdatedInNomis(videoLinkAppointment, appointmentChangedEventMessage)
    }
  }

  fun deleteAppointmentWhenTransferredOrReleased(releasedOffenderEventMessage: ReleasedOffenderEventMessage) {
    val reason = releasedOffenderEventMessage.additionalInformation.reason
    if (reason == Reason.TRANSFERRED || reason == Reason.RELEASED) {
      val offenderBookings = prisonApiService.getOffenderDetailsFromOffenderNos(
        listOf(releasedOffenderEventMessage.additionalInformation.nomsNumber),
        false,
      )
      log.debug("OffenderBookings: {}", offenderBookings)
      if (offenderBookings.isNotEmpty()) {
        val activeOffenderBooking = offenderBookings.first()
        val offenderBookingId = activeOffenderBooking.bookingId
        val prisonerAppointments =
          videoLinkAppointmentRepository.findAllByHearingTypeIsAndStartDateTimeIsAfterAndVideoLinkBookingOffenderBookingIdIsAndVideoLinkBookingPrisonIdIs(
            hearingType = MAIN,
            startDateTime = LocalDateTime.parse(
              releasedOffenderEventMessage.occurredAt,
              DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            ),
            offenderBookingId = offenderBookingId,
            prisonId = releasedOffenderEventMessage.additionalInformation.prisonId,
          )
        log.info("Appointments count for bookingId:  {} {}", offenderBookingId, prisonerAppointments.size)
        prisonerAppointments.forEach {
          try {
            this.deleteVideoLinkBooking(it.videoLinkBooking.id!!)
          } catch (e: EntityNotFoundException) {
            log.info("Video link appointment for offenderBookingId {} already deleted", offenderBookingId)
            return@forEach
          }
          log.info(
            "Getting information to send emails for {} ",
            offenderBookingId,
          )

          if (!enabled) {
            log.info(
              "Email notification is not enabled so emails not sent to either court/prison when offender " +
                "with bookingId {} was transferred or released",
              offenderBookingId,
            )
            return
          }

          var courtEmail = courtService.getCourtEmailForCourtId(it.videoLinkBooking.courtId)
          var courtName =
            courtService.getCourtNameForCourtId(it.videoLinkBooking.courtId) ?: it.videoLinkBooking.courtName ?: ""

          val prisonEmail = getPrisonEmail(it.videoLinkBooking.prisonId)
          val prisonName = getPrisonName(it.videoLinkBooking.prisonId)

          if (prisonEmail == null || prisonName == null) {
            log.info("Prison name or email address for {} not found", offenderBookingId)
            return@forEach
          }

          val notifyRequestData =
            getNotifyRequestData(activeOffenderBooking, it.videoLinkBooking, prisonName, courtName)

          if (reason == Reason.TRANSFERRED && courtEmail != null) {
            notifyService.sendOffenderTransferredEmailToCourtAndPrison(notifyRequestData, courtEmail, prisonEmail)
            log.info(
              "Email sent to courtName {} following BVL appointment deletion for bookingId {}",
              courtName,
              offenderBookingId,
            )
          } else if (reason == Reason.TRANSFERRED && courtEmail == null) {
            notifyService.sendOffenderTransferredEmailToPrisonOnly(notifyRequestData, prisonEmail)
            log.info(
              "Email for courtId {} not found when deleting BVL appointment for bookingId {}",
              it.videoLinkBooking.courtId,
              offenderBookingId,
            )
          } else if (reason == Reason.RELEASED && courtEmail != null) {
            notifyService.sendOffenderReleasedEmailToCourtAndPrison(notifyRequestData, courtEmail, prisonEmail)
            log.info(
              "Email sent to courtName {} following BVL appointment deletion for bookingId {}",
              courtName,
              offenderBookingId,
            )
          } else if (reason == Reason.RELEASED && courtEmail == null) {
            notifyService.sendOffenderReleasedEmailToPrisonOnly(notifyRequestData, prisonEmail)
            log.info(
              "Email for prisonId {} not found when deleting BVL appointment for bookingId {}",
              it.videoLinkBooking.prisonId,
              offenderBookingId,
            )
          }
        }
      }
    }
  }

  private fun getNotifyRequestData(
    activeOffenderBooking: OffenderBooking,
    videoLinkBooking: VideoLinkBooking,
    prisonName: String,
    courtName: String,
  ): NotifyRequest {
    return NotifyRequest(
      firstName = activeOffenderBooking.firstName,
      lastName = activeOffenderBooking.lastName,
      dateOfBirth = activeOffenderBooking.dateOfBirth,
      mainHearing = videoLinkBooking.appointments[MAIN]!!,
      preHearing = videoLinkBooking.appointments[PRE],
      postHearing = videoLinkBooking.appointments[POST],
      comments = videoLinkBooking.comment,
      prisonName = prisonName,
      courtName = courtName,
      prisonNumber = activeOffenderBooking.offenderNo,
    )
  }

  fun getPrisonEmail(prisonId: String): String? {
    // email prison VCC's only not OMU's
    try {
      return prisonRegisterClient.getPrisonEmailAddress(
        prisonId,
        DepartmentType.VIDEOLINK_CONFERENCING_CENTRE,
      )?.emailAddress
    } catch (e: WebClientResponseException) {
      log.info(
        "Could not get prison VCC email address for {} from prisonRegister. Exception message {}",
        prisonId,
        e.message,
      )
    }
    return null
  }

  fun getPrisonName(prisonId: String): String? {
    try {
      return prisonRegisterClient.getPrisonDetails(prisonId)?.prisonName
    } catch (e: WebClientResponseException) {
      log.info("Could not get prison name for {} from prisonRegister. Error message: {}", prisonId, e.message)
    }
    return null
  }

  enum class DepartmentType {
    VIDEOLINK_CONFERENCING_CENTRE,
  }
}

data class UpdateComment(val comment: String?)
