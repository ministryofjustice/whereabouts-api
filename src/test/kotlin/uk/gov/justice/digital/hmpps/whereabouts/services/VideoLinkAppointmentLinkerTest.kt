package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import java.time.LocalDateTime

class VideoLinkAppointmentLinkerTest {
  private val prisonApiService: PrisonApiService = mock()
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository = mock()
  private val videoLinkBookingRepository: VideoLinkBookingRepository = mock()

  private val service = VideoLinkAppointmentLinker(
    videoLinkAppointmentRepository,
    videoLinkBookingRepository,
    prisonApiService
  )

  @Test
  fun `Links no bookings`() {
    assertThat(service.videoLinkBookingsForOffenderBookingId(1L)).isEmpty()
  }

  @Test
  fun `Builds no VideoLinkBookings if no matches in VideoLinkAppointmentRepository`() {
    whenever(
      videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(
        anyLong(),
        eq(HearingType.MAIN)
      )
    ).thenReturn(listOf())
    whenever(
      videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(
        anyLong(),
        eq(HearingType.PRE)
      )
    ).thenReturn(listOf())
    whenever(
      videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(
        anyLong(),
        eq(HearingType.POST)
      )
    ).thenReturn(listOf())

    assertThat(service.videoLinkBookingsForOffenderBookingId(1L)).isEmpty()

    verifyZeroInteractions(prisonApiService)
  }

  @Test
  fun `Builds VideoLinkBooking with main appointment`() {
    val startTime = LocalDateTime.of(2020, 10, 1, 0, 9, 0)

    whenever(prisonApiService.getPrisonAppointment(anyLong())).thenReturn(prisonAppointment(0, startTime))

    whenever(
      videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(
        anyLong(),
        eq(HearingType.PRE)
      )
    ).thenReturn(listOf())
    whenever(
      videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(
        anyLong(),
        eq(HearingType.POST)
      )
    ).thenReturn(listOf())
    whenever(
      videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(
        anyLong(),
        eq(HearingType.MAIN)
      )
    ).thenReturn(videoLinkAppointments(HearingType.MAIN, 0))

    assertThat(service.videoLinkBookingsForOffenderBookingId(1L))
      .hasSize(1)
      .element(0).isEqualTo(VideoLinkBooking(main = videoLinkAppointments(HearingType.MAIN, 0)[0]))
  }

  @Test
  fun `Handles missing prison appointment`() {

    whenever(prisonApiService.getPrisonAppointment(anyLong())).thenReturn(null)

    whenever(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(anyLong(), eq(HearingType.PRE)))
      .thenReturn(listOf())

    whenever(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(anyLong(), eq(HearingType.POST)))
      .thenReturn(listOf())

    whenever(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(anyLong(), eq(HearingType.MAIN)))
      .thenReturn(videoLinkAppointments(HearingType.MAIN, 0))

    assertThat(service.videoLinkBookingsForOffenderBookingId(1L)).hasSize(0)
  }

  @Test
  fun `Builds VideoLinkBooking from matching main, pre and post appointments`() {
    val startTime = LocalDateTime.of(2020, 10, 1, 0, 9, 0)

    whenever(prisonApiService.getPrisonAppointment(0L)).thenReturn(prisonAppointment(0, startTime))
    whenever(prisonApiService.getPrisonAppointment(1L)).thenReturn(prisonAppointment(1, startTime.plusHours(1)))
    whenever(prisonApiService.getPrisonAppointment(2L)).thenReturn(prisonAppointment(2, startTime.plusHours(2)))

    val preAppts = videoLinkAppointments(HearingType.PRE, 0)
    val mainAppts = videoLinkAppointments(HearingType.MAIN, 1)
    val postAppts = videoLinkAppointments(HearingType.POST, 2)

    whenever(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(anyLong(), eq(HearingType.PRE)))
      .thenReturn(preAppts)

    whenever(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(anyLong(), eq(HearingType.MAIN)))
      .thenReturn(mainAppts)

    whenever(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(anyLong(), eq(HearingType.POST)))
      .thenReturn(postAppts)

    assertThat(service.videoLinkBookingsForOffenderBookingId(1L))
      .hasSize(1)
      .element(0).isEqualTo(VideoLinkBooking(main = mainAppts[0], pre = preAppts[0], post = postAppts[0]))
  }

