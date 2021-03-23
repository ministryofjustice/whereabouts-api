package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking

@Component
@Primary
class DelegatingVideoLinkBookingEventListener(@Qualifier("delegate") private val delegates: List<VideoLinkBookingEventListener>) : VideoLinkBookingEventListener {
  override fun bookingCreated(booking: VideoLinkBooking, specification: VideoLinkBookingSpecification, agencyId: String) {
    delegates.forEach { it.bookingCreated(booking, specification, agencyId) }
  }

  override fun bookingUpdated(booking: VideoLinkBooking, specification: VideoLinkBookingUpdateSpecification) {
    delegates.forEach { it.bookingUpdated(booking, specification) }
  }

  override fun bookingDeleted(booking: VideoLinkBooking) {
    delegates.forEach { it.bookingDeleted(booking) }
  }
}
