package uk.gov.justice.digital.hmpps.whereabouts.model

import com.nhaarman.mockitokotlin2.argThat

/**
 * An implementation of equals which compares the properties of two VideoLinkBookings, including the appointments map.
 * Used for testing.
 */
fun VideoLinkBooking.equalByProperties(other: VideoLinkBooking?): Boolean {
  if (this === other) return true
  if (other == null) return false

  return id == other.id &&
    offenderBookingId == other.offenderBookingId &&
    courtId == other.courtId &&
    courtName == other.courtName &&
    madeByTheCourt == other.madeByTheCourt &&
    createdByUsername == other.createdByUsername &&
    appointments == other.appointments
}

/**
 * A bespoke Mockito matcher that deeply compares the properties two VideoLinkBooking objects. This is needed
 * because the VideoLinkBooking equals method implements Hibernate equality by comparing Ids. See BaseEntity, VideoLinkBooking.
 */
fun eqByProps(videoLinkBooking: VideoLinkBooking) = argThat<VideoLinkBooking> { equalByProperties(videoLinkBooking) }
