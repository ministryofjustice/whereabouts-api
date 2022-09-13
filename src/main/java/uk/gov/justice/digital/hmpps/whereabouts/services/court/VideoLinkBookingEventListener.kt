package uk.gov.justice.digital.hmpps.whereabouts.services.court

import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking

interface VideoLinkBookingEventListener {
  fun bookingCreated(booking: VideoLinkBooking, specification: VideoLinkBookingSpecification, agencyId: String)
  fun bookingUpdated(booking: VideoLinkBooking, specification: VideoLinkBookingUpdateSpecification, agencyId: String)
  fun bookingDeleted(booking: VideoLinkBooking)
}
