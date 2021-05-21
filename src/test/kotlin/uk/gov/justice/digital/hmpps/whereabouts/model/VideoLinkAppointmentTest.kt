package uk.gov.justice.digital.hmpps.whereabouts.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoLinkAppointmentTest {
  val COURT_NAME = "Court Name"
  val COURT_ID = "CID"

  val appointment = VideoLinkAppointment(bookingId = 1L, appointmentId = 1L, hearingType = HearingType.MAIN)
  @Test
  fun `chooseCourtName with name`() {
    assertThat(appointment.copy(court = COURT_NAME).chooseCourtName()).isEqualTo(COURT_NAME)
  }

  @Test
  fun `chooseCourtName with name and courtId selects name`() {
    assertThat(appointment.copy(court = COURT_NAME, courtId = COURT_ID).chooseCourtName()).isEqualTo(COURT_NAME)
  }

  @Test
  fun `chooseCourtName with courtId only selects courtId`() {
    assertThat(appointment.copy(courtId = COURT_ID).chooseCourtName()).isEqualTo(COURT_ID)
  }

  @Test
  fun `chooseCourtName neither court name or courtId selects 'Unknown'`() {
    assertThat(appointment.copy().chooseCourtName()).isEqualTo("Unknown")
  }
}
