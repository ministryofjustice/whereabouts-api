package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkAppointmentLinker.Companion.CHUNK_SIZE
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
  fun `Assembles one prison appointment`() {
    whenever(prisonApiService.getPrisonAppointmentsForBookingId(anyLong(), eq(0), anyInt()))
      .thenReturn(prisonAppointments(1))

    assertThat(service.getPrisonVideoLinkAppointmentsForBookingId(1L)).hasSize(1)
  }

  @Test
  fun `Filters non VLB prison appointments`() {
    whenever(prisonApiService.getPrisonAppointmentsForBookingId(anyLong(), eq(0), anyInt()))
      .thenReturn(prisonAppointments(CHUNK_SIZE))

    assertThat(service.getPrisonVideoLinkAppointmentsForBookingId(1L)).hasSize(100)
  }

  @Test
  fun `Assembles prison appointments from chunks`() {
    whenever(prisonApiService.getPrisonAppointmentsForBookingId(anyLong(), anyInt(), anyInt()))
      .thenReturn(prisonAppointments(CHUNK_SIZE))
      .thenReturn(prisonAppointments(CHUNK_SIZE))
      .thenReturn(prisonAppointments(5))
      .thenReturn(prisonAppointments(0))

    assertThat(service.getPrisonVideoLinkAppointmentsForBookingId(1L)).hasSize(CHUNK_SIZE + 5)
  }

  @Test
  fun `Builds no VideoLinkBookings if no matches in VideoLinkAppointmentRepository`() {
    whenever(prisonApiService.getPrisonAppointmentsForBookingId(anyLong(), anyInt(), anyInt()))
      .thenReturn(prisonAppointments(CHUNK_SIZE))
      .thenReturn(prisonAppointments(0))

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
  }

  @Test
  fun `Builds VideoLinkBooking with main appointment`() {
    whenever(prisonApiService.getPrisonAppointmentsForBookingId(anyLong(), anyInt(), anyInt()))
      .thenReturn(prisonAppointments(1))
      .thenReturn(prisonAppointments(0))

    whenever(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(anyLong(), eq(HearingType.PRE)))
      .thenReturn(listOf())

    whenever(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(anyLong(), eq(HearingType.POST)))
      .thenReturn(listOf())

    whenever(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(anyLong(), eq(HearingType.MAIN)))
      .thenReturn(videoLinkAppointments(HearingType.MAIN, 0))

    assertThat(service.videoLinkBookingsForOffenderBookingId(1L))
      .hasSize(1)
      .element(0).isEqualTo(VideoLinkBooking(main = videoLinkAppointments(HearingType.MAIN, 0)[0]))
  }

  @Test
  fun `Builds VideoLinkBooking with main, pre and post appointment`() {
    whenever(prisonApiService.getPrisonAppointmentsForBookingId(anyLong(), anyInt(), anyInt()))
      .thenReturn(prisonAppointments(10))
      .thenReturn(prisonAppointments(0))

    val preAppts = videoLinkAppointments(HearingType.PRE, 0)
    val mainAppts = videoLinkAppointments(HearingType.MAIN, 1)
    val postAppts = videoLinkAppointments(HearingType.POST, 2)

    whenever(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(anyLong(), eq(HearingType.PRE)))
      .thenReturn(preAppts)

    whenever(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(anyLong(), eq(HearingType.MAIN)))
      .thenReturn(mainAppts)

    whenever(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(anyLong(), eq(HearingType.POST)))
      .thenReturn(postAppts)

    /**
     * Prison appointments eventId = contiguous 0..10 1hr apponitment times
     * 1 pre VLA appointmentId = 0
     * 1 main VLA appointmentId = 1
     * 1 post VLA apppointmentId = 2
     *
     * The VLA will match prison appointments and the times will be contiguous. Therefore they will
     * be treated as the pre, main and post appointment for the same VLB:
     */

    assertThat(service.videoLinkBookingsForOffenderBookingId(1L))
      .hasSize(1)
      .element(0).isEqualTo(VideoLinkBooking(main = mainAppts[0], pre = preAppts[0], post = postAppts[0]))
  }

  @Test
  fun `Builds multiple VideoLinkBookings`() {
    // 20 Prison appointments. eventId = 0..19
    whenever(prisonApiService.getPrisonAppointmentsForBookingId(anyLong(), anyInt(), anyInt()))
      .thenReturn(prisonAppointments(20))
      .thenReturn(prisonAppointments(0))

    // 0 precedes main 1, 12 precedes main 13. Should get first and third VLB with pre appt.
    val preAppts = videoLinkAppointments(HearingType.PRE, 0, 7, 12, 21)

    // First 4 mainAppts match a prison appointment. Last 2 do not match. Should get 4 VLBs.
    val mainAppts = videoLinkAppointments(HearingType.MAIN, 1, 6, 13, 15, 22, 23)

    // 2 succeeds main 1, 16 succeeds main 15. Should get first and fourth VLB with post appt
    val postAppts = videoLinkAppointments(HearingType.POST, 2, 8, 16, 17)

    whenever(videoLinkAppointmentRepository.unlinkedPreAppointmentsForBookingId(anyLong(), eq(HearingType.PRE)))
      .thenReturn(preAppts)

    whenever(videoLinkAppointmentRepository.unlinkedMainAppointmentsForBookingId(anyLong(), eq(HearingType.MAIN)))
      .thenReturn(mainAppts)

    whenever(videoLinkAppointmentRepository.unlinkedPostAppointmentsForBookingId(anyLong(), eq(HearingType.POST)))
      .thenReturn(postAppts)

    assertThat(service.videoLinkBookingsForOffenderBookingId(1L))
      .hasSize(4)
      .containsAll(
        listOf(
          VideoLinkBooking(main = mainAppts[0], pre = preAppts[0], post = postAppts[0]),
          VideoLinkBooking(main = mainAppts[1]),
          VideoLinkBooking(main = mainAppts[2], pre = preAppts[2]),
          VideoLinkBooking(main = mainAppts[3], post = postAppts[2])
        )
      )
  }

  private fun prisonAppointments(appointmentsToCreate: Int): List<PrisonAppointment> {
    val initialTime = LocalDateTime.of(2020, 1, 1, 0, 0)
    return List(appointmentsToCreate) { index ->
      PrisonAppointment(
        agencyId = "WWI",
        bookingId = 1,
        startTime = initialTime.plusHours(1L * index),
        endTime = initialTime.plusHours(1L * index).plusHours(1),
        eventId = index.toLong(),
        eventLocationId = 100L,
        eventSubType = if (index < CHUNK_SIZE / 2) {
          "VLB"
        } else {
          "NOT_VLB"
        },
      )
    }
  }

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
