package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateCourtAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.CourtAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.repository.CourtAppointmentRepository

class CourtServiceTest {

  private val elite2ApiService: Elite2ApiService = mock()
  private val courtAppointmentRepository: CourtAppointmentRepository = mock()

  @Test
  fun `should push an appointment to elite2`() {
    val service = CourtService(elite2ApiService, courtAppointmentRepository)
    val bookingId: Long = 1

    service.addCourtAppointment(CreateCourtAppointment(
        bookingId = bookingId,
        locationId = 1,
        comment = "test",
        startTime = "2019-10-10T10:00:00",
        endTime = "2019-10-10T11:00:00",
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
  fun `should create court appointment using the event id returned from elite2`() {
    val service = CourtService(elite2ApiService, courtAppointmentRepository)
    val bookingId: Long = 1

    whenever(elite2ApiService.postAppointment(anyLong(), any())).thenReturn(1L)

    service.addCourtAppointment(CreateCourtAppointment(
        bookingId = bookingId,
        locationId = 1,
        comment = "test",
        startTime = "2019-10-10T10:00:00",
        endTime = "2019-10-10T11:00:00",
        court = "York Crown Court"
    ))

    verify(courtAppointmentRepository).save(
        CourtAppointment(
            appointmentId = 1,
            court = "York Crown Court",
            bookingId = bookingId,
            hearingType = HearingType.MAIN
        ))
  }

  @Test
  fun `should return NO court appointments`() {
    val service = CourtService(elite2ApiService, courtAppointmentRepository)
    val appointments = service.getCourtAppointments(setOf(1, 2))

    verify(courtAppointmentRepository).findCourtAppointmentByAppointmentIdIn(setOf(1, 2))
    assertThat(appointments).isEmpty()
  }

  @Test
  fun `should return and map court appointments`() {
    whenever(courtAppointmentRepository.findCourtAppointmentByAppointmentIdIn(setOf(3, 4))).thenReturn(
        setOf(
            CourtAppointment(id = 1, bookingId = 2, appointmentId = 3, hearingType = HearingType.MAIN, court = "YORK"),
            CourtAppointment(id = 2, bookingId = 3, appointmentId = 4, hearingType = HearingType.PRE, court = "YORK"
            ))
    )
    val service = CourtService(elite2ApiService, courtAppointmentRepository)
    val appointments = service.getCourtAppointments(setOf(3, 4))

    assertThat(appointments)
        .extracting("id", "bookingId", "appointmentId", "hearingType", "court")
        .containsExactlyInAnyOrder(
            Tuple.tuple(1L, 2L, 3L, HearingType.MAIN, "YORK"),
            Tuple.tuple(2L, 3L, 4L, HearingType.PRE, "YORK")
        )
  }
}
