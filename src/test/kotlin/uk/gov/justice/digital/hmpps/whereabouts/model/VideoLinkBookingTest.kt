package uk.gov.justice.digital.hmpps.whereabouts.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import java.time.LocalDateTime

class VideoLinkBookingTest {
  val COURT_NAME = "The Court"
  val COURT_ID = "TC"
  val AGENCY_ID = "WWI"
  val START_DATE_TIME = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
  val END_DATE_TIME = LocalDateTime.of(2022, 1, 1, 11, 0, 0)

  var videoLinkBooking =
    VideoLinkBooking(courtName = COURT_NAME, courtId = COURT_ID, offenderBookingId = 1L, agencyId = AGENCY_ID).apply {
      addPreAppointment(1L, 10L, START_DATE_TIME, END_DATE_TIME)
      addMainAppointment(2L, 20L, START_DATE_TIME, END_DATE_TIME)
      addPostAppointment(3L, 30L, START_DATE_TIME, END_DATE_TIME)
    }

  @Test
  fun `preAppointment`() {
    assertThat(videoLinkBooking.appointments[PRE])
      .isNotNull()
      .extracting("appointmentId", "hearingType")
      .containsExactly(1L, PRE)
  }

  @Test
  fun `mainAppointment`() {
    assertThat(videoLinkBooking.appointments[MAIN])
      .isNotNull()
      .extracting("appointmentId", "hearingType")
      .containsExactly(2L, MAIN)
  }

  @Test
  fun `postAppointment`() {
    assertThat(videoLinkBooking.appointments[POST])
      .isNotNull()
      .extracting("appointmentId", "hearingType")
      .containsExactly(3L, POST)
  }

  @Test
  fun `no appointments`() {
    val vlb = VideoLinkBooking(offenderBookingId = 1L, agencyId = AGENCY_ID)
    assertThat(vlb.appointments[PRE]).isNull()
    assertThat(vlb.appointments[MAIN]).isNull()
    assertThat(vlb.appointments[POST]).isNull()
  }
}
