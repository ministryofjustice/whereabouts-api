package uk.gov.justice.digital.hmpps.whereabouts.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentCreatedDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDetailsDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentSearchDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentSearchDto
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import java.time.LocalDate
import javax.persistence.EntityNotFoundException

@Service
class AppointmentService(
  private val prisonApiService: PrisonApiService,
  private val videoLinkBookingRepository: VideoLinkBookingRepository
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

  fun getAppointment(appointmentId: Long): AppointmentDetailsDto {
    val prisonAppointment: PrisonAppointment = prisonApiService.getPrisonAppointment(appointmentId)
      ?: throw EntityNotFoundException("Appointment $appointmentId does not exist")

    val offenderNo = prisonApiService.getOffenderNoFromBookingId(prisonAppointment.bookingId)
    val videoLinkBooking: VideoLinkBooking? =
      videoLinkBookingRepository.findByMainAppointmentIds(listOf(appointmentId)).firstOrNull()

    return AppointmentDetailsDto(
      appointment = makeAppointmentDto(offenderNo, prisonAppointment),
      videoLinkBooking = videoLinkBooking?.let { makeVideoLinkBookingAppointmentDto(it) }
    )
  }

  fun createAppointment(createAppointmentSpecification: CreateAppointmentSpecification): AppointmentCreatedDto {
    val event = prisonApiService.postAppointment(

      createAppointmentSpecification.bookingId!!,
      CreateBookingAppointment(
        locationId = createAppointmentSpecification.locationId,
        startTime = createAppointmentSpecification.startTime.toString(),
        endTime = createAppointmentSpecification.endTime.toString(),
        comment = createAppointmentSpecification.comment,
        appointmentType = createAppointmentSpecification.appointmentType,
      )
    )

    return AppointmentCreatedDto(
      mainAppointmentId = event.eventId
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
    offenderNo = offenderNo
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
    court = videoLinkAppointment.court,
    hearingType = videoLinkAppointment.hearingType,
    createdByUsername = videoLinkAppointment.createdByUsername,
    madeByTheCourt = videoLinkAppointment.madeByTheCourt

  )
