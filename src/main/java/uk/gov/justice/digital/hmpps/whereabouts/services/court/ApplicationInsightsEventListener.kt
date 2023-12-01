package uk.gov.justice.digital.hmpps.whereabouts.services.court

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.listeners.AppointmentChangedEventMessage
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade

@Component
class ApplicationInsightsEventListener(
  private val authenticationFacade: AuthenticationFacade,
  private val telemetryClient: TelemetryClient,
) : VideoLinkBookingEventListener {
  override fun bookingCreated(
    booking: VideoLinkBooking,
    specification: VideoLinkBookingSpecification,

  ) {
    val properties = mutableMapOf(
      "id" to (booking.id?.toString()),
      "bookingId" to booking.offenderBookingId.toString(),
      "court" to specification.court,
      "courtId" to specification.courtId,
      "user" to authenticationFacade.currentUsername,
      "agencyId" to booking.prisonId,
      "madeByTheCourt" to specification.madeByTheCourt.toString(),
    )

    booking.appointments[MAIN]?.also { properties.putAll(appointmentDetail(it, specification.main)) }
    booking.appointments[PRE]?.also { properties.putAll(appointmentDetail(it, specification.pre!!)) }
    booking.appointments[POST]?.also { properties.putAll(appointmentDetail(it, specification.post!!)) }

    telemetryClient.trackEvent("VideoLinkBookingCreated", properties, null)
  }

  override fun bookingUpdated(
    booking: VideoLinkBooking,
    specification: VideoLinkBookingUpdateSpecification,

  ) {
    val properties = mutableMapOf(
      "id" to (booking.id?.toString()),
      "bookingId" to booking.offenderBookingId.toString(),
      "courtId" to specification.courtId,
      "agencyId" to booking.prisonId,
      "user" to authenticationFacade.currentUsername,
    )

    booking.appointments[MAIN]?.also { properties.putAll(appointmentDetail(it, specification.main)) }
    booking.appointments[PRE]?.also { properties.putAll(appointmentDetail(it, specification.pre!!)) }
    booking.appointments[POST]?.also { properties.putAll(appointmentDetail(it, specification.post!!)) }

    telemetryClient.trackEvent("VideoLinkBookingUpdated", properties, null)
  }

  override fun bookingDeleted(booking: VideoLinkBooking) {
    telemetryClient.trackEvent("VideoLinkBookingDeleted", telemetryProperties(booking), null)
  }

  override fun appointmentUpdatedInNomis(currentAppointment: VideoLinkAppointment, updatedAppointment: AppointmentChangedEventMessage) {
    val properties = mutableMapOf(
      "bookingId" to (currentAppointment.videoLinkBooking?.id.toString()),
      "appointmentId" to currentAppointment.appointmentId.toString(),
      "agencyId" to currentAppointment.videoLinkBooking?.prisonId,
      "court" to currentAppointment.videoLinkBooking?.courtName,
      "courtId" to currentAppointment.videoLinkBooking?.courtId,
      "current_startDate" to currentAppointment.startDateTime.toString(),
      "current_endDate" to currentAppointment.endDateTime.toString(),
      "update_startDate" to updatedAppointment.scheduledStartTime,
      "update_endDate" to updatedAppointment.scheduledEndTime,
    )

    telemetryClient.trackEvent("VideoLinkAppointmentUpdated", properties, null)
  }

  private fun telemetryProperties(booking: VideoLinkBooking): MutableMap<String, String?> {
    val properties = mutableMapOf(
      "id" to (booking.id?.toString()),
      "bookingId" to booking.offenderBookingId.toString(),
      "court" to booking.courtName,
      "courtId" to booking.courtId,
      "user" to authenticationFacade.currentUsername,
    )

    booking.appointments.values.forEach { properties.putAll(appointmentDetail(it)) }
    return properties
  }

  private fun appointmentDetail(
    appointment: VideoLinkAppointment,
    specification: VideoLinkAppointmentSpecification,
  ): Map<String, String> {
    val prefix = appointment.hearingType.name.lowercase()
    return mapOf(
      "${prefix}AppointmentId" to appointment.appointmentId.toString(),
      "${prefix}Id" to appointment.id.toString(),
      "${prefix}Start" to specification.startTime.toString(),
      "${prefix}End" to specification.endTime.toString(),
    )
  }

  private fun appointmentDetail(appointment: VideoLinkAppointment): Map<String, String> {
    val prefix = appointment.hearingType.name.lowercase()
    return mapOf(
      "${prefix}AppointmentId" to appointment.appointmentId.toString(),
      "${prefix}Id" to appointment.id.toString(),
    )
  }
}
