package uk.gov.justice.digital.hmpps.whereabouts.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class VideoLinkAppointmentTest {
  private val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
  private val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)
  @Test
  fun `toString check`() {
    assertThat(
      VideoLinkAppointment(
        appointmentId = 1L,
        hearingType = HearingType.MAIN,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        videoLinkBooking = VideoLinkBooking(offenderBookingId = 2L, agencyId = "WWI", comment = "some comment")
      ).toString()
    ).isEqualTo("VideoLinkAppointment(id = null, appointmentId = 1, hearingType = MAIN)")
  }

  @Test
  fun `equality by appointmentId same id`() {
    val dontCareBooking = VideoLinkBooking(offenderBookingId = 1, agencyId = "WWI")
    val vla1 = VideoLinkAppointment(id = 1, appointmentId = 2, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking, startDateTime = startDateTime, endDateTime = endDateTime)
    val vla2 = VideoLinkAppointment(id = 1, appointmentId = 2, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking, startDateTime = startDateTime, endDateTime = endDateTime)
    assertThat(vla1.equals(vla2)).isTrue
  }

  @Test
  fun `equality other null`() {
    val dontCareBooking = VideoLinkBooking(offenderBookingId = 1, agencyId = "WWI")
    val vla1 = VideoLinkAppointment(id = 1, appointmentId = 2, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking, startDateTime = startDateTime, endDateTime = endDateTime)
    assertThat(vla1.equals(null)).isFalse
  }

  @Test
  fun `equality other not a VideoLinkAppointment`() {
    val dontCareBooking = VideoLinkBooking(offenderBookingId = 1, agencyId = "WWI")
    val vla1 = VideoLinkAppointment(id = 1, appointmentId = 2, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking, startDateTime = startDateTime, endDateTime = endDateTime)
    assertThat(vla1.equals(object {})).isFalse
  }
}