  @Test
  fun `Builds VideoLinkBooking from main only when pre and post times do not align`() {
    val startTime = LocalDateTime.of(2020, 10, 1, 0, 9, 0)

    whenever(prisonApiService.getPrisonAppointment(0L))
      .thenReturn(prisonAppointment(0, startTime.plusMinutes(1)))

    whenever(prisonApiService.getPrisonAppointment(1L))
      .thenReturn(prisonAppointment(1, startTime.plusHours(1)))

    whenever(prisonApiService.getPrisonAppointment(2L))
      .thenReturn(prisonAppointment(2, startTime.plusHours(2).plusMinutes(1)))

    val preAppts = videoLinkAppointments(HearingType.PRE, 0)
    val mainAppts = videoLinkAppointments(HearingType.MAIN, 1)
    val postAppts = videoLinkAppointments(HearingType.POST, 2)

    whenever(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(anyLong(), eq(HearingType.PRE)))
      .thenReturn(preAppts)

    whenever(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(anyLong(), eq(HearingType.MAIN)))
      .thenReturn(mainAppts)

    whenever(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(anyLong(), eq(HearingType.POST)))
      .thenReturn(postAppts)

    assertThat(service.videoLinkBookingsForOffenderBookingId(1L))
      .hasSize(1)
      .element(0).isEqualTo(VideoLinkBooking(main = mainAppts[0]))
  }

  @Test
  fun `Builds VideoLinkBookings for multiple appointments`() {
    val startTime = LocalDateTime.of(2020, 10, 1, 0, 9, 0)

    whenever(prisonApiService.getPrisonAppointment(0L)).thenReturn(prisonAppointment(0, startTime))
    whenever(prisonApiService.getPrisonAppointment(1L)).thenReturn(prisonAppointment(1, startTime.plusHours(1)))
    whenever(prisonApiService.getPrisonAppointment(2L)).thenReturn(prisonAppointment(2, startTime.plusHours(2)))

    whenever(prisonApiService.getPrisonAppointment(4L)).thenReturn(prisonAppointment(4, startTime.plusHours(4)))

    whenever(prisonApiService.getPrisonAppointment(6L)).thenReturn(prisonAppointment(6, startTime.plusHours(6)))
    whenever(prisonApiService.getPrisonAppointment(7L)).thenReturn(prisonAppointment(7, startTime.plusHours(7)))

    whenever(prisonApiService.getPrisonAppointment(10L)).thenReturn(prisonAppointment(10, startTime.plusHours(10)))
    whenever(prisonApiService.getPrisonAppointment(11L)).thenReturn(prisonAppointment(11, startTime.plusHours(11)))

    val preAppts = videoLinkAppointments(HearingType.PRE, 0, 6, 12)
    val mainAppts = videoLinkAppointments(HearingType.MAIN, 1, 4, 7, 10, 13)
    val postAppts = videoLinkAppointments(HearingType.POST, 2, 11, 14)

    whenever(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(anyLong(), eq(HearingType.PRE)))
      .thenReturn(preAppts)

    whenever(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(anyLong(), eq(HearingType.MAIN)))
      .thenReturn(mainAppts)

    whenever(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(anyLong(), eq(HearingType.POST)))
      .thenReturn(postAppts)

    assertThat(service.videoLinkBookingsForOffenderBookingId(1L))
      .containsExactlyInAnyOrder(
        VideoLinkBooking(main = mainAppts[0], pre = preAppts[0], post = postAppts[0]),
        VideoLinkBooking(main = mainAppts[1]),
        VideoLinkBooking(main = mainAppts[2], pre = preAppts[1]),
        VideoLinkBooking(main = mainAppts[3], post = postAppts[1]),
      )
  }

  private fun prisonAppointment(appointmentId: Long, startTime: LocalDateTime) =
    PrisonAppointment(
      comment = null,
      agencyId = "WWI",
      bookingId = 1L,
      startTime = startTime,
      endTime = startTime.plusHours(1),
      eventId = appointmentId,
      eventLocationId = 100L,
      eventSubType = "VLB"
    )

  private fun videoLinkAppointments(hearingType: HearingType, vararg appointmentIds: Long): List<VideoLinkAppointment> =
    appointmentIds.map { appointmentId ->
      VideoLinkAppointment(
        bookingId = 1,
        appointmentId = appointmentId,
        hearingType = hearingType,
        madeByTheCourt = true,
        court = "The Court"
      )
    }
}
