package uk.gov.justice.digital.hmpps.whereabouts.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class VideoLinkAppointmentTest {
  private val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
  private val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)
  private val locationId = 10L
  @Test
  fun `toString check`() {
    assertThat(
      VideoLinkAppointment(
        appointmentId = 1L,
        hearingType = HearingType.MAIN,
        locationId = locationId,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        videoLinkBooking = VideoLinkBooking(offenderBookingId = 2L, agencyId = "WWI", comment = "some comment")
      ).toString()
    ).isEqualTo("VideoLinkAppointment(id = null, appointmentId = 1, startDateTime = 2022-01-01T10:00, endDateTime = 2022-01-01T11:00, hearingType = MAIN)")
  }

  @Test
  fun `equality by appointmentId same id`() {
    val dontCareBooking = VideoLinkBooking(offenderBookingId = 1, agencyId = "WWI")
    val vla1 = VideoLinkAppointment(id = 1, appointmentId = 2, locationId = locationId, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking, startDateTime = startDateTime, endDateTime = endDateTime)
    val vla2 = VideoLinkAppointment(id = 1, appointmentId = 2, locationId = locationId, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking, startDateTime = startDateTime, endDateTime = endDateTime)
    assertThat(vla1.equals(vla2)).isTrue
  }

  @Test
  fun `equality other null`() {
    val dontCareBooking = VideoLinkBooking(offenderBookingId = 1, agencyId = "WWI")
    val vla1 = VideoLinkAppointment(id = 1, appointmentId = 2, locationId = locationId, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking, startDateTime = startDateTime, endDateTime = endDateTime)
    assertThat(vla1.equals(null)).isFalse
  }

  @Test
  fun `equality other not a VideoLinkAppointment`() {
    val dontCareBooking = VideoLinkBooking(offenderBookingId = 1, agencyId = "WWI")
    val vla1 = VideoLinkAppointment(id = 1, appointmentId = 2, locationId = locationId, hearingType = HearingType.MAIN, videoLinkBooking = dontCareBooking, startDateTime = startDateTime, endDateTime = endDateTime)
    assertThat(vla1.equals(object {})).isFalse
  }
}
