package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.isA
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentSearchDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreatedAppointmentDetailsDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderBooking
import uk.gov.justice.digital.hmpps.whereabouts.dto.Repeat
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentSearchDto
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.RecurringAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RelatedAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.RepeatPeriod
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.CourtRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.RecurringAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApi.EventPropagation
import uk.gov.justice.digital.hmpps.whereabouts.services.court.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class AppointmentServiceTest {
  private val courtRepository: CourtRepository = mock()
  private val prisonApiService: PrisonApiService = mock()
  private val prisonApiServiceAuditable: PrisonApiServiceAuditable = mock()
  private val videoLinkBookingRepository: VideoLinkBookingRepository = mock()
  private val recurringAppointmentRepository: RecurringAppointmentRepository = mock()
  private val videoLinkBookingService: VideoLinkBookingService = mock()
  private val telemetryClient: TelemetryClient = mock()

  private lateinit var appointmentService: AppointmentService

  @BeforeEach
  fun before() {
    appointmentService = AppointmentService(
      CourtService(courtRepository),
      prisonApiService,
      prisonApiServiceAuditable,
      videoLinkBookingRepository,
      recurringAppointmentRepository,
      videoLinkBookingService,
      telemetryClient
    )

    whenever(courtRepository.findAll(isA<Sort>())).thenReturn(listOf())
  }

  @Nested
  inner class Appointments {

    @Test
    fun `when getting appointments it returns the list of appointments filtered by offender location`() {
      val filteredOffenderNo = "A1234AA"
      val filteredOffenderLocation = "$OFFENDER_LOCATION_PREFIX-1"
      val otherOffenderNo = "B2345BB"
      val otherOffenderLocation = "MDI-2-1"

      whenever(prisonApiService.getScheduledAppointments(anyString(), any(), anyOrNull(), anyOrNull())).thenReturn(
        listOf(
          ScheduledAppointmentSearchDto(
            id = 1L,
            agencyId = AGENCY_ID,
            locationId = 11L,
            locationDescription = "A location",
            appointmentTypeCode = "VLB",
            appointmentTypeDescription = "Video Link Booking",
            startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
            offenderNo = filteredOffenderNo,
            firstName = "BILL",
            lastName = "BENN",
            createUserId = "ASMITH"
          ),
          ScheduledAppointmentSearchDto(
            id = 2L,
            agencyId = AGENCY_ID,
            locationId = 12L,
            locationDescription = "Another location",
            appointmentTypeCode = "VLB",
            appointmentTypeDescription = "Video Link Booking",
            startTime = LocalDateTime.of(2020, 1, 2, 12, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 2, 13, 0, 0),
            offenderNo = otherOffenderNo,
            firstName = "ANY",
            lastName = "NAME",
            createUserId = "BSMITH"
          )
        )
      )
      whenever(prisonApiService.getOffenderDetailsFromOffenderNos(any())).thenReturn(
        listOf(
          OffenderBooking(
            22L, "123", filteredOffenderNo, "A", "Name", "MDI", LocalDate.of(2000, 1, 2), 44L, filteredOffenderLocation
          ),
          OffenderBooking(
            33L, "234", otherOffenderNo, "Another", "Name", "MDI", LocalDate.of(2000, 1, 3), 55L, otherOffenderLocation
          )
        )
      )

      val filteredAppointments =
        appointmentService.getAppointments(AGENCY_ID, DATE, TIME_SLOT, OFFENDER_LOCATION_PREFIX, LOCATION_ID)

      assertThat(filteredAppointments).isEqualTo(
        listOf(
          AppointmentSearchDto(
            id = 1L,
            agencyId = AGENCY_ID,
            locationId = 11L,
            locationDescription = "A location",
            appointmentTypeCode = "VLB",
            appointmentTypeDescription = "Video Link Booking",
            startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
            offenderNo = filteredOffenderNo,
            firstName = "BILL",
            lastName = "BENN",
            createUserId = "ASMITH"
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
      val exampleLocationDescription = "A location"
      val exampleAppointmentTypeDescription = "Video Link Booking"
      val exampleFirstName = "BILL"
      val exampleLastName = "BENN"
      val exampleCreateUserId = "ASMITH"

      whenever(prisonApiService.getScheduledAppointments(anyString(), any(), anyOrNull(), anyOrNull())).thenReturn(
        listOf(
          ScheduledAppointmentSearchDto(
            id = exampleId,
            agencyId = AGENCY_ID,
            locationId = exampleLocationId,
            locationDescription = exampleLocationDescription,
            appointmentTypeCode = exampleAppointmentType,
            appointmentTypeDescription = exampleAppointmentTypeDescription,
            startTime = exampleStartTime,
            endTime = exampleEndTime,
            offenderNo = exampleOffenderNo,
            firstName = exampleFirstName,
            lastName = exampleLastName,
            createUserId = exampleCreateUserId
          )
        )
      )

      val filteredAppointments = appointmentService.getAppointments(AGENCY_ID, DATE, null, null, null)

      assertThat(filteredAppointments).isEqualTo(
        listOf(
          AppointmentSearchDto(
            id = exampleId,
            agencyId = AGENCY_ID,
            locationId = exampleLocationId,
            locationDescription = exampleLocationDescription,
            appointmentTypeCode = exampleAppointmentType,
            appointmentTypeDescription = exampleAppointmentTypeDescription,
            startTime = exampleStartTime,
            endTime = exampleEndTime,
            offenderNo = exampleOffenderNo,
            firstName = exampleFirstName,
            lastName = exampleLastName,
            createUserId = exampleCreateUserId
          )
        )
      )
    }

    @Test
    fun `when getting appointments it calls prison api to get the list of appointments`() {
      whenever(prisonApiService.getScheduledAppointments(anyString(), any(), anyOrNull(), anyOrNull())).thenReturn(
        emptyList()
      )

      appointmentService.getAppointments(AGENCY_ID, DATE, TIME_SLOT, null, LOCATION_ID)

      verify(prisonApiService).getScheduledAppointments(eq(AGENCY_ID), eq(DATE), eq(TIME_SLOT), eq(LOCATION_ID))
    }

    @Test
    fun `when getting appointments it calls prison api to get the offender booking details when an offender location prefix is supplied`() {
      val offenderLocationPrefix = "WWI-1"
      val offenderNo1 = "A1234AA"
      val offenderNo2 = "B2345BB"
      whenever(prisonApiService.getScheduledAppointments(anyString(), any(), anyOrNull(), anyOrNull())).thenReturn(
        listOf(
          ScheduledAppointmentSearchDto(
            id = 1L,
            agencyId = AGENCY_ID,
            locationId = LOCATION_ID,
            locationDescription = "A location",
            appointmentTypeCode = "VLB",
            appointmentTypeDescription = "Video Link Booking",
            startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
            offenderNo = offenderNo1,
            firstName = "BILL",
            lastName = "BENN",
            createUserId = "ASMITH"
          ),
          ScheduledAppointmentSearchDto(
            id = 1L,
            agencyId = AGENCY_ID,
            locationId = LOCATION_ID,
            locationDescription = "Another location",
            appointmentTypeCode = "VLB",
            appointmentTypeDescription = "Video Link Booking Again",
            startTime = LocalDateTime.of(2020, 1, 1, 12, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 13, 0, 0),
            offenderNo = offenderNo2,
            firstName = "BOB",
            lastName = "BABB",
            createUserId = "BSMITH"
          )
        )
      )

      appointmentService.getAppointments(AGENCY_ID, DATE, TIME_SLOT, offenderLocationPrefix, LOCATION_ID)

      verify(prisonApiService).getOffenderDetailsFromOffenderNos(
        eq(
          setOf(
            offenderNo1,
            offenderNo2
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
        DataHelpers.makePrisonAppointment(bookingId = BOOKING_ID, startTime = START_TIME, endTime = END_TIME)
      )
      whenever(prisonApiService.getOffenderNoFromBookingId(any())).thenReturn(OFFENDER_NO)
    }

    @Test
    fun `make a call to the prison api to request core appointment details`() {
      appointmentService.getAppointment(1)

      verify(prisonApiService).getPrisonAppointment(1)
    }

    @Test
    fun `check to see if the appointment is a video link booking`() {
      appointmentService.getAppointment(1)

      verify(videoLinkBookingRepository).findByAppointmentIdsAndHearingType(listOf(1), HearingType.MAIN)
    }

    @Test
    fun `check to see if the appointment is a recurring one`() {
      appointmentService.getAppointment(1)

      verify(recurringAppointmentRepository).findRecurringAppointmentByRelatedAppointmentsContains(RelatedAppointment(1))
    }

    @Test
    fun `make a request to get the nomis prison number`() {
      appointmentService.getAppointment(1)

      verify(prisonApiService).getOffenderNoFromBookingId(BOOKING_ID)
    }

    @Test
    fun `throws entity not found exception`() {
      whenever(prisonApiService.getPrisonAppointment(anyLong())).thenThrow(EntityNotFoundException::class.java)

      assertThrows(EntityNotFoundException::class.java) {
        appointmentService.getAppointment(1)
      }
    }

    @Test
    fun `transform into appointment details`() {
      val appointmentDetails = appointmentService.getAppointment(1)

      assertThat(appointmentDetails.appointment).extracting(
        "id",
        "agencyId",
        "locationId",
        "appointmentTypeCode",
        "offenderNo",
        "startTime",
        "endTime",
        "comment"
      ).contains(
        1L,
        AGENCY_ID,
        EVENT_LOCATION_ID,
        "INTERV",
        OFFENDER_NO,
        START_TIME,
        END_TIME,
        "test"
      )
    }

    @Test
    fun `transform into video link booking`() {
      whenever(
        videoLinkBookingRepository.findByAppointmentIdsAndHearingType(
          any(),
          eq(HearingType.MAIN),
          isNull(),
          isNull()
        )
      ).thenReturn(
        listOf(DataHelpers.makeVideoLinkBooking(1L, offenderBookingId = BOOKING_ID))
      )

      val appointmentDetails = appointmentService.getAppointment(1)

      assertThat(appointmentDetails.appointment).extracting(
        "id",
        "agencyId",
        "locationId",
        "appointmentTypeCode",
        "offenderNo",
        "startTime",
        "endTime",
        "createUserId"
      ).contains(1L, AGENCY_ID, EVENT_LOCATION_ID, "INTERV", OFFENDER_NO, START_TIME, END_TIME, "SA")

      assertThat(appointmentDetails.videoLinkBooking?.id).isEqualTo(1L)

      assertThat(appointmentDetails.videoLinkBooking?.main).extracting(
        "id",
        "bookingId",
        "appointmentId",
        "court",
        "hearingType",
        "createdByUsername",
        "madeByTheCourt"
      ).contains(1L, BOOKING_ID, 1L, "Court name", HearingType.MAIN, "SA", true)

      assertThat(appointmentDetails.videoLinkBooking?.pre).extracting(
        "id",
        "bookingId",
        "appointmentId",
        "court",
        "hearingType",
        "createdByUsername",
        "madeByTheCourt"
      ).contains(2L, BOOKING_ID, 2L, "Court name", HearingType.PRE, "SA", true)

      assertThat(appointmentDetails.videoLinkBooking?.post).extracting(
        "id",
        "bookingId",
        "appointmentId",
        "court",
        "hearingType",
        "createdByUsername",
        "madeByTheCourt"
      ).contains(3L, BOOKING_ID, 3L, "Court name", HearingType.POST, "SA", true)
    }

    @Test
    fun `should transform into recurring appointment`() {
      whenever(recurringAppointmentRepository.findRecurringAppointmentByRelatedAppointmentsContains(any())).thenReturn(
        Optional.of(
          RecurringAppointment(
            id = 1,
            repeatPeriod = RepeatPeriod.FORTNIGHTLY,
            count = 1,
            startTime = START_TIME
          )
        )
      )

      val appointmentDetails = appointmentService.getAppointment(1)

      assertThat(appointmentDetails.recurring).extracting("id", "repeatPeriod", "count", "startTime")
        .contains(1L, RepeatPeriod.FORTNIGHTLY, 1L, START_TIME)
    }

    @Test
    fun `should make a request to get the appointment details relating to the video link bookings`() {
      reset(prisonApiService)

      whenever(prisonApiService.getOffenderNoFromBookingId(any())).thenReturn(OFFENDER_NO)

      whenever(prisonApiService.getPrisonAppointment(4L)).thenReturn(
        DataHelpers.makePrisonAppointment(
          eventId = 4L,
          startTime = LocalDateTime.parse("2020-10-12T20:00"),
          endTime = LocalDateTime.parse("2020-10-12T21:00")
        )
      )

      whenever(prisonApiService.getPrisonAppointment(2L)).thenReturn(
        DataHelpers.makePrisonAppointment(
          eventId = 2L,
          startTime = LocalDateTime.parse("2020-10-13T20:00"),
          endTime = LocalDateTime.parse("2020-10-13T21:00")
        )
      )

      whenever(prisonApiService.getPrisonAppointment(3L)).thenReturn(
        DataHelpers.makePrisonAppointment(
          eventId = 3L,
          startTime = LocalDateTime.parse("2020-10-14T20:00"),
          endTime = LocalDateTime.parse("2020-10-14T21:00")
        )
      )

      whenever(
        videoLinkBookingRepository.findByAppointmentIdsAndHearingType(
          any(),
          eq(HearingType.MAIN),
          isNull(),
          isNull()
        )
      ).thenReturn(
        listOf(DataHelpers.makeVideoLinkBooking(4L))
      )

      val appointmentDetails = appointmentService.getAppointment(4L)

      verify(prisonApiService).getPrisonAppointment(2L)
      verify(prisonApiService).getPrisonAppointment(3L)
      verify(prisonApiService).getPrisonAppointment(4L)

      assertThat(appointmentDetails.videoLinkBooking?.main).extracting("startTime", "endTime", "locationId")
        .contains(LocalDateTime.parse("2020-10-12T20:00"), LocalDateTime.parse("2020-10-12T21:00"), 1L)

      assertThat(appointmentDetails.videoLinkBooking?.pre).extracting("startTime", "endTime", "locationId")
        .contains(LocalDateTime.parse("2020-10-13T20:00"), LocalDateTime.parse("2020-10-13T21:00"), 1L)

      assertThat(appointmentDetails.videoLinkBooking?.post).extracting("startTime", "endTime", "locationId")
        .contains(LocalDateTime.parse("2020-10-14T20:00"), LocalDateTime.parse("2020-10-14T21:00"), 1L)
    }
  }

  @Nested
  inner class CreateAnAppointment {

    @BeforeEach
    fun beforeEach() {
      val createAppointmentDetails = CreatedAppointmentDetailsDto(
        appointmentEventId = 0,
        bookingId = 1,
        locationId = 2,
        startTime = START_TIME,
        endTime = END_TIME,
        appointmentType = "INST"
      )
      whenever(prisonApiServiceAuditable.createAppointments(any())).thenReturn(
        listOf(
          createAppointmentDetails.copy(appointmentEventId = 1),
          createAppointmentDetails.copy(appointmentEventId = 2),
          createAppointmentDetails.copy(appointmentEventId = 3),
          createAppointmentDetails.copy(appointmentEventId = 4)
        )
      )
    }

    @Test
    fun `calls prison API to create a new appointment`() {
      appointmentService.createAppointment(
        DataHelpers.makeCreateAppointmentSpecification(
          bookingId = BOOKING_ID,
          startTime = START_TIME,
          endTime = END_TIME
        )
      )

      verify(prisonApiServiceAuditable).createAppointments(
        DataHelpers.makeCreatePrisonAppointment(
          bookingId = BOOKING_ID,
          startTime = START_TIME,
          endTime = END_TIME
        )
      )
    }

    @Test
    fun `calls prison API to create a set of repeatable appointments`() {
      appointmentService.createAppointment(
        DataHelpers.makeCreateAppointmentSpecification(
          bookingId = BOOKING_ID,
          startTime = START_TIME,
          endTime = END_TIME,
          repeat = Repeat(RepeatPeriod.DAILY, 1)
        )
      )

      verify(prisonApiServiceAuditable).createAppointments(
        DataHelpers.makeCreatePrisonAppointment(
          bookingId = BOOKING_ID,
          startTime = START_TIME,
          endTime = END_TIME,
          repeat = Repeat(RepeatPeriod.DAILY, 1)
        )
      )
    }

    @Test
    fun `should save the recurring appointment data`() {
      appointmentService.createAppointment(
        DataHelpers.makeCreateAppointmentSpecification(
          bookingId = BOOKING_ID,
          startTime = START_TIME,
          endTime = END_TIME,
          repeat = Repeat(RepeatPeriod.DAILY, 4)
        )
      )

      verify(recurringAppointmentRepository).save(
        RecurringAppointment(
          repeatPeriod = RepeatPeriod.DAILY,
          count = 4,
          startTime = START_TIME,
          relatedAppointments = mutableListOf(
            RelatedAppointment(1),
            RelatedAppointment(2),
            RelatedAppointment(3),
            RelatedAppointment(4)
          )
        )
      )
    }

    @Test
    fun `should fire an event when a recurring appointment has been created`() {
      appointmentService.createAppointment(
        DataHelpers.makeCreateAppointmentSpecification(
          bookingId = BOOKING_ID,
          startTime = START_TIME,
          endTime = END_TIME,
          repeat = Repeat(RepeatPeriod.DAILY, 1)
        )
      )

      verify(telemetryClient).trackEvent(
        "Recurring Appointment created for a prisoner",
        mapOf(
          "appointmentType" to "ABC",
          "repeatPeriod" to "DAILY",
          "count" to "1",
          "bookingId" to BOOKING_ID.toString(),
          "locationId" to "1"
        ),
        null
      )
    }

    @Test
    fun `should return the details of the created appointments`() {
      val created = appointmentService.createAppointment(DataHelpers.makeCreateAppointmentSpecification())

      assertThat(created).extracting(
        "bookingId",
        "locationId",
        "startTime",
        "endTime",
        "appointmentType",
        "appointmentEventId"
      ).contains(
        Tuple.tuple(1L, 2L, START_TIME, END_TIME, "INST", 1L),
        Tuple.tuple(1L, 2L, START_TIME, END_TIME, "INST", 2L),
        Tuple.tuple(1L, 2L, START_TIME, END_TIME, "INST", 3L),
        Tuple.tuple(1L, 2L, START_TIME, END_TIME, "INST", 4L)
      )
    }
  }

  @Nested
  inner class DeleteAppointment {

    @BeforeEach
    fun beforeEach() {
      whenever(prisonApiService.getPrisonAppointment(anyLong())).thenReturn(DataHelpers.makePrisonAppointment())
    }

    @Test
    fun `should check to see if the appointment exists in nomis`() {
      whenever(prisonApiService.getPrisonAppointment(2)).thenReturn(null)

      assertThrows(EntityNotFoundException::class.java) {
        appointmentService.deleteAppointment(2)
      }
    }

    @Test
    fun `should delete an appointment`() {
      appointmentService.deleteAppointment(1L)
      verify(prisonApiService).deleteAppointment(1L, EventPropagation.ALLOW)
    }

    @Test
    fun `should delete video link booking`() {
      whenever(
        videoLinkBookingRepository.findByAppointmentIdsAndHearingType(
          any(),
          eq(HearingType.MAIN),
          isNull(),
          isNull()
        )
      ).thenReturn(
        listOf(DataHelpers.makeVideoLinkBooking(2L))
      )

      appointmentService.deleteAppointment(1L)

      verify(videoLinkBookingService).deleteVideoLinkBooking(2L)
      verify(prisonApiService, never()).deleteAppointment(anyLong(), any())
      verify(recurringAppointmentRepository, never()).deleteById(anyLong())
    }

    @Test
    fun `should delete the single appointment of a sequence of recurring appointments when requested`() {
      val relatedAppointments = mutableListOf(RelatedAppointment(2L), RelatedAppointment(3L))
      whenever(prisonApiService.getPrisonAppointment(3L)).thenReturn(DataHelpers.makePrisonAppointment())
      whenever(recurringAppointmentRepository.findRecurringAppointmentByRelatedAppointmentsContains(any())).thenReturn(
        Optional.of(
          RecurringAppointment(
            id = 100,
            repeatPeriod = RepeatPeriod.DAILY,
            count = 2,
            startTime = START_TIME,
            relatedAppointments = relatedAppointments
          )
        )
      )

      appointmentService.deleteAppointment(3L)

      verify(prisonApiService).deleteAppointment(3L, EventPropagation.ALLOW)
      // JPA will remove the item from the DB when removed from the list as orphanRemoval = true
      assertThat(relatedAppointments).hasSize(1)
      assertThat(relatedAppointments[0].id).isEqualTo(2L)
    }

    @Test
    fun `should delete all recurring appointments in a sequence when the only remaining appointment is deleted`() {
      val relatedAppointments = mutableListOf(RelatedAppointment(3L))
      whenever(prisonApiService.getPrisonAppointment(3L)).thenReturn(DataHelpers.makePrisonAppointment())
      whenever(recurringAppointmentRepository.findRecurringAppointmentByRelatedAppointmentsContains(any())).thenReturn(
        Optional.of(
          RecurringAppointment(
            id = 100,
            repeatPeriod = RepeatPeriod.DAILY,
            count = 2,
            startTime = START_TIME,
            relatedAppointments = relatedAppointments
          )
        )
      )

      appointmentService.deleteAppointment(3L)

      verify(prisonApiService).deleteAppointment(3L, EventPropagation.ALLOW)
      verify(recurringAppointmentRepository).deleteById(100)
    }
  }

  @Nested
  inner class DeleteRecurringAppointmentSequence {

    @BeforeEach
    fun beforeEach() {
      whenever(recurringAppointmentRepository.findById(anyLong())).thenReturn(
        Optional.of(
          RecurringAppointment(
            id = 100,
            repeatPeriod = RepeatPeriod.DAILY,
            count = 2,
            startTime = START_TIME,
            relatedAppointments = mutableListOf(RelatedAppointment(2L), RelatedAppointment(3L))
          )
        )
      )
    }

    @Test
    fun `should delete all recurring appointments`() {
      appointmentService.deleteRecurringAppointmentSequence(100)

      verify(prisonApiService).deleteAppointments(listOf(2L, 3L), EventPropagation.ALLOW)
      verify(recurringAppointmentRepository).deleteById(100)
      verify(videoLinkBookingService, never()).deleteVideoLinkBooking(anyLong())
    }

    @Test
    fun `should raise a tracking event when deleting a recurring appointment`() {
      appointmentService.deleteRecurringAppointmentSequence(100)

      verify(telemetryClient).trackEvent(
        "Recurring Appointment deleted",
        mapOf(
          "appointmentsDeleted" to "2"
        ),
        null
      )
    }
  }

  companion object {
    private val DATE = LocalDate.of(2021, 1, 1)
    private val TIME_SLOT = TimePeriod.ED
    private const val OFFENDER_LOCATION_PREFIX = "MDI-1"
    private const val LOCATION_ID = 1234L

    private val START_TIME = LocalDateTime.parse("2020-10-10T20:01")
    private val END_TIME = LocalDateTime.parse("2020-10-10T21:01")
    private const val EVENT_LOCATION_ID = 1L
    private const val OFFENDER_NO = "A12345"
    private const val AGENCY_ID = "MDI"
    private const val BOOKING_ID = -1L
  }
}
