package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateVideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import javax.persistence.EntityNotFoundException
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

    fun deleteVideoLinkBooking(videoBookingId: Long) {
      val booking = videoLinkBookingRepository.findById(videoBookingId).orElseThrow {
        EntityNotFoundException("Video link booking with id $videoBookingId not found")
      }
      booking.toAppointments().forEach { prisonApiService.deleteAppointment(it.appointmentId) }
    }

}

/**

Load up the booking, if it does not exist then we throw an exception

For each appointment
  Call the prison API end-point DELETE /appointments/{appointmentId} for the appointment (Ignore 404 Not Found response)

For each appointment
  Deleting the VIDEO_LINK_APPOINTMENT record

Then delete the VIDEO_LINK_BOOKING itself

Then fire an Application Insights DeleteVideoLinkBooking custom event

If any of the calls to prison api fail then the whole operation fails and the VIDEO_LINK_BOOKING and referenced VIDEO_LINK_APPOINTMENT records are left intact

**/