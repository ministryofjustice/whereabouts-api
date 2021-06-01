package uk.gov.justice.digital.hmpps.whereabouts.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoLinkAppointmentTest {
  @Test
  fun `toString check`() {
    assertThat(
      VideoLinkAppointment(
        appointmentId = 1L,
        hearingType = HearingType.MAIN,
        videoLinkBooking = VideoLinkBooking(offenderBookingId = 2L)
      ).toString()
    ).isEqualTo("VideoLinkAppointment(id = null, appointmentId = 1, hearingType = MAIN)")
  }

  @Test
  fun `equality by appointmentId same id`() {
    val dontCareBooking = VideoLinkBooking(offenderBookingId = 1)
    val vla1 = VideoLinkAppointment(id = 1, appointmentId = 2, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking)
    val vla2 = VideoLinkAppointment(id = 1, appointmentId = 2, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking)
    assertThat(vla1.equals(vla2)).isTrue
  }

  @Test
  fun `equality other null`() {
    val dontCareBooking = VideoLinkBooking(offenderBookingId = 1)
    val vla1 = VideoLinkAppointment(id = 1, appointmentId = 2, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking)
    assertThat(vla1.equals(null)).isFalse
  }

  @Test
  fun `equality other not a VideoLinkAppointment`() {
    val dontCareBooking = VideoLinkBooking(offenderBookingId = 1)
    val vla1 = VideoLinkAppointment(id = 1, appointmentId = 2, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking)
    assertThat(vla1.equals(object {})).isFalse
  }
}
