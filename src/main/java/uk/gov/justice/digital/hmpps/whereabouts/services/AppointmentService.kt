package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.*
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentSearchDto
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RecurringAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RelatedAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.RecurringAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.court.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional

@Service
class AppointmentService(
  private val courtService: CourtService,
  private val prisonApiService: PrisonApiService,
  private val prisonApiServiceAuditable: PrisonApiServiceAuditable,
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val recurringAppointmentRepository: RecurringAppointmentRepository,
  private val videoLinkBookingService: VideoLinkBookingService,
  private val telemetryClient: TelemetryClient
) {

  fun getAppointments(
    agencyId: String,
    date: LocalDate,
    timeSlot: TimePeriod?,
    offenderLocationPrefix: String?,
    locationId: Long?
  ): List<AppointmentSearchDto> {

    val appointmentsFromPrisonApi =
      prisonApiService.getScheduledAppointments(agencyId, date, timeSlot, locationId)

    val locationFilter = generateOffenderLocationFilter(offenderLocationPrefix, appointmentsFromPrisonApi)

    return appointmentsFromPrisonApi
      .filter { a -> locationFilter.filterLocations(a) }
      .map { a -> makeAppointmentDto(a) }.toList()
  }

  private fun generateOffenderLocationFilter(
    offenderLocationPrefix: String?,
    appointmentsFromPrisonApi: List<ScheduledAppointmentSearchDto>
  ): LocationFilter {

    if (offenderLocationPrefix == null) return NoOpFilter()

    val offenderNos = appointmentsFromPrisonApi.map { a -> a.offenderNo }.toSet()
    val offenderBookingDetails = prisonApiService.getOffenderDetailsFromOffenderNos(offenderNos)
    val offenderLocationDescriptionByOffenderNo =
      offenderBookingDetails.associate { b -> b.offenderNo to b.assignedLivingUnitDesc }

    return OffenderLocationFilter(offenderLocationPrefix, offenderLocationDescriptionByOffenderNo)
  }

  @Transactional
  fun getAppointment(appointmentId: Long): AppointmentDetailsDto {
    val mainAppointmentDetails: PrisonAppointment = prisonApiService.getPrisonAppointment(appointmentId)
      ?: throw EntityNotFoundException("Appointment $appointmentId does not exist")

    val offenderNo = try {
      prisonApiService.getOffenderNoFromBookingId(mainAppointmentDetails.bookingId)
    } catch (e: Exception) {
      null
    }

    val videoLinkBooking: VideoLinkBooking? =
      videoLinkBookingRepository.findByAppointmentIdsAndHearingType(listOf(appointmentId), MAIN).firstOrNull()

    val recurringAppointment: RecurringAppointment? =
      recurringAppointmentRepository.findRecurringAppointmentByRelatedAppointmentsContains(
        RelatedAppointment(appointmentId)
      ).orElse(null)

    return AppointmentDetailsDto(
      appointment = makeAppointmentDto(offenderNo, mainAppointmentDetails),
      videoLinkBooking = videoLinkBooking?.let {
        val preAppointmentDetails =
          it.appointments[PRE]?.let { pre -> prisonApiService.getPrisonAppointment(pre.appointmentId) }
        val postAppointmentDetails =
          it.appointments[POST]?.let { post -> prisonApiService.getPrisonAppointment(post.appointmentId) }

        makeVideoLinkBookingAppointmentDto(it, mainAppointmentDetails, preAppointmentDetails, postAppointmentDetails)
      },
      recurring = recurringAppointment?.let { makeRecurringAppointmentDto(it) }
    )
  }

  @Transactional
  fun createAppointment(createAppointmentSpecification: CreateAppointmentSpecification): List<CreatedAppointmentDetailsDto> {
    val appointmentCreated =
      prisonApiServiceAuditable.createAppointments(makePrisonAppointment(createAppointmentSpecification))

    createAppointmentSpecification.repeat?.let {
      val appointmentIds: Set<Long> = appointmentCreated?.map { it.appointmentEventId }?.toSet() ?: emptySet()

      val recurringAppointment =
        makeRecurringAppointment(appointmentIds = appointmentIds, startTime = createAppointmentSpecification.startTime, repeat = createAppointmentSpecification.repeat)

      recurringAppointmentRepository.save(recurringAppointment)

      raiseRecurringAppointmentCreatedTrackingEvent(
        createAppointmentSpecification,
        createAppointmentSpecification.repeat
      )
    }

    return appointmentCreated
  }

  @Transactional
  fun deleteAppointment(appointmentId: Long) {
    prisonApiService.getPrisonAppointment(appointmentId)
      ?: throw EntityNotFoundException("Appointment $appointmentId does not exist")

    val videoLinkBooking: VideoLinkBooking? =
      videoLinkBookingRepository.findByAppointmentIdsAndHearingType(listOf(appointmentId), MAIN).firstOrNull()

    if (videoLinkBooking != null) {
      videoLinkBookingService.deleteVideoLinkBooking(videoLinkBooking.id!!)
      return
    }

    val recurringAppointment: RecurringAppointment? =
      recurringAppointmentRepository.findRecurringAppointmentByRelatedAppointmentsContains(
        RelatedAppointment(appointmentId)
      ).orElse(null)

    if (recurringAppointment == null) {
      prisonApiService.deleteAppointment(appointmentId)
      return
    }

    prisonApiService.deleteAppointment(appointmentId)
    removeSingleAppointmentInRecurringList(appointmentId, recurringAppointment)
    return
  }

  @Transactional
  fun deleteRecurringAppointmentSequence(recurringAppointmentId: Long) {
    val recurringAppointment: RecurringAppointment =
      recurringAppointmentRepository.findById(recurringAppointmentId).orElseThrow { EntityNotFoundException("Appointment $recurringAppointmentId does not exist") }

    recurringAppointment.relatedAppointments?.let {
      val appointmentIds = it.map { appointment -> appointment.id }

      prisonApiService.deleteAppointments(appointmentIds)

      recurringAppointmentRepository.deleteById(recurringAppointmentId)

      raiseRecurringAppointmentDeletedTrackingEvent(appointmentIds.count().toLong())
    }
  }

  private fun removeSingleAppointmentInRecurringList(appointmentId: Long, recurringAppointment: RecurringAppointment) {
    if (recurringAppointment.relatedAppointments == null) {
      return
    }
    val allRecurringAppointments = recurringAppointment.relatedAppointments!!
    val recurringAppointmentToDelete = allRecurringAppointments.find { it.id == appointmentId }
    recurringAppointmentToDelete.let { allRecurringAppointments.remove(it) }
    if (allRecurringAppointments.size == 0) {
      recurringAppointmentRepository.deleteById(recurringAppointment.id)
    }
  }

  private fun makeRecurringAppointment(
    appointmentIds: Set<Long>,
    startTime: LocalDateTime,
    repeat: Repeat
  ) = RecurringAppointment(
    repeatPeriod = repeat.repeatPeriod,
    count = repeat.count,
    startTime = startTime,
    relatedAppointments = appointmentIds.let {
      it.map { id ->
        RelatedAppointment(
          id
        )
      }
    }.toMutableList()
  )

  private fun raiseRecurringAppointmentCreatedTrackingEvent(
    createAppointmentSpecification: CreateAppointmentSpecification,
    repeat: Repeat
  ) {
    telemetryClient.trackEvent(
      "Recurring Appointment created for a prisoner",
      mapOf(
        "appointmentType" to createAppointmentSpecification.appointmentType,
        "repeatPeriod" to repeat.repeatPeriod.toString(),
        "count" to repeat.count.toString(),
        "bookingId" to createAppointmentSpecification.bookingId.toString(),
        "locationId" to createAppointmentSpecification.locationId.toString()
      ),
      null
    )
  }

  private fun raiseRecurringAppointmentDeletedTrackingEvent(appointmentsDeleted: Long) {
    telemetryClient.trackEvent(
      "Recurring Appointment deleted",
      mapOf(
        "appointmentsDeleted" to appointmentsDeleted.toString()
      ),
      null
    )
  }

  private fun makeVideoLinkBookingAppointmentDto(
    videoLinkBooking: VideoLinkBooking,
    mainAppointmentDetails: PrisonAppointment? = null,
    preAppointmentDetails: PrisonAppointment? = null,
    postAppointmentDetails: PrisonAppointment? = null
  ): VideoLinkBookingDto =
    VideoLinkBookingDto(
      id = videoLinkBooking.id!!,
      main = makeVideoLinkAppointmentDto(
        videoLinkBooking.appointments[MAIN]!!,
        mainAppointmentId = videoLinkBooking.appointments[MAIN]!!.appointmentId,
        startTime = mainAppointmentDetails?.startTime,
        endTime = mainAppointmentDetails?.endTime,
        locationId = mainAppointmentDetails?.eventLocationId
      ),
      pre = videoLinkBooking.appointments[PRE]?.let {
        makeVideoLinkAppointmentDto(
          it,
          mainAppointmentId = videoLinkBooking.appointments[MAIN]!!.appointmentId,
          startTime = preAppointmentDetails?.startTime,
          endTime = preAppointmentDetails?.endTime,
          locationId = preAppointmentDetails?.eventLocationId
        )
      },
      post = videoLinkBooking.appointments[POST]?.let {
        makeVideoLinkAppointmentDto(
          it,
          mainAppointmentId = videoLinkBooking.appointments[MAIN]!!.appointmentId,
          startTime = postAppointmentDetails?.startTime,
          endTime = postAppointmentDetails?.endTime,
          locationId = postAppointmentDetails?.eventLocationId
        )
      }
    )

  private fun makeVideoLinkAppointmentDto(
    videoLinkAppointment: VideoLinkAppointment,
    mainAppointmentId: Long?,
    startTime: LocalDateTime? = null,
    endTime: LocalDateTime? = null,
    locationId: Long? = null
  ): VideoLinkAppointmentDto =
    VideoLinkAppointmentDto(
      id = videoLinkAppointment.id!!,
      bookingId = videoLinkAppointment.videoLinkBooking.offenderBookingId,
      appointmentId = videoLinkAppointment.appointmentId,
      videoLinkBookingId = videoLinkAppointment.videoLinkBooking.id!!,
      mainAppointmentId = mainAppointmentId,
      court = courtService.chooseCourtName(videoLinkAppointment.videoLinkBooking),
      courtId = videoLinkAppointment.videoLinkBooking.courtId,
      hearingType = videoLinkAppointment.hearingType,
      createdByUsername = videoLinkAppointment.videoLinkBooking.createdByUsername,
      madeByTheCourt = videoLinkAppointment.videoLinkBooking.madeByTheCourt,
      startTime = startTime,
      endTime = endTime,
      locationId = locationId
    )

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}

