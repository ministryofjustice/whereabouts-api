package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking

@Primary
@Component
class DelegatingVideoLinkBookingEventListener(
  val eventStoreListener: EventStoreListener,
  val applicationInsightsEventListener: ApplicationInsightsEventListener
) : VideoLinkBookingEventListener {
  override fun bookingCreated(
    booking: VideoLinkBooking,
    specification: VideoLinkBookingSpecification,
    agencyId: String
  ) {
    eventStoreListener.bookingCreated(booking, specification, agencyId)
    applicationInsightsEventListener.bookingCreated(booking, specification, agencyId)
  }

  override fun bookingUpdated(booking: VideoLinkBooking, specification: VideoLinkBookingUpdateSpecification) {
    eventStoreListener.bookingUpdated(booking, specification)
    applicationInsightsEventListener.bookingUpdated(booking, specification)
  }

  override fun bookingDeleted(booking: VideoLinkBooking) {
    eventStoreListener.bookingDeleted(booking)
    applicationInsightsEventListener.bookingDeleted(booking)
  }
}
