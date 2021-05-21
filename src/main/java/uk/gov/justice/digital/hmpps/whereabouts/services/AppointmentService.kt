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
import uk.gov.justice.digital.hmpps.whereabouts.model.MainRecurringAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RecurringAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.RecurringAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import java.time.LocalDate
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional

@Service
class AppointmentService(
  private val prisonApiService: PrisonApiService,
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val recurringAppointmentRepository: RecurringAppointmentRepository,
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
    val prisonAppointment: PrisonAppointment = prisonApiService.getPrisonAppointment(appointmentId)
      ?: throw EntityNotFoundException("Appointment $appointmentId does not exist")

    val offenderNo = prisonApiService.getOffenderNoFromBookingId(prisonAppointment.bookingId)

    val videoLinkBooking: VideoLinkBooking? =
      videoLinkBookingRepository.findByMainAppointmentIds(listOf(appointmentId)).firstOrNull()

    val mainRecurringAppointment: MainRecurringAppointment? =
      recurringAppointmentRepository.findById(appointmentId).orElse(null)

    return AppointmentDetailsDto(
      appointment = makeAppointmentDto(offenderNo, prisonAppointment),
      videoLinkBooking = videoLinkBooking?.let { makeVideoLinkBookingAppointmentDto(it) },
      recurring = mainRecurringAppointment?.let { makeRecurringAppointmentDto(it) }
    )
  }

  @Transactional
  fun createAppointment(createAppointmentSpecification: CreateAppointmentSpecification): CreatedAppointmentDetailsDto {
    val appointmentCreated =
      prisonApiService.createAppointments(makePrisonAppointment(createAppointmentSpecification)).first()

    createAppointmentSpecification.repeat?.let {
      val recurringAppointment =
        makeMainRecurringAppointment(appointmentCreated, createAppointmentSpecification.repeat)

      recurringAppointmentRepository.save(recurringAppointment)

      raiseRecurringAppointmentTrackingEvent(createAppointmentSpecification, createAppointmentSpecification.repeat)
    }

    return appointmentCreated
  }

  private fun makeMainRecurringAppointment(
    appointmentsCreated: CreatedAppointmentDetailsDto,
    repeat: Repeat
  ) = MainRecurringAppointment(
    id = appointmentsCreated.appointmentEventId,
    repeatPeriod = repeat.repeatPeriod,
    count = repeat.count,
    recurringAppointments = appointmentsCreated.recurringAppointmentEventIds?.let {
      it.map { id ->
        RecurringAppointment(
          id
        )
      }
    }
  )

  private fun raiseRecurringAppointmentTrackingEvent(
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

private fun makeAppointmentDto(offenderNo: String, prisonAppointment: PrisonAppointment): AppointmentDto =
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

private fun makeVideoLinkBookingAppointmentDto(videoLinkBooking: VideoLinkBooking): VideoLinkBookingDto =
  VideoLinkBookingDto(
    id = videoLinkBooking.id!!,
    main = makeVideoLinkAppointmentDto(videoLinkBooking.main),
    pre = videoLinkBooking.pre?.let { makeVideoLinkAppointmentDto(it) },
    post = videoLinkBooking.post?.let { makeVideoLinkAppointmentDto(it) }
  )

private fun makeVideoLinkAppointmentDto(videoLinkAppointment: VideoLinkAppointment): VideoLinkAppointmentDto =
  VideoLinkAppointmentDto(
    id = videoLinkAppointment.id!!,
    bookingId = videoLinkAppointment.bookingId,
    appointmentId = videoLinkAppointment.appointmentId,
    court = videoLinkAppointment.chooseCourtName(),
    courtId = videoLinkAppointment.courtId,
    hearingType = videoLinkAppointment.hearingType,
    createdByUsername = videoLinkAppointment.createdByUsername,
    madeByTheCourt = videoLinkAppointment.madeByTheCourt
  )

private fun makeRecurringAppointmentDto(mainRecurringAppointment: MainRecurringAppointment): RecurringAppointmentDto =
  RecurringAppointmentDto(
    repeatPeriod = mainRecurringAppointment.repeatPeriod,
    count = mainRecurringAppointment.count
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
