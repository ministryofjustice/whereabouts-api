package uk.gov.justice.digital.hmpps.whereabouts.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoLinkBookingTest {
  val COURT_NAME = "The Court"
  val COURT_ID = "TC"

  var videoLinkBooking = VideoLinkBooking(
    main = VideoLinkAppointment(
      court = COURT_NAME,
      courtId = COURT_ID,
      bookingId = 1L,
      hearingType = HearingType.MAIN,
      appointmentId = -1L
    )
  )

  @Test
  fun `matches no court or courtId`() {
    assertThat(videoLinkBooking.matchesCourt(null, null)).isTrue()
  }

  @Test
  fun `matching court`() {
    assertThat(videoLinkBooking.matchesCourt(COURT_NAME, null)).isTrue()
  }

  @Test
  fun `no matching court`() {
    assertThat(videoLinkBooking.matchesCourt("Some other court", null)).isFalse()
  }

  @Test
  fun `matching courtId`() {
    assertThat(videoLinkBooking.matchesCourt(null, COURT_ID)).isTrue()
  }

  @Test
  fun `no matching courtId`() {
    assertThat(videoLinkBooking.matchesCourt(null, "XXX")).isFalse()
  }

  @Test
  fun `courtId takes precedence over court name`() {
    assertThat(videoLinkBooking.matchesCourt("Some other court", COURT_ID)).isTrue()
  }
}
