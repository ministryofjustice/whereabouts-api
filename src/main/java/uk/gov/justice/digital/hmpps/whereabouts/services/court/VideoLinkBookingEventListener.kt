package uk.gov.justice.digital.hmpps.whereabouts.services.court

import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.services.AppointmentChangedEventMessage

interface VideoLinkBookingEventListener {
  fun bookingCreated(booking: VideoLinkBooking, specification: VideoLinkBookingSpecification)
  fun bookingUpdated(booking: VideoLinkBooking, specification: VideoLinkBookingUpdateSpecification)
  fun bookingDeleted(booking: VideoLinkBooking)
  fun appointmentRemovedFromBooking(booking: VideoLinkBooking)
  fun appointmentUpdatedInNomis(currentAppointment: VideoLinkAppointment, updatedAppointment: AppointmentChangedEventMessage)
}
