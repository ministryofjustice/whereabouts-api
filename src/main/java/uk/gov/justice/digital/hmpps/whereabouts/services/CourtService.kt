package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateVideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import javax.transaction.Transactional

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

    val eventId = prisonApiService.postAppointment(
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
        appointmentId = eventId,
        bookingId = createVideoLinkAppointment.bookingId,
        court = createVideoLinkAppointment.court,
        hearingType = createVideoLinkAppointment.hearingType,
        createdByUsername = authenticationFacade.currentUsername,
        madeByTheCourt = createVideoLinkAppointment.madeByTheCourt
      )
    )
  }

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

  fun createVideoLinkBooking(specification: VideoLinkBookingSpecification): Long {
    val mainId = savePrisonAppointment(specification, specification.main)
    val preId = specification.pre?.let { savePrisonAppointment(specification, it) }
    val postId = specification.post?.let { savePrisonAppointment(specification, it) }

    val persistentBooking = videoLinkBookingRepository.save(
      VideoLinkBooking(
        pre = preId?.let { toAppointment(preId, HearingType.PRE, specification) },
        main = toAppointment(mainId, HearingType.MAIN, specification),
        post = postId?.let { toAppointment(postId, HearingType.POST, specification) }
      )
    )
    trackVideoLinkBookingCreated(persistentBooking)
    return persistentBooking.id!!
  }

  private fun trackVideoLinkBookingCreated(booking: VideoLinkBooking) {
    val properties = mutableMapOf(
      "id" to (booking.id?.toString()),
      "bookingId" to booking.main.bookingId.toString(),
      "court" to booking.main.court,
      "user" to authenticationFacade.currentUsername,
    )

    properties.putAll(appointmentDetail(booking.main))
    booking.pre?.also { properties.putAll(appointmentDetail(it)) }
    booking.post?.also { properties.putAll(appointmentDetail(it)) }

    telemetryClient.trackEvent("VideoLinkBookingCreated", properties, null)
  }

  private fun appointmentDetail(appointment: VideoLinkAppointment): Map<String, String> {
    val prefix = appointment.hearingType.name.toLowerCase()
    return mapOf(
      "${prefix}AppointmentId" to appointment.appointmentId.toString(),
      "${prefix}Id" to appointment.id.toString()
    )
  }

  private fun savePrisonAppointment(
    bookingSpec: VideoLinkBookingSpecification,
    appointmentSpec: VideoLinkAppointmentSpecification
  ): Long = prisonApiService.postAppointment(
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
}
