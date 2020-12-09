package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateVideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.time.LocalDate
import javax.persistence.EntityNotFoundException

const val VIDEO_LINK_APPOINTMENT_TYPE = "VLB"

@Service
class CourtService(
  private val authenticationFacade: AuthenticationFacade,
  private val prisonApiService: PrisonApiService,
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val telemetryClient: TelemetryClient,
  @Value("\${courts}") private val courts: String
) {

  fun getCourtLocations() = courts.split(",").toSet()

  @Transactional
  fun createVideoLinkAppointment(createVideoLinkAppointment: CreateVideoLinkAppointment) {

    val event = prisonApiService.postAppointment(
      createVideoLinkAppointment.bookingId,
      CreateBookingAppointment(
        appointmentType = VIDEO_LINK_APPOINTMENT_TYPE,
        locationId = createVideoLinkAppointment.locationId,
        comment = createVideoLinkAppointment.comment,
        startTime = createVideoLinkAppointment.startTime.toString(),
        endTime = createVideoLinkAppointment.endTime.toString()
      )
    )

    videoLinkAppointmentRepository.save(
      VideoLinkAppointment(
        appointmentId = event.eventId,
        bookingId = createVideoLinkAppointment.bookingId,
        court = createVideoLinkAppointment.court,
        hearingType = createVideoLinkAppointment.hearingType,
        createdByUsername = authenticationFacade.currentUsername,
        madeByTheCourt = createVideoLinkAppointment.madeByTheCourt
      )
    )
  }

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
    val mainEvent = savePrisonAppointment(specification, specification.main)
    val preEvent = specification.pre?.let { savePrisonAppointment(specification, it) }
    val postEvent = specification.post?.let { savePrisonAppointment(specification, it) }

    val persistentBooking = videoLinkBookingRepository.save(
      VideoLinkBooking(
        pre = preEvent?.let { toAppointment(preEvent.eventId, HearingType.PRE, specification) },
        main = toAppointment(mainEvent.eventId, HearingType.MAIN, specification),
        post = postEvent?.let { toAppointment(postEvent.eventId, HearingType.POST, specification) }
      )
    )
    trackVideoLinkBookingCreated(persistentBooking, specification, mainEvent.agencyId)
    return persistentBooking.id!!
  }

  private fun trackVideoLinkBookingCreated(
    booking: VideoLinkBooking,
    specification: VideoLinkBookingSpecification,
    agencyId: String
  ) {
    val properties = mutableMapOf(
      "id" to (booking.id?.toString()),
      "bookingId" to booking.main.bookingId.toString(),
      "court" to booking.main.court,
      "user" to authenticationFacade.currentUsername,
      "agencyId" to agencyId,
      "madeByTheCourt" to booking.main.madeByTheCourt.toString(),
    )

    properties.putAll(appointmentDetail(booking.main, specification.main))
    booking.pre?.also { properties.putAll(appointmentDetail(it, specification.pre!!)) }
    booking.post?.also { properties.putAll(appointmentDetail(it, specification.post!!)) }

    telemetryClient.trackEvent("VideoLinkBookingCreated", properties, null)
  }

  private fun appointmentDetail(
    appointment: VideoLinkAppointment,
    specification: VideoLinkAppointmentSpecification
  ): Map<String, String> {
    val prefix = appointment.hearingType.name.toLowerCase()
    return mapOf(
      "${prefix}AppointmentId" to appointment.appointmentId.toString(),
      "${prefix}Id" to appointment.id.toString(),
      "${prefix}Start" to specification.startTime.toString(),
      "${prefix}End" to specification.endTime.toString(),
    )
  }

  private fun savePrisonAppointment(
    bookingSpec: VideoLinkBookingSpecification,
    appointmentSpec: VideoLinkAppointmentSpecification
  ): Event = prisonApiService.postAppointment(
    bookingSpec.bookingId!!,
    CreateBookingAppointment(
      appointmentType = VIDEO_LINK_APPOINTMENT_TYPE,
      locationId = appointmentSpec.locationId!!,
      startTime = appointmentSpec.startTime.toString(),
      endTime = appointmentSpec.endTime.toString(),
      comment = bookingSpec.comment
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
      ?: throw EntityNotFoundException("main appointment with id ${booking.main.appointmentId} not found in nomis")
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
  fun deleteVideoLinkBooking(videoBookingId: Long) {
    val booking = videoLinkBookingRepository.findById(videoBookingId).orElseThrow {
      EntityNotFoundException("Video link booking with id $videoBookingId not found")
    }

    booking.toAppointments().forEach { prisonApiService.deleteAppointment(it.appointmentId) }
    videoLinkBookingRepository.deleteById(booking.id!!)

    trackVideoLinkBookingDeleted(booking)
  }

  @Transactional(readOnly = true)
  fun getVideoLinkBookingsForDateAndCourt(date: LocalDate, court: String?): List<VideoLinkBookingResponse> {
    val scheduledAppointments = prisonApiService
      .getScheduledAppointmentsByAgencyAndDate("WWI", date)
      .filter { it.appointmentTypeCode == "VLB" }

    val scheduledAppointmentIds = scheduledAppointments.map { it.id }

    val bookings = videoLinkBookingRepository.findByMainAppointmentIds(scheduledAppointmentIds)

    val scheduledAppointmentsById = scheduledAppointments.associateBy { it.id }

    return bookings
      .filter { scheduledAppointmentsById.containsKey(it.main.appointmentId) }
      .filter { if (court == null) true else it.main.court == court }
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

  private fun toVideoLinkAppointmentDto(scheduledAppointment: ScheduledAppointmentDto?) =
    scheduledAppointment?.let {
      VideoLinkBookingResponse.LocationTimeslot(
        locationId = it.locationId,
        startTime = it.startTime,
        endTime = it.endTime
      )
    }

  private fun trackVideoLinkBookingDeleted(
    booking: VideoLinkBooking
  ) {
    val properties = mutableMapOf(
      "id" to (booking.id?.toString()),
      "bookingId" to booking.main.bookingId.toString(),
      "court" to booking.main.court,
      "user" to authenticationFacade.currentUsername,
    )

    properties.putAll(deletedAppointmentDetail(booking.main))
    booking.pre?.also { properties.putAll(deletedAppointmentDetail(it)) }
    booking.post?.also { properties.putAll(deletedAppointmentDetail(it)) }

    telemetryClient.trackEvent("VideoLinkBookingDeleted", properties, null)
  }

  private fun deletedAppointmentDetail(
    appointment: VideoLinkAppointment,
  ): Map<String, String> {
    val prefix = appointment.hearingType.name.toLowerCase()
    return mapOf(
      "${prefix}AppointmentId" to appointment.appointmentId.toString(),
      "${prefix}Id" to appointment.id.toString(),
    )
  }
}