private fun makeAppointmentDto(scheduledAppointmentDto: ScheduledAppointmentSearchDto): AppointmentSearchDto =
  AppointmentSearchDto(
    id = scheduledAppointmentDto.id,
    agencyId = scheduledAppointmentDto.agencyId,
    locationId = scheduledAppointmentDto.locationId,
    locationDescription = scheduledAppointmentDto.locationDescription,
    appointmentTypeCode = scheduledAppointmentDto.appointmentTypeCode,
    appointmentTypeDescription = scheduledAppointmentDto.appointmentTypeDescription,
    offenderNo = scheduledAppointmentDto.offenderNo,
    firstName = scheduledAppointmentDto.firstName,
    lastName = scheduledAppointmentDto.lastName,
    startTime = scheduledAppointmentDto.startTime,
    endTime = scheduledAppointmentDto.endTime,
    createUserId = scheduledAppointmentDto.createUserId
  )

private fun makeAppointmentDto(offenderNo: String? = null, prisonAppointment: PrisonAppointment): AppointmentDto =
  AppointmentDto(
    id = prisonAppointment.eventId,
    agencyId = prisonAppointment.agencyId,
    locationId = prisonAppointment.eventLocationId,
    appointmentTypeCode = prisonAppointment.eventSubType,
    startTime = prisonAppointment.startTime,
    endTime = prisonAppointment.endTime,
    offenderNo = offenderNo,
    createUserId = prisonAppointment.createUserId,
    comment = prisonAppointment.comment
  )

private fun makeRecurringAppointmentDto(recurringAppointment: RecurringAppointment): RecurringAppointmentDto =
  RecurringAppointmentDto(
    id = recurringAppointment.id!!,
    repeatPeriod = recurringAppointment.repeatPeriod,
    count = recurringAppointment.count,
    startTime = recurringAppointment.startTime
  )

private fun makePrisonAppointment(createAppointmentSpecification: CreateAppointmentSpecification) =
  CreatePrisonAppointment(
    appointmentDefaults = AppointmentDefaults(
      appointmentType = createAppointmentSpecification.appointmentType,
      comment = createAppointmentSpecification.comment,
      startTime = createAppointmentSpecification.startTime,
      endTime = createAppointmentSpecification.endTime,
      locationId = createAppointmentSpecification.locationId
    ),
    appointments = listOf(
      Appointment(
        bookingId = createAppointmentSpecification.bookingId,
        comment = createAppointmentSpecification.comment,
        startTime = createAppointmentSpecification.startTime,
        endTime = createAppointmentSpecification.endTime
      )
    ),
    repeat = createAppointmentSpecification.repeat
  )
