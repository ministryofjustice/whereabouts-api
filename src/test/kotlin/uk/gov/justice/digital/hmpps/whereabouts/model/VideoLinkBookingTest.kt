package uk.gov.justice.digital.hmpps.whereabouts.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE

class VideoLinkBookingTest {
  val COURT_NAME = "The Court"
  val COURT_ID = "TC"

  var videoLinkBooking = VideoLinkBooking(courtName = COURT_NAME, courtId = COURT_ID, offenderBookingId = 1L).apply {
    addPreAppointment(appointmentId = 1L)
    addMainAppointment(appointmentId = 2L)
    addPostAppointment(appointmentId = 3L)
  }

  @Test
  fun `matches no court or courtId`() {
    assertThat(videoLinkBooking.matchesCourt(null, null)).isTrue
  }

  @Test
  fun `matching court`() {
    assertThat(videoLinkBooking.matchesCourt(COURT_NAME, null)).isTrue
  }

  @Test
  fun `no matching court`() {
    assertThat(videoLinkBooking.matchesCourt("Some other court", null)).isFalse
  }

  @Test
  fun `matching courtId`() {
    assertThat(videoLinkBooking.matchesCourt(null, COURT_ID)).isTrue
  }

  @Test
  fun `no matching courtId`() {
    assertThat(videoLinkBooking.matchesCourt(null, "XXX")).isFalse
  }

  @Test
  fun `courtId takes precedence over court name`() {
    assertThat(videoLinkBooking.matchesCourt("Some other court", COURT_ID)).isTrue
  }

  @Test
  fun `preApppointment`() {
    assertThat(videoLinkBooking.appointments[PRE])
      .isNotNull()
      .extracting("appointmentId", "hearingType")
      .containsExactly(1L, PRE)
  }

  @Test
  fun `mainApppointment`() {
    assertThat(videoLinkBooking.appointments[MAIN])
      .isNotNull()
      .extracting("appointmentId", "hearingType")
      .containsExactly(2L, MAIN)
  }

  @Test
  fun `postApppointment`() {
    assertThat(videoLinkBooking.appointments[POST])
      .isNotNull()
      .extracting("appointmentId", "hearingType")
      .containsExactly(3L, POST)
  }

  @Test
  fun `no appointments`() {
    val vlb = VideoLinkBooking(offenderBookingId = 1L)
    assertThat(vlb.appointments[PRE]).isNull()
    assertThat(vlb.appointments[MAIN]).isNull()
    assertThat(vlb.appointments[POST]).isNull()
  }
}
