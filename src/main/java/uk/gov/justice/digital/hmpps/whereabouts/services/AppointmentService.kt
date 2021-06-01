package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.Appointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDefaults
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDetailsDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentSearchDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreatePrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreatedAppointmentDetailsDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.RecurringAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.Repeat
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentSearchDto
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
      videoLinkBookingRepository.findByMainAppointmentIds(listOf(appointmentId)).firstOrNull()

    val recurringAppointment: RecurringAppointment? =
      recurringAppointmentRepository.findRecurringAppointmentByRelatedAppointmentsContains(
        RelatedAppointment(appointmentId)
      ).orElse(null)

    return AppointmentDetailsDto(
      appointment = makeAppointmentDto(offenderNo, mainAppointmentDetails),
      videoLinkBooking = videoLinkBooking?.let {
        val preAppointmentDetails = it.pre?.let { pre -> prisonApiService.getPrisonAppointment(pre.appointmentId) }
        val postAppointmentDetails = it.post?.let { post -> prisonApiService.getPrisonAppointment(post.appointmentId) }

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
        makeRecurringAppointment(appointmentIds = appointmentIds, repeat = createAppointmentSpecification.repeat)

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
      videoLinkBookingRepository.findByMainAppointmentIds(listOf(appointmentId)).firstOrNull()

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

    recurringAppointment.relatedAppointments?.let {
      val appointmentIds = it.map { appointment -> appointment.id }

      prisonApiService.deleteAppointments(appointmentIds)

      recurringAppointmentRepository.deleteById(recurringAppointment.id)

      raiseRecurringAppointmentDeletedTrackingEvent(appointmentIds.count().toLong())
    }
  }

  private fun makeRecurringAppointment(
    appointmentIds: Set<Long>,
    repeat: Repeat
  ) = RecurringAppointment(
    repeatPeriod = repeat.repeatPeriod,
    count = repeat.count,
    relatedAppointments = appointmentIds.let {
      it.map { id ->
        RelatedAppointment(
          id
        )
      }
    }
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
        videoLinkBooking.main,
        startTime = mainAppointmentDetails?.startTime,
        endTime = mainAppointmentDetails?.endTime,
        locationId = mainAppointmentDetails?.eventLocationId
      ),
      pre = videoLinkBooking.pre?.let {
        makeVideoLinkAppointmentDto(
          it,
          startTime = preAppointmentDetails?.startTime,
          endTime = preAppointmentDetails?.endTime,
          locationId = preAppointmentDetails?.eventLocationId
        )
      },
      post = videoLinkBooking.post?.let {
        makeVideoLinkAppointmentDto(
          it,
          startTime = postAppointmentDetails?.startTime,
          endTime = postAppointmentDetails?.endTime,
          locationId = postAppointmentDetails?.eventLocationId
        )
      }
    )

  private fun makeVideoLinkAppointmentDto(
    videoLinkAppointment: VideoLinkAppointment,
    startTime: LocalDateTime? = null,
    endTime: LocalDateTime? = null,
    locationId: Long? = null
  ): VideoLinkAppointmentDto =
    VideoLinkAppointmentDto(
      id = videoLinkAppointment.id!!,
      bookingId = videoLinkAppointment.bookingId,
      appointmentId = videoLinkAppointment.appointmentId,
      court = courtService.chooseCourtName(videoLinkAppointment),
      courtId = videoLinkAppointment.courtId,
      hearingType = videoLinkAppointment.hearingType,
      createdByUsername = videoLinkAppointment.createdByUsername,
      madeByTheCourt = videoLinkAppointment.madeByTheCourt,
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
    repeatPeriod = recurringAppointment.repeatPeriod,
    count = recurringAppointment.count
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
