package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.BasicBookingDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.model.*
import java.time.LocalDate
import java.time.LocalDateTime

class AppointmentServiceTest {

  private val prisonApiService: PrisonApiService = mock()

  private lateinit var appointmentService: AppointmentService

  private val agencyId = "MDI"
  private val date = LocalDate.of(2021, 1, 1)
  private val timeSlot = TimePeriod.ED
  private val offenderLocationPrefix = "MDI-1"
  private val locationId = 1234L

  @BeforeEach
  fun before() {
    appointmentService = AppointmentService(
      prisonApiService
    )
  }

  @Test
  fun `when getting appointments it returns the list of appointments filtered by offender location`() {
    val filteredOffenderNo = "A1234AA"
    val filteredOffenderLocation = "$offenderLocationPrefix-1"
    val otherOffenderNo = "B2345BB"
    val otherOffenderLocation = "MDI-2-1"
    whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any(), anyOrNull(), anyOrNull()))
      .thenReturn(listOf(ScheduledAppointmentDto(
        id = 1L,
        agencyId = agencyId,
        locationId = 11L,
        appointmentTypeCode = "VLB",
        startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
        endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
        offenderNo = filteredOffenderNo
      ), ScheduledAppointmentDto(
        id = 2L,
        agencyId = agencyId,
        locationId = 12L,
        appointmentTypeCode = "VLB",
        startTime = LocalDateTime.of(2020, 1, 2, 12, 0, 0),
        endTime = LocalDateTime.of(2020, 1, 2, 13, 0, 0),
        offenderNo = otherOffenderNo
      )))
    whenever(prisonApiService.getOffenderDetailsFromOffenderNos(any()))
      .thenReturn(listOf(BasicBookingDetails(
        22L, "123", filteredOffenderNo, "A", "Name", "MDI", LocalDate.of(2000, 1, 2), 44L, filteredOffenderLocation
      ), BasicBookingDetails(
        33L, "234", otherOffenderNo, "Another", "Name", "MDI", LocalDate.of(2000, 1, 3), 55L, otherOffenderLocation
      )))

    val filteredAppointments = appointmentService.getAppointments(agencyId, date, timeSlot, offenderLocationPrefix, locationId)

    assertThat(filteredAppointments).isEqualTo(listOf(AppointmentDto(
      id = 1L,
      agencyId = agencyId,
      locationId = 11L,
      appointmentTypeCode = "VLB",
      startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
      endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
      offenderNo = filteredOffenderNo
    )))
  }

  @Test
  fun `when getting appointments it handles minimal inputs`() {
    val exampleId = 1L
    val exampleLocationId = 3L
    val exampleAppointmentType = "VLB"
    val exampleStartTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0)
    val exampleEndTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0)
    val exampleOffenderNo = "A1234AA"
    whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any(), anyOrNull(), anyOrNull()))
      .thenReturn(listOf(ScheduledAppointmentDto(
        id = exampleId,
        agencyId = agencyId,
        locationId = exampleLocationId,
        appointmentTypeCode = exampleAppointmentType,
        startTime = exampleStartTime,
        endTime = exampleEndTime,
        offenderNo = exampleOffenderNo
      )))

    val filteredAppointments = appointmentService.getAppointments(agencyId, date, null, null, null)

    assertThat(filteredAppointments).isEqualTo(listOf(AppointmentDto(
      id = exampleId,
      agencyId = agencyId,
      locationId = exampleLocationId,
      appointmentTypeCode = exampleAppointmentType,
      startTime = exampleStartTime,
      endTime = exampleEndTime,
      offenderNo = exampleOffenderNo
    )))
  }

  @Test
  fun `when getting appointments it calls prison api to get the list of appointments`() {
    whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any(), anyOrNull(), anyOrNull()))
      .thenReturn(emptyList())

    appointmentService.getAppointments(agencyId, date, timeSlot, null, locationId)

    verify(prisonApiService)
      .getScheduledAppointmentsByAgencyAndDate(eq(agencyId), eq(date), eq(timeSlot), eq(locationId))
  }

  @Test
  fun `when getting appointments it calls prison api to get the offender booking details when an offender location prefix is supplied`() {
    val offenderLocationPrefix = "WWI-1"
    val offenderNo1 = "A1234AA"
    val offenderNo2 = "B2345BB"
    whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any(), anyOrNull(), anyOrNull()))
      .thenReturn(listOf(ScheduledAppointmentDto(
        id = 1L,
        agencyId = agencyId,
        locationId = locationId,
        appointmentTypeCode = "VLB",
        startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
        endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
        offenderNo = offenderNo1
      ),ScheduledAppointmentDto(
        id = 1L,
        agencyId = agencyId,
        locationId = locationId,
        appointmentTypeCode = "VLB",
        startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
        endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
        offenderNo = offenderNo2
      )))

    appointmentService.getAppointments(agencyId, date, timeSlot, offenderLocationPrefix, locationId)

    verify(prisonApiService)
      .getOffenderDetailsFromOffenderNos(eq(setOf(
        offenderNo1, offenderNo2
      )))
  }
}
