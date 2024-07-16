package uk.gov.justice.digital.hmpps.whereabouts.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers
import java.time.LocalDateTime

class VideoLinkBookingTest {
  private val courtName = "The Court"
  private val courtId = "TC"
  private val courtHearingType = CourtHearingType.APPEAL
  private val prisonId = "WWI"
  private val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
  private val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)

  private val videoLinkBooking =
    DataHelpers.makeVideoLinkBooking(id = 1L, courtName = courtName, courtId = courtId, courtHearingType = courtHearingType, offenderBookingId = 1L, prisonId = prisonId).apply {
      addPreAppointment(1L, 10L, startDateTime, endDateTime)
      addMainAppointment(2L, 20L, startDateTime, endDateTime)
      addPostAppointment(3L, 30L, startDateTime, endDateTime)
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
    val vlb = VideoLinkBooking(offenderBookingId = 1L, prisonId = prisonId)
    assertThat(vlb.appointments[PRE]).isNull()
    assertThat(vlb.appointments[MAIN]).isNull()
    assertThat(vlb.appointments[POST]).isNull()
  }
}
