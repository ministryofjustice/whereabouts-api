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
