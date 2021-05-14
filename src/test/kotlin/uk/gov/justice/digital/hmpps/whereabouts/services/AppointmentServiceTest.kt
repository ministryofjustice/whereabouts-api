package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderBooking
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.utils.makeAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.utils.makeVideoLinkBooking
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException

class AppointmentServiceTest {

  private val prisonApiService: PrisonApiService = mock()
  private val videoLinkBookingRepository: VideoLinkBookingRepository = mock()

  private lateinit var appointmentService: AppointmentService

  @BeforeEach
  fun before() {
    appointmentService = AppointmentService(
      prisonApiService,
      videoLinkBookingRepository
    )
  }

  @Nested
  inner class Appointments {

    @Test
    fun `when getting appointments it returns the list of appointments filtered by offender location`() {
      val filteredOffenderNo = "A1234AA"
      val filteredOffenderLocation = "$OFFENDER_LOCATION_PREFIX-1"
      val otherOffenderNo = "B2345BB"
      val otherOffenderLocation = "MDI-2-1"

      whenever(prisonApiService.getScheduledAppointments(anyString(), any(), anyOrNull(), anyOrNull()))
        .thenReturn(
          listOf(
            ScheduledAppointmentDto(
              id = 1L,
              agencyId = AGENCY_ID,
              locationId = 11L,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
              endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
              offenderNo = filteredOffenderNo
            ),
            ScheduledAppointmentDto(
              id = 2L,
              agencyId = AGENCY_ID,
              locationId = 12L,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 2, 12, 0, 0),
              endTime = LocalDateTime.of(2020, 1, 2, 13, 0, 0),
              offenderNo = otherOffenderNo
            )
          )
        )
      whenever(prisonApiService.getOffenderDetailsFromOffenderNos(any()))
        .thenReturn(
          listOf(
            OffenderBooking(
              22L,
              "123",
              filteredOffenderNo,
              "A",
              "Name",
              "MDI",
              LocalDate.of(2000, 1, 2),
              44L,
              filteredOffenderLocation
            ),
            OffenderBooking(
              33L,
              "234",
              otherOffenderNo,
              "Another",
              "Name",
              "MDI",
              LocalDate.of(2000, 1, 3),
              55L,
              otherOffenderLocation
            )
          )
        )

      val filteredAppointments =
        appointmentService.getAppointments(AGENCY_ID, DATE, TIME_SLOT, OFFENDER_LOCATION_PREFIX, LOCATION_ID)

      assertThat(filteredAppointments).isEqualTo(
        listOf(
          AppointmentDto(
            id = 1L,
            agencyId = AGENCY_ID,
            locationId = 11L,
            appointmentTypeCode = "VLB",
            startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
            offenderNo = filteredOffenderNo
          )
        )
      )
    }

