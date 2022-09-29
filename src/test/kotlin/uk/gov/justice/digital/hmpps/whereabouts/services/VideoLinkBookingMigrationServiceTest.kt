package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import java.time.LocalDateTime

class VideoLinkBookingMigrationServiceTest {
  private val videoLinkBookingRepository: VideoLinkBookingRepository = mock()
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository = mock()
  private val prisonApiService: PrisonApiService = mock()

  private lateinit var service: VideoLinkBookingMigrationService

  @BeforeEach
  fun before() {
    service = VideoLinkBookingMigrationService(
      videoLinkBookingRepository,
      videoLinkAppointmentRepository,
      prisonApiService,
    )
  }

  @Test
  fun `delete video link booking when no main appointment attached`() {
    val videoLinkBooking = VideoLinkBooking(1L, 123L, "York", "EYI")
    videoLinkBooking.addPreAppointment(1L, 123L, START_DATETIME, END_DATETIME, 1L)

    service.updateVideoLink(videoLinkBooking)
    verify(videoLinkBookingRepository).delete(videoLinkBooking)
  }

  @Test
  fun `delete video link booking with all appointments when main appointment not exist in nomis`() {
    val videoLinkBooking = VideoLinkBooking(1L, 123L, "York", "EYI")
    videoLinkBooking.addPreAppointment(1L, 123L, START_DATETIME, END_DATETIME, 1L)
    videoLinkBooking.addMainAppointment(2L, 123L, START_DATETIME, END_DATETIME, 2L)

    whenever(prisonApiService.getPrisonAppointment(any())).thenReturn(null)

    service.updateVideoLink(videoLinkBooking)
    verify(videoLinkBookingRepository).delete(videoLinkBooking)
  }

  @Test
  fun `update video link booking with main appointment and delete pre appointment when pre appointment not exist in nomis`() {
    val videoLinkBooking = VideoLinkBooking(1L, 123L, "York", "EYI")
    videoLinkBooking.addPreAppointment(1L, 123L, START_DATETIME, END_DATETIME, 1L)
    videoLinkBooking.addMainAppointment(2L, 123L, START_DATETIME, END_DATETIME, 2L)

    whenever(prisonApiService.getPrisonAppointment(1L)).thenReturn(null)
    whenever(prisonApiService.getPrisonAppointment(2L)).thenReturn(
      PrisonAppointment(
        agencyId = "MDI",
        bookingId = 1L,
        startTime = START_DATETIME,
        eventId = 2L,
        eventLocationId = 123L,
        eventSubType = "VLB",
        comment = "comment",
      )
    )

    service.updateVideoLink(videoLinkBooking)

    verify(videoLinkBookingRepository).save(videoLinkBooking)
    assertThat(videoLinkBooking.appointments.get(HearingType.PRE)).isNull()
    assertThat(videoLinkBooking.prisonId).isEqualTo("MDI")
    assertThat(videoLinkBooking.comment).isEqualTo("comment")
  }
  @Test
  fun `update video link booking with main appointment and post appointment when both appointments exists in nomis`() {
    val videoLinkBooking = VideoLinkBooking(1L, 123L, "York", "EYI")
    videoLinkBooking.addPostAppointment(1L, 123L, START_DATETIME, END_DATETIME, 1L)
    videoLinkBooking.addMainAppointment(2L, 123L, START_DATETIME, END_DATETIME, 2L)

    whenever(prisonApiService.getPrisonAppointment(1L)).thenReturn(
      PrisonAppointment(
        agencyId = "MDI",
        bookingId = 1L,
        startTime = START_DATETIME,
        eventId = 2L,
        eventLocationId = 123L,
        eventSubType = "VLB",
        comment = "comment",
      )
    )
    whenever(prisonApiService.getPrisonAppointment(2L)).thenReturn(
      PrisonAppointment(
        agencyId = "MDI",
        bookingId = 1L,
        startTime = START_DATETIME,
        eventId = 2L,
        eventLocationId = 123L,
        eventSubType = "VLB",
        comment = "comment",
      )
    )

    service.updateVideoLink(videoLinkBooking)

    verify(videoLinkBookingRepository).save(videoLinkBooking)
    assertThat(videoLinkBooking.prisonId).isEqualTo("MDI")
    assertThat(videoLinkBooking.comment).isEqualTo("comment")
    assertThat(videoLinkBooking.appointments.get(HearingType.MAIN)?.startDateTime).isEqualToIgnoringSeconds(START_DATETIME)
    assertThat(videoLinkBooking.appointments.get(HearingType.POST)?.startDateTime).isEqualToIgnoringSeconds(START_DATETIME)
    assertThat(videoLinkBooking.appointments.get(HearingType.PRE)).isNull()
  }

  companion object {
    private val START_DATETIME = LocalDateTime.of(2021, 1, 1, 12, 0)
    private val END_DATETIME = LocalDateTime.of(2021, 1, 1, 13, 0)
  }
}
