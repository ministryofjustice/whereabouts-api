package uk.gov.justice.digital.hmpps.whereabouts.services.court

import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking

interface VideoLinkBookingEventListener {
  fun bookingCreated(booking: VideoLinkBooking, specification: VideoLinkBookingSpecification)
  fun bookingUpdated(booking: VideoLinkBooking, specification: VideoLinkBookingUpdateSpecification)
  fun bookingDeleted(booking: VideoLinkBooking)
}