    @Test
    fun `when getting appointments it handles minimal inputs`() {
      val exampleId = 1L
      val exampleLocationId = 3L
      val exampleAppointmentType = "VLB"
      val exampleStartTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0)
      val exampleEndTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0)
      val exampleOffenderNo = "A1234AA"
      whenever(prisonApiService.getScheduledAppointments(anyString(), any(), anyOrNull(), anyOrNull()))
        .thenReturn(
          listOf(
            ScheduledAppointmentDto(
              id = exampleId,
              agencyId = AGENCY_ID,
              locationId = exampleLocationId,
              appointmentTypeCode = exampleAppointmentType,
              startTime = exampleStartTime,
              endTime = exampleEndTime,
              offenderNo = exampleOffenderNo
            )
          )
        )

      val filteredAppointments = appointmentService.getAppointments(AGENCY_ID, DATE, null, null, null)

      assertThat(filteredAppointments).isEqualTo(
        listOf(
          AppointmentDto(
            id = exampleId,
            agencyId = AGENCY_ID,
            locationId = exampleLocationId,
            appointmentTypeCode = exampleAppointmentType,
            startTime = exampleStartTime,
            endTime = exampleEndTime,
            offenderNo = exampleOffenderNo
          )
        )
      )
    }

    @Test
    fun `when getting appointments it calls prison api to get the list of appointments`() {
      whenever(prisonApiService.getScheduledAppointments(anyString(), any(), anyOrNull(), anyOrNull()))
        .thenReturn(emptyList())

      appointmentService.getAppointments(AGENCY_ID, DATE, TIME_SLOT, null, LOCATION_ID)

      verify(prisonApiService)
        .getScheduledAppointments(eq(AGENCY_ID), eq(DATE), eq(TIME_SLOT), eq(LOCATION_ID))
    }

    @Test
    fun `when getting appointments it calls prison api to get the offender booking details when an offender location prefix is supplied`() {
      val offenderLocationPrefix = "WWI-1"
      val offenderNo1 = "A1234AA"
      val offenderNo2 = "B2345BB"
      whenever(prisonApiService.getScheduledAppointments(anyString(), any(), anyOrNull(), anyOrNull()))
        .thenReturn(
          listOf(
            ScheduledAppointmentDto(
              id = 1L,
              agencyId = AGENCY_ID,
              locationId = LOCATION_ID,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
              endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
              offenderNo = offenderNo1
            ),
            ScheduledAppointmentDto(
              id = 1L,
              agencyId = AGENCY_ID,
              locationId = LOCATION_ID,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
              endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
              offenderNo = offenderNo2
            )
          )
        )

      appointmentService.getAppointments(AGENCY_ID, DATE, TIME_SLOT, offenderLocationPrefix, LOCATION_ID)

      verify(prisonApiService)
        .getOffenderDetailsFromOffenderNos(
          eq(
            setOf(
              offenderNo1, offenderNo2
            )
          )
        )
    }
  }

  @Nested
  inner class AppointmentDetails {

    @BeforeEach
    fun beforeEach() {
      whenever(prisonApiService.getPrisonAppointment(anyLong())).thenReturn(
        makeAppointmentDto(bookingId = BOOKING_ID, startTime = START_TIME, endTime = END_TIME)
      )
      whenever(prisonApiService.getOffenderNoFromBookingId(any())).thenReturn(OFFENDER_NO)
    }

    @Test
    fun `make a call to the prison api to request core appointment details`() {
      appointmentService.getAppointment(MAIN_APPOINTMENT_ID)

      verify(prisonApiService).getPrisonAppointment(MAIN_APPOINTMENT_ID)
    }

    @Test
    fun `check to see if the appointment is a video link booking`() {
      appointmentService.getAppointment(MAIN_APPOINTMENT_ID)

      verify(videoLinkBookingRepository).findByMainAppointmentIds(listOf(MAIN_APPOINTMENT_ID))
    }

    @Test
    fun `make a request to get the nomis prison number`() {
      appointmentService.getAppointment(MAIN_APPOINTMENT_ID)

      verify(prisonApiService).getOffenderNoFromBookingId(BOOKING_ID)
    }

    @Test
    fun `throws entity not found exception`() {
      whenever(prisonApiService.getPrisonAppointment(anyLong())).thenThrow(EntityNotFoundException::class.java)

      assertThrows(EntityNotFoundException::class.java) {
        appointmentService.getAppointment(MAIN_APPOINTMENT_ID)
      }
    }

    @Test
    fun `transform into appointment details attaching video link booking`() {
      whenever(videoLinkBookingRepository.findByMainAppointmentIds(any())).thenReturn(
        listOf(
          makeVideoLinkBooking(1L)
        )
      )

      val appointmentDetails = appointmentService.getAppointment(MAIN_APPOINTMENT_ID)

      assertThat(appointmentDetails.appointment)
        .extracting("id", "agencyId", "locationId", "appointmentTypeCode", "offenderNo", "startTime", "endTime")
        .contains(MAIN_APPOINTMENT_ID, AGENCY_ID, EVENT_LOCATION_ID, "INTERV", OFFENDER_NO, START_TIME, END_TIME)

      assertThat(appointmentDetails.videoLinkBooking?.id).isEqualTo(1L)

      assertThat(appointmentDetails.videoLinkBooking?.main)
        .extracting("id", "bookingId", "appointmentId", "court", "hearingType", "createdByUsername", "madeByTheCourt")
        .contains(1L, BOOKING_ID, MAIN_APPOINTMENT_ID, "Court 1", HearingType.MAIN, "SA", true)

      assertThat(appointmentDetails.videoLinkBooking?.pre)
        .extracting("id", "bookingId", "appointmentId", "court", "hearingType", "createdByUsername", "madeByTheCourt")
        .contains(2L, BOOKING_ID, 2L, "Court 1", HearingType.PRE, "SA", true)

      assertThat(appointmentDetails.videoLinkBooking?.post)
        .extracting("id", "bookingId", "appointmentId", "court", "hearingType", "createdByUsername", "madeByTheCourt")
        .contains(3L, BOOKING_ID, 3L, "Court 1", HearingType.POST, "SA", true)
    }
  }

  companion object {
    private val DATE = LocalDate.of(2021, 1, 1)
    private val TIME_SLOT = TimePeriod.ED
    private const val OFFENDER_LOCATION_PREFIX = "MDI-1"
    private const val LOCATION_ID = 1234L

    private val START_TIME = LocalDateTime.now()
    private val END_TIME = LocalDateTime.now()
    private const val MAIN_APPOINTMENT_ID = 1L
    private const val EVENT_LOCATION_ID = 1L
    private const val OFFENDER_NO = "A12345"
    private const val AGENCY_ID = "MDI"
    private const val BOOKING_ID = -1L
  }
}