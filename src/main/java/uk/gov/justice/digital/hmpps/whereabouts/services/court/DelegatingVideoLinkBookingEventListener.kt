package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.listeners.AppointmentChangedEventMessage
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking

@Primary
@Component
class DelegatingVideoLinkBookingEventListener(
  val eventStoreListener: EventStoreListener,
  val applicationInsightsEventListener: ApplicationInsightsEventListener,
  val courtService: CourtService,
) : VideoLinkBookingEventListener {
  override fun bookingCreated(
    booking: VideoLinkBooking,
    specification: VideoLinkBookingSpecification,

  ) {
    eventStoreListener.bookingCreated(booking, specification)
    applicationInsightsEventListener.bookingCreated(booking, specification)
  }

  override fun bookingUpdated(
    booking: VideoLinkBooking,
    specification: VideoLinkBookingUpdateSpecification,

  ) {
    val copy = copyWithCourtName(booking)
    eventStoreListener.bookingUpdated(copy, specification)
    applicationInsightsEventListener.bookingUpdated(copy, specification)
  }

  override fun bookingDeleted(booking: VideoLinkBooking) {
    val copy = copyWithCourtName(booking)
    eventStoreListener.bookingDeleted(copy)
    applicationInsightsEventListener.bookingDeleted(copy)
  }

  override fun appointmentRemovedFromBooking(booking: VideoLinkBooking) {
    val copy = copyWithCourtName(booking)
    eventStoreListener.appointmentRemovedFromBooking(copy)
    applicationInsightsEventListener.appointmentRemovedFromBooking(copy)
  }

  override fun appointmentUpdatedInNomis(currentAppointment: VideoLinkAppointment, updatedAppointment: AppointmentChangedEventMessage) {
    applicationInsightsEventListener.appointmentUpdatedInNomis(currentAppointment, updatedAppointment)
  }

  // ensure changes aren't persisted to existing booking
  private fun copyWithCourtName(booking: VideoLinkBooking): VideoLinkBooking {
    val bookingCopy = booking.copy()
    bookingCopy.courtName = courtService.chooseCourtName(booking)
    return bookingCopy
  }
}
