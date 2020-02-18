package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.groups.Tuple
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateVideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import java.time.LocalDateTime

class CourtServiceTest {

  private val elite2ApiService: Elite2ApiService = mock()
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository = mock()

  @Test
  fun `should push an appointment to elite2`() {
    val service = CourtService(elite2ApiService, videoLinkAppointmentRepository, "York Crown Court")
    val bookingId: Long = 1

    service.createVideoLinkAppointment(CreateVideoLinkAppointment(
        bookingId = bookingId,
        locationId = 1,
        comment = "test",
        startTime = LocalDateTime.of(2019,10,10,10,0),
        endTime =  LocalDateTime.of(2019,10,10,11,0),
        court = "York Crown Court"
    ))

    verify(elite2ApiService).postAppointment(bookingId, CreateBookingAppointment(
        appointmentType = "VLB",
        locationId = 1,
        comment = "test",
        startTime = "2019-10-10T10:00:00",
        endTime = "2019-10-10T11:00:00"
    ))
  }

  @Test
  fun `should create video link appointment using the event id returned from elite2`() {
    val service = CourtService(elite2ApiService, videoLinkAppointmentRepository, "York Crown Court")
    val bookingId: Long = 1

    whenever(elite2ApiService.postAppointment(anyLong(), any())).thenReturn(1L)

    service.createVideoLinkAppointment(CreateVideoLinkAppointment(
        bookingId = bookingId,
        locationId = 1,
        comment = "test",
        startTime = LocalDateTime.of(2019,10,10,10,0),
        endTime =  LocalDateTime.of(2019,10,10,11,0),
        court = "York Crown Court"
    ))

    verify(videoLinkAppointmentRepository).save(
        VideoLinkAppointment(
            appointmentId = 1,
            court = "York Crown Court",
            bookingId = bookingId,
            hearingType = HearingType.MAIN
        ))
  }

  @Test
  fun `should return NO video link appointments`() {
    val service = CourtService(elite2ApiService, videoLinkAppointmentRepository, "")
    val appointments = service.getVideoLinkAppointments(setOf(1, 2))

    verify(videoLinkAppointmentRepository).findVideoLinkAppointmentByAppointmentIdIn(setOf(1, 2))
    assertThat(appointments).isEmpty()
  }

  @Test
  fun `should return and map video link appointments`() {
    whenever(videoLinkAppointmentRepository.findVideoLinkAppointmentByAppointmentIdIn(setOf(3, 4))).thenReturn(
        setOf(
            VideoLinkAppointment(id = 1, bookingId = 2, appointmentId = 3, hearingType = HearingType.MAIN, court = "YORK"),
            VideoLinkAppointment(id = 2, bookingId = 3, appointmentId = 4, hearingType = HearingType.PRE, court = "YORK"
            ))
    )
    val service = CourtService(elite2ApiService, videoLinkAppointmentRepository, "")
    val appointments = service.getVideoLinkAppointments(setOf(3, 4))

    assertThat(appointments)
        .extracting("id", "bookingId", "appointmentId", "hearingType", "court")
        .containsExactlyInAnyOrder(
            Tuple.tuple(1L, 2L, 3L, HearingType.MAIN, "YORK"),
            Tuple.tuple(2L, 3L, 4L, HearingType.PRE, "YORK")
        )
  }

  @Test
  fun `should validate the court location`() {
    val service = CourtService(elite2ApiService, videoLinkAppointmentRepository, "York, London")

    assertThatThrownBy {
      service.createVideoLinkAppointment(CreateVideoLinkAppointment(
          bookingId = 1,
          locationId = 1,
          court = "Mars",
          hearingType = HearingType.PRE,
          startTime = LocalDateTime.of(2019,10,10,10,0),
          endTime =  LocalDateTime.of(2019,10,10,11,0)
      ))
    }.isInstanceOf(InvalidCourtLocation::class.java)
        .hasMessageContaining("Invalid court location")

  }
}
