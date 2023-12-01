package uk.gov.justice.digital.hmpps.whereabouts.services.court

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isA
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Sort
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.LocationDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.listeners.AdditionalInformation
import uk.gov.justice.digital.hmpps.whereabouts.listeners.AppointmentChangedEventMessage
import uk.gov.justice.digital.hmpps.whereabouts.listeners.Reason
import uk.gov.justice.digital.hmpps.whereabouts.listeners.ReleasedOffenderEventMessage
import uk.gov.justice.digital.hmpps.whereabouts.listeners.ScheduleEventStatus
import uk.gov.justice.digital.hmpps.whereabouts.model.Court
import uk.gov.justice.digital.hmpps.whereabouts.model.CourtHearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.model.eqByProps
import uk.gov.justice.digital.hmpps.whereabouts.repository.CourtRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApi.EventPropagation
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiServiceAuditable
import uk.gov.justice.digital.hmpps.whereabouts.services.ValidationException
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional

class VideoLinkBookingServiceTest {
  private val courtRepository: CourtRepository = mock()
  private val courtService = CourtService(courtRepository)
  private val prisonApiService: PrisonApiService = mock()
  private val prisonApiServiceAuditable: PrisonApiServiceAuditable = mock()
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository = mock()
  private val videoLinkBookingRepository: VideoLinkBookingRepository = mock()
  private val videoLinkBookingEventListener: VideoLinkBookingEventListener = mock()
  private val clock: Clock = Clock.fixed(Instant.parse("2020-10-01T00:00:00Z"), ZoneId.of("UTC"))
  private val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
  private val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)

  @BeforeEach
  fun initialiseCourtRepository() {
    whenever(courtRepository.findAll(isA<Sort>())).thenReturn(listOf(Court(COURT_ID, COURT_NAME)))
  }

  @Test
  fun `Should return NO video link appointments`() {
    val service = service()
    val appointments = service.getVideoLinkAppointments(setOf(1, 2))

    verify(videoLinkAppointmentRepository).findVideoLinkAppointmentByAppointmentIdIn(setOf(1, 2))
    assertThat(appointments).isEmpty()
  }

  @Test
  fun `Should return and map video link appointments`() {
    val videoLinkBooking = VideoLinkBooking(
      id = 21L,
      offenderBookingId = 3L,
      courtName = COURT_NAME,
      courtId = null,
      courtHearingType = COURT_HEARING_TYPE,
      madeByTheCourt = true,
      prisonId = "WWI",
    )
    val mainAppointment = VideoLinkAppointment(
      id = 1,
      appointmentId = 3,
      locationId = 6,
      hearingType = HearingType.MAIN,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
      videoLinkBooking = videoLinkBooking,
    )
    val preAppointment = VideoLinkAppointment(
      id = 2,
      appointmentId = 4,
      locationId = 7,
      hearingType = HearingType.PRE,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
      videoLinkBooking = videoLinkBooking,
    )
    val postAppointment = VideoLinkAppointment(
      id = 3,
      appointmentId = 5,
      locationId = 8,
      hearingType = HearingType.POST,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
      videoLinkBooking = videoLinkBooking,
    )
    videoLinkBooking.appointments[HearingType.PRE] = preAppointment
    videoLinkBooking.appointments[HearingType.MAIN] = mainAppointment
    videoLinkBooking.appointments[HearingType.POST] = postAppointment

    whenever(videoLinkAppointmentRepository.findVideoLinkAppointmentByAppointmentIdIn(setOf(3, 4))).thenReturn(
      setOf(
        preAppointment,
        mainAppointment,
        postAppointment,
      ),
    )
    val service = service()
    val appointments = service.getVideoLinkAppointments(setOf(3, 4))

    assertThat(appointments)
      .extracting(
        "id",
        "bookingId",
        "appointmentId",
        "videoLinkBookingId",
        "mainAppointmentId",
        "hearingType",
        "court",
        "courtId",
        "madeByTheCourt",
      )
      .containsExactlyInAnyOrder(
        Tuple.tuple(1L, 3L, 3L, 21L, 3L, HearingType.MAIN, COURT_NAME, null, true),
        Tuple.tuple(2L, 3L, 4L, 21L, 3L, HearingType.PRE, COURT_NAME, null, true),
        Tuple.tuple(3L, 3L, 5L, 21L, 3L, HearingType.POST, COURT_NAME, null, true),
      )
  }

  @Test
  fun `Should return and map pre video link appointments without an associated main appointment`() {
    val videoLinkBooking = VideoLinkBooking(
      id = 21L,
      offenderBookingId = 3L,
      courtName = COURT_NAME,
      courtId = COURT_ID,
      courtHearingType = COURT_HEARING_TYPE,
      madeByTheCourt = false,
      prisonId = "WWI",
    )
    val preAppointment = VideoLinkAppointment(
      id = 2,
      appointmentId = 4,
      locationId = 6,
      hearingType = HearingType.PRE,
      videoLinkBooking = videoLinkBooking,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
    )
    videoLinkBooking.appointments[HearingType.PRE] = preAppointment

    whenever(videoLinkAppointmentRepository.findVideoLinkAppointmentByAppointmentIdIn(setOf(3, 4))).thenReturn(
      setOf(
        preAppointment,
      ),
    )
    val service = service()
    val appointments = service.getVideoLinkAppointments(setOf(3, 4))

    assertThat(appointments)
      .extracting(
        "id",
        "bookingId",
        "appointmentId",
        "videoLinkBookingId",
        "mainAppointmentId",
        "hearingType",
        "court",
        "courtId",
        "madeByTheCourt",
      )
      .containsExactlyInAnyOrder(
        Tuple.tuple(2L, 3L, 4L, 21L, null, HearingType.PRE, COURT_NAME, COURT_ID, false),
      )
  }

  @Nested
  inner class CreateVideoLinkBooking {
    val service = service()

    private val referenceTime = LocalDateTime
      .now(clock)
      .plusDays(8)
      .plusHours(10)
      .plusMinutes(30)

    private val referenceNow = LocalDateTime.now(clock)

    private val mainAppointmentId = 12L
    private val preAppointmentId = 13L
    private val postAppointmentId = 14L

    private val expectedVideoLinkBookingId = 11L

    @BeforeEach
    fun stubLocations() {
      whenever(prisonApiService.getLocation(anyLong())).thenAnswer { locationDto(it.arguments[0] as Long) }
    }

    @Test
    fun `Happy path - Main appointment only - madeByTheCourt`() {
      fun makeBooking(id: Long?) = VideoLinkBooking(
        id = id,
        offenderBookingId = 1L,
        courtName = COURT_NAME,
        courtId = COURT_ID,
        courtHearingType = COURT_HEARING_TYPE,
        prisonId = "WWI",
      ).apply { addMainAppointment(mainAppointmentId, 10L, startDateTime, endDateTime) }

      val specification = VideoLinkBookingSpecification(
        bookingId = 1L,
        court = COURT_NAME,
        courtId = COURT_ID,
        courtHearingType = COURT_HEARING_TYPE,
        comment = "Comment",
        madeByTheCourt = true,
        main = VideoLinkAppointmentSpecification(
          locationId = 2L,
          startTime = referenceTime,
          endTime = referenceTime.plusMinutes(30),
        ),
      )

      whenever(prisonApiServiceAuditable.postAppointment(anyLong(), any(), any())).thenReturn(
        Event(
          mainAppointmentId,
          AGENCY_WANDSWORTH,
          10L,
          startDateTime,
          endDateTime,
        ),
      )

      whenever(videoLinkBookingRepository.save(any())).thenReturn(makeBooking(expectedVideoLinkBookingId))

      val vlbBookingId = service.createVideoLinkBooking(specification)

      assertThat(vlbBookingId).isEqualTo(expectedVideoLinkBookingId)

      verify(prisonApiServiceAuditable).postAppointment(
        1L,
        CreateBookingAppointment(
          appointmentType = VLB_APPOINTMENT_TYPE,
          locationId = 2L,
          comment = "Comment",
          startTime = "2020-10-09T10:30",
          endTime = "2020-10-09T11:00",
        ),
        EventPropagation.DENY,
      )

      verify(videoLinkBookingRepository).save(eqByProps(makeBooking(null)))

      verify(videoLinkBookingEventListener).bookingCreated(
        eqByProps(makeBooking(expectedVideoLinkBookingId)),
        eq(specification),
      )
    }

    @Test
    fun `Validation failure - Neither court or courtId specified`() {
      assertThatThrownBy {
        service.createVideoLinkBooking(
          VideoLinkBookingSpecification(
            bookingId = 1L,
            court = null,
            courtId = null,
            courtHearingType = COURT_HEARING_TYPE,
            comment = "Comment",
            madeByTheCourt = true,
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime.plusMinutes(30),
            ),
          ),
        )
      }.isInstanceOf(ValidationException::class.java).hasMessage("One of court or courtId must be specified")
    }

    @Test
    fun `Validation failure - Main appointment starts in past`() {
      assertThatThrownBy {
        service.createVideoLinkBooking(
          VideoLinkBookingSpecification(
            bookingId = 1L,
            court = COURT_NAME,
            courtId = null,
            courtHearingType = COURT_HEARING_TYPE,
            comment = "Comment",
            madeByTheCourt = true,
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.minusSeconds(1),
              endTime = referenceNow.plusSeconds(1),
            ),
          ),
        )
      }.isInstanceOf(ValidationException::class.java).hasMessage("Main appointment start time must be in the future.")
    }

    @Test
    fun `Validation failure - Main appointment end time not after start time`() {
      assertThatThrownBy {
        service.createVideoLinkBooking(
          VideoLinkBookingSpecification(
            bookingId = 1L,
            court = COURT_NAME,
            courtId = null,
            courtHearingType = COURT_HEARING_TYPE,
            comment = "Comment",
            madeByTheCourt = true,
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime,
            ),
          ),
        )
      }.isInstanceOf(ValidationException::class.java).hasMessage("Main appointment start time must precede end time.")
    }

    @Test
    fun `Validation failure - Pre appointment starts in past`() {
      assertThatThrownBy {
        service.createVideoLinkBooking(
          VideoLinkBookingSpecification(
            bookingId = 1L,
            court = COURT_NAME,
            courtId = null,
            courtHearingType = COURT_HEARING_TYPE,
            comment = "Comment",
            madeByTheCourt = true,
            pre = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.minusSeconds(1),
              endTime = referenceNow.plusSeconds(1),
            ),
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime.plusSeconds(1),
            ),
          ),
        )
      }.isInstanceOf(ValidationException::class.java).hasMessage("Pre appointment start time must be in the future.")
    }

    @Test
    fun `Validation failure - Pre appointment end time not after start time`() {
      assertThatThrownBy {
        service.createVideoLinkBooking(
          VideoLinkBookingSpecification(
            bookingId = 1L,
            court = COURT_NAME,
            courtId = null,
            courtHearingType = COURT_HEARING_TYPE,
            comment = "Comment",
            madeByTheCourt = true,
            pre = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.plusSeconds(1),
              endTime = referenceNow,
            ),
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime.plusSeconds(1),
            ),
          ),
        )
      }.isInstanceOf(ValidationException::class.java).hasMessage("Pre appointment start time must precede end time.")
    }

    @Test
    fun `Validation failure - Post appointment starts in past`() {
      assertThatThrownBy {
        service.createVideoLinkBooking(
          VideoLinkBookingSpecification(
            bookingId = 1L,
            court = COURT_NAME,
            courtId = null,
            courtHearingType = COURT_HEARING_TYPE,
            comment = "Comment",
            madeByTheCourt = true,
            post = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.minusSeconds(1),
              endTime = referenceNow.plusSeconds(1),
            ),
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime.plusSeconds(1),
            ),
          ),
        )
      }.isInstanceOf(ValidationException::class.java).hasMessage("Post appointment start time must be in the future.")
    }

    @Test
    fun `Validation failure - Post appointment end time not after start time`() {
      assertThatThrownBy {
        service.createVideoLinkBooking(
          VideoLinkBookingSpecification(
            bookingId = 1L,
            court = COURT_NAME,
            courtId = null,
            courtHearingType = COURT_HEARING_TYPE,
            comment = "Comment",
            madeByTheCourt = true,
            post = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.plusSeconds(1),
              endTime = referenceNow,
            ),
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime.plusSeconds(1),
            ),
          ),
        )
      }.isInstanceOf(ValidationException::class.java).hasMessage("Post appointment start time must precede end time.")
    }

    @Test
    fun `Validation failure - main locationId not found`() {
      whenever(prisonApiService.getLocation(anyLong())).thenReturn(null)

      assertThatThrownBy {
        service.createVideoLinkBooking(
          VideoLinkBookingSpecification(
            bookingId = 1L,
            court = COURT_NAME,
            courtId = null,
            courtHearingType = COURT_HEARING_TYPE,
            comment = "Comment",
            madeByTheCourt = true,
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.plusSeconds(1),
              endTime = referenceNow.plusSeconds(2),
            ),
          ),
        )
      }.isInstanceOf(ValidationException::class.java).hasMessage("Main locationId 2 not found in NOMIS.")
    }

    @Test
    fun `Happy path - pre, main and post appointments - Not madeByTheCourt`() {
      val offenderBookingId = 1L

      fun makeBooking(id: Long?) =
        VideoLinkBooking(
          id = id,
          offenderBookingId = 1L,
          courtName = COURT_NAME,
          courtId = COURT_ID,
          courtHearingType = COURT_HEARING_TYPE,
          madeByTheCourt = false,
          prisonId = "WWI",
        ).apply {
          addPreAppointment(
            appointmentId = preAppointmentId,
            locationId = 10L,
            id = 20L,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
          )
          addMainAppointment(
            appointmentId = mainAppointmentId,
            locationId = 10L,
            id = 21L,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
          )
          addPostAppointment(
            appointmentId = postAppointmentId,
            locationId = 10L,
            id = 22L,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
          )
        }

      val mainCreateAppointment = CreateBookingAppointment(
        appointmentType = VLB_APPOINTMENT_TYPE,
        locationId = 2L,
        comment = "Comment",
        startTime = "2020-10-09T10:30",
        endTime = "2020-10-09T11:00",
      )

      val preCreateAppointment = CreateBookingAppointment(
        appointmentType = VLB_APPOINTMENT_TYPE,
        locationId = 1L,
        comment = "Comment",
        startTime = "2020-10-09T10:10",
        endTime = "2020-10-09T10:30",
      )

      val postCreateAppointment = CreateBookingAppointment(
        appointmentType = VLB_APPOINTMENT_TYPE,
        locationId = 3L,
        comment = "Comment",
        startTime = "2020-10-09T11:00",
        endTime = "2020-10-09T11:20",
      )

      whenever(
        prisonApiServiceAuditable.postAppointment(
          offenderBookingId,
          mainCreateAppointment,
          EventPropagation.DENY,
        ),
      ).thenReturn(
        Event(
          mainAppointmentId,
          AGENCY_WANDSWORTH,
          10L,
          startDateTime,
          endDateTime,
        ),
      )
      whenever(
        prisonApiServiceAuditable.postAppointment(
          offenderBookingId,
          preCreateAppointment,
          EventPropagation.DENY,
        ),
      ).thenReturn(
        Event(
          preAppointmentId,
          AGENCY_WANDSWORTH,
          10L,
          startDateTime,
          endDateTime,
        ),
      )
      whenever(
        prisonApiServiceAuditable.postAppointment(
          offenderBookingId,
          postCreateAppointment,
          EventPropagation.DENY,
        ),
      ).thenReturn(
        Event(
          postAppointmentId,
          AGENCY_WANDSWORTH,
          10L,
          startDateTime,
          endDateTime,
        ),
      )

      whenever(videoLinkBookingRepository.save(any())).thenReturn(makeBooking(expectedVideoLinkBookingId))

      val vlbBookingId = service.createVideoLinkBooking(
        VideoLinkBookingSpecification(
          bookingId = offenderBookingId,
          court = COURT_NAME,
          courtId = COURT_ID,
          courtHearingType = COURT_HEARING_TYPE,
          comment = "Comment",
          madeByTheCourt = false,
          pre = VideoLinkAppointmentSpecification(
            locationId = 1L,
            startTime = referenceTime.minusMinutes(20),
            endTime = referenceTime,
          ),
          main = VideoLinkAppointmentSpecification(
            locationId = 2L,
            startTime = referenceTime,
            endTime = referenceTime.plusMinutes(30),
          ),
          post = VideoLinkAppointmentSpecification(
            locationId = 3L,
            startTime = referenceTime.plusMinutes(30),
            endTime = referenceTime.plusMinutes(50),
          ),
        ),
      )

      assertThat(vlbBookingId).isEqualTo(11)

      verify(prisonApiServiceAuditable).postAppointment(
        offenderBookingId,
        mainCreateAppointment,
        EventPropagation.DENY,
      )
      verify(prisonApiServiceAuditable).postAppointment(
        offenderBookingId,
        preCreateAppointment,
        EventPropagation.DENY,
      )
      verify(prisonApiServiceAuditable).postAppointment(
        offenderBookingId,
        postCreateAppointment,
        EventPropagation.DENY,
      )

      verify(videoLinkBookingRepository).save(eqByProps(makeBooking(null)))
    }
  }

  @Nested
  inner class UpdateVideoLinkBooking {
    private val referenceTime = LocalDateTime
      .now(clock)
      .plusDays(8)
      .plusHours(10)
      .plusMinutes(30)

    private val referenceNow = LocalDateTime.now(clock)

    @BeforeEach
    fun stubLocations() {
      whenever(prisonApiService.getLocation(anyLong())).thenAnswer { locationDto(it.arguments[0] as Long) }
    }

    @Test
    fun `Happy path - update main`() {
      val service = service()

      val theBooking = VideoLinkBooking(
        id = 1L,
        offenderBookingId = 30L,
        courtName = "The court",
        madeByTheCourt = true,
        prisonId = "WRI",
      )
      theBooking.addMainAppointment(
        appointmentId = 40L,
        id = 2L,
        locationId = 10L,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
      )

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(theBooking))
      whenever(
        prisonApiServiceAuditable.postAppointment(
          anyLong(),
          any(),
          any(),
        ),
      ).thenReturn(
        Event(
          3L,
          "WRI",
          10L,
          startDateTime,
          endDateTime,
        ),
      )

      val updateSpecification = VideoLinkBookingUpdateSpecification(
        courtId = "TSTCRT",
        comment = "New Comment",
        main = VideoLinkAppointmentSpecification(
          locationId = 99L,
          startTime = referenceTime,
          endTime = referenceTime.plusMinutes(30),
        ),
      )

      service.updateVideoLinkBooking(1L, updateSpecification)

      verify(prisonApiService).deleteAppointments(
        listOf(40),
        EventPropagation.DENY,
      )
      verify(prisonApiServiceAuditable).postAppointment(
        30L,
        CreateBookingAppointment(
          appointmentType = "VLB",
          locationId = 99L,
          startTime = "2020-10-09T10:30",
          endTime = "2020-10-09T11:00",
          comment = "New Comment",
        ),
        EventPropagation.DENY,
      )

      val expectedAfterUpdate =
        VideoLinkBooking(
          id = 1L,
          offenderBookingId = 30L,
          courtName = null,
          courtId = "TSTCRT",
          madeByTheCourt = true,
          prisonId = "WRI",
        )
      expectedAfterUpdate.addMainAppointment(3L, 10L, startDateTime, endDateTime)

      assertThat(theBooking)
        .usingRecursiveComparison()
        .isEqualTo(expectedAfterUpdate)

      verify(videoLinkBookingEventListener).bookingUpdated(expectedAfterUpdate, updateSpecification)
    }

    /**
     * Other validations are the same as for Create Booking, so not tested here.
     */
    @Test
    fun `Validation - main appointment start time in the past`() {
      val service = service()
      assertThatThrownBy {
        service.updateVideoLinkBooking(
          1L,
          VideoLinkBookingUpdateSpecification(
            courtId = "TSTCRT",
            comment = "New Comment",
            main = VideoLinkAppointmentSpecification(
              locationId = 99L,
              startTime = referenceNow.minusSeconds(1),
              endTime = referenceNow.plusSeconds(1),
            ),
          ),
        )
      }
        .isInstanceOf(ValidationException::class.java)
        .hasMessage("Main appointment start time must be in the future.")
    }

    @Test
    fun `Happy path - update pre, main and post`() {
      val service = service()

      val theBooking =
        VideoLinkBooking(
          id = 1L,
          offenderBookingId = 30L,
          courtName = "The court",
          madeByTheCourt = true,
          prisonId = "WRI",
        )
      theBooking.addPreAppointment(
        appointmentId = 40L,
        id = 2L,
        locationId = 10L,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
      )
      theBooking.addMainAppointment(
        appointmentId = 41L,
        id = 3L,
        locationId = 10L,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
      )
      theBooking.addPostAppointment(
        appointmentId = 42L,
        id = 4L,
        locationId = 10L,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
      )

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(theBooking))
      whenever(prisonApiServiceAuditable.postAppointment(anyLong(), any(), any())).thenReturn(
        Event(
          9999L,
          "WRI",
          10L,
          startDateTime,
          endDateTime,
        ),
      )

      service.updateVideoLinkBooking(
        1L,
        VideoLinkBookingUpdateSpecification(
          courtId = "TSTCRT",
          comment = "New Comment",
          pre = VideoLinkAppointmentSpecification(
            locationId = 99L,
            startTime = referenceTime,
            endTime = referenceTime.plusMinutes(30),
          ),
          main = VideoLinkAppointmentSpecification(
            locationId = 98L,
            startTime = referenceTime.plusMinutes(30),
            endTime = referenceTime.plusMinutes(60),
          ),
          post = VideoLinkAppointmentSpecification(
            locationId = 97L,
            startTime = referenceTime.plusMinutes(60),
            endTime = referenceTime.plusMinutes(90),
          ),
        ),
      )

      verify(prisonApiService).deleteAppointments(
        listOf(40, 41, 42),
        EventPropagation.DENY,
      )

      verify(prisonApiServiceAuditable).postAppointment(
        30L,
        CreateBookingAppointment(
          appointmentType = "VLB",
          locationId = 99L,
          startTime = "2020-10-09T10:30",
          endTime = "2020-10-09T11:00",
          comment = "New Comment",
        ),
        EventPropagation.DENY,
      )

      verify(prisonApiServiceAuditable).postAppointment(
        30L,
        CreateBookingAppointment(
          appointmentType = "VLB",
          locationId = 98L,
          startTime = "2020-10-09T11:00",
          endTime = "2020-10-09T11:30",
          comment = "New Comment",
        ),
        EventPropagation.DENY,
      )

      verify(prisonApiServiceAuditable).postAppointment(
        30L,
        CreateBookingAppointment(
          appointmentType = "VLB",
          locationId = 97L,
          startTime = "2020-10-09T11:30",
          endTime = "2020-10-09T12:00",
          comment = "New Comment",
        ),
        EventPropagation.DENY,
      )

      assertThat(theBooking)
        .usingRecursiveComparison()
        .isEqualTo(
          VideoLinkBooking(
            id = 1L,
            offenderBookingId = 30L,
            courtName = null,
            courtId = "TSTCRT",
            madeByTheCourt = true,
            prisonId = "WRI",
          ).apply {
            addPreAppointment(
              appointmentId = 9999L,
              locationId = 10L,
              startDateTime = startDateTime,
              endDateTime = endDateTime,
            )
            addMainAppointment(
              appointmentId = 9999L,
              locationId = 10L,
              startDateTime = startDateTime,
              endDateTime = endDateTime,
            )
            addPostAppointment(
              appointmentId = 9999L,
              locationId = 10L,
              startDateTime = startDateTime,
              endDateTime = endDateTime,
            )
          },
        )
    }
  }

  @Nested
  inner class DeleteVideoLinkBooking {
    val service = service()

    private val mainAppointmentId = 12L
    private val preAppointmentId = 13L
    private val postAppointmentId = 14L

    private val videoLinkBooking =
      VideoLinkBooking(offenderBookingId = 1, courtName = COURT_NAME, id = 100, prisonId = "WRI").apply {
        addPreAppointment(
          appointmentId = preAppointmentId,
          id = 111,
          locationId = 10L,
          startDateTime = startDateTime,
          endDateTime = endDateTime,
        )
        addMainAppointment(
          appointmentId = mainAppointmentId,
          id = 222,
          locationId = 10L,
          startDateTime = startDateTime,
          endDateTime = endDateTime,
        )
        addPostAppointment(
          appointmentId = postAppointmentId,
          id = 333,
          locationId = 10L,
          startDateTime = startDateTime,
          endDateTime = endDateTime,
        )
      }

    @Test
    fun `When there is no video link booking it throws an exception`() {
      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.empty())
      Assertions.assertThrows(EntityNotFoundException::class.java) {
        service.deleteVideoLinkBooking(videoLinkBooking.id!!)
      }
    }

    @Test
    fun `Happy path`() {
      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(videoLinkBooking))

      service.deleteVideoLinkBooking(videoLinkBooking.id!!)

      verify(prisonApiService).deleteAppointments(
        listOf(12, 13, 14),
        EventPropagation.DENY,
      )

      verify(videoLinkBookingRepository).deleteById(videoLinkBooking.id!!)
      verify(videoLinkBookingEventListener).bookingDeleted(videoLinkBooking)
    }
  }

  @Nested
  inner class ProcessNomisUpdate {
    val service = service()

    @Test
    fun `Skip update when appointment not exist`() {
      val appointmentChangedEventMessage = AppointmentChangedEventMessage(
        scheduleEventId = 484209875,
        agencyLocationId = "WWI",
        bookingId = 1056979,
        eventDatetime = "2022-10-06T09:34:40",
        recordDeleted = false,
        scheduleEventStatus = ScheduleEventStatus.SCH,
        scheduledEndTime = "2022-10-06T15:00:00",
        scheduledStartTime = "2022-10-06T11:00:00",
      )
      whenever(videoLinkAppointmentRepository.findOneByAppointmentId(anyLong())).thenReturn(null)
      service.processNomisUpdate(appointmentChangedEventMessage)
      verify(videoLinkAppointmentRepository).findOneByAppointmentId(anyLong())
      verify(videoLinkBookingRepository, never()).delete(any())
    }

    @Test
    fun `Skip update and logs the event when appointment exist and recordDeleted flag is false`() {
      val appointmentChangedEventMessage = AppointmentChangedEventMessage(
        scheduleEventId = 484209875,
        agencyLocationId = "WWI",
        bookingId = 1056979,
        eventDatetime = "2022-10-06T09:34:40",
        recordDeleted = false,
        scheduleEventStatus = ScheduleEventStatus.SCH,
        scheduledEndTime = "2022-10-06T15:00:00",
        scheduledStartTime = "2022-10-06T11:00:00",
      )
      val videoLinkAppointment = mock<VideoLinkAppointment>()

      whenever(videoLinkAppointmentRepository.findOneByAppointmentId(anyLong())).thenReturn(videoLinkAppointment)
      service.processNomisUpdate(appointmentChangedEventMessage)
      verify(videoLinkBookingEventListener).appointmentUpdatedInNomis(any(), any())
      verify(videoLinkBookingRepository, never()).delete(any())
    }

    @Test
    fun `delete single appointment when recordDeleted flag is true and appointment is not MAIN`() {
      val appointmentChangedEventMessage = AppointmentChangedEventMessage(
        scheduleEventId = 2,
        agencyLocationId = "WWI",
        bookingId = 1056979,
        eventDatetime = "2022-10-06T09:34:40",
        recordDeleted = true,
        scheduleEventStatus = ScheduleEventStatus.SCH,
        scheduledEndTime = "2022-10-06T15:00:00",
        scheduledStartTime = "2022-10-06T11:00:00",
      )
      val videoLinkBooking = createVideoLinkBooking()

      whenever(videoLinkAppointmentRepository.findOneByAppointmentId(anyLong())).thenReturn(videoLinkBooking.appointments[HearingType.PRE])
      service.processNomisUpdate(appointmentChangedEventMessage)
      verify(videoLinkBookingRepository).save(any())
      verify(videoLinkBookingRepository, never()).delete(any())
    }

    @Test
    fun `delete PRE,POST,MAIN appointments when recordDeleted flag is true and appointment is MAIN`() {
      val appointmentChangedEventMessage = AppointmentChangedEventMessage(
        scheduleEventId = 1,
        agencyLocationId = "WWI",
        bookingId = 1056979,
        eventDatetime = "2022-10-06T09:34:40",
        recordDeleted = true,
        scheduleEventStatus = ScheduleEventStatus.SCH,
        scheduledEndTime = "2022-10-06T15:00:00",
        scheduledStartTime = "2022-10-06T11:00:00",
      )
      val videoLinkBooking = createVideoLinkBooking()

      whenever(videoLinkAppointmentRepository.findOneByAppointmentId(anyLong())).thenReturn(videoLinkBooking.appointments[HearingType.MAIN])
      service.processNomisUpdate(appointmentChangedEventMessage)
      verify(videoLinkBookingRepository).delete(any())
      verify(prisonApiService).deleteAppointments(listOf(3, 4, 5), EventPropagation.DENY)
    }

    @Test
    fun `delete PRE,POST,MAIN appointments when recordDeleted flag is false and MAIN appointment scheduleEventStatus is CANC`() {
      val appointmentChangedEventMessage = AppointmentChangedEventMessage(
        scheduleEventId = 1,
        agencyLocationId = "WWI",
        bookingId = 1056979,
        eventDatetime = "2022-10-06T09:34:40",
        recordDeleted = false,
        scheduleEventStatus = ScheduleEventStatus.CANC,
        scheduledEndTime = "2022-10-06T15:00:00",
        scheduledStartTime = "2022-10-06T11:00:00",
      )
      val videoLinkBooking = createVideoLinkBooking()

      whenever(videoLinkAppointmentRepository.findOneByAppointmentId(anyLong())).thenReturn(videoLinkBooking.appointments[HearingType.MAIN])
      service.processNomisUpdate(appointmentChangedEventMessage)
      verify(videoLinkBookingRepository).delete(any())
      verify(prisonApiService).deleteAppointments(listOf(3, 4, 5), EventPropagation.DENY)
    }

    @Test
    fun `delete PRE appointment when recordDeleted flag is false and PRE appointment scheduleEventStatus is CANC`() {
      val appointmentChangedEventMessage = AppointmentChangedEventMessage(
        scheduleEventId = 1,
        agencyLocationId = "WWI",
        bookingId = 1056979,
        eventDatetime = "2022-10-06T09:34:40",
        recordDeleted = false,
        scheduleEventStatus = ScheduleEventStatus.CANC,
        scheduledEndTime = "2022-10-06T15:00:00",
        scheduledStartTime = "2022-10-06T11:00:00",
      )
      val videoLinkBooking = createVideoLinkBooking()

      whenever(videoLinkAppointmentRepository.findOneByAppointmentId(anyLong())).thenReturn(videoLinkBooking.appointments[HearingType.PRE])
      service.processNomisUpdate(appointmentChangedEventMessage)
      verify(videoLinkBookingRepository).save(any())
      verify(videoLinkBookingRepository, never()).delete(any())
      verify(prisonApiService, never()).deleteAppointments(any(), any())
    }

    private fun createVideoLinkBooking(): VideoLinkBooking {
      val videoLinkBooking = VideoLinkBooking(
        id = 21L,
        offenderBookingId = 3L,
        courtName = COURT_NAME,
        courtId = null,
        courtHearingType = COURT_HEARING_TYPE,
        madeByTheCourt = true,
        prisonId = "WWI",
      )
      val mainAppointment = VideoLinkAppointment(
        id = 1,
        appointmentId = 3,
        locationId = 6,
        hearingType = HearingType.MAIN,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        videoLinkBooking = videoLinkBooking,
      )
      val preAppointment = VideoLinkAppointment(
        id = 2,
        appointmentId = 4,
        locationId = 7,
        hearingType = HearingType.PRE,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        videoLinkBooking = videoLinkBooking,
      )
      val postAppointment = VideoLinkAppointment(
        id = 3,
        appointmentId = 5,
        locationId = 8,
        hearingType = HearingType.POST,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        videoLinkBooking = videoLinkBooking,
      )
      videoLinkBooking.appointments[HearingType.PRE] = preAppointment
      videoLinkBooking.appointments[HearingType.MAIN] = mainAppointment
      videoLinkBooking.appointments[HearingType.POST] = postAppointment

      return videoLinkBooking
    }
  }

  @Nested
  inner class GetVideoLinkBooking {
    val service = service()

    private val mainAppointmentId = 12L
    private val preAppointmentId = 13L
    private val postAppointmentId = 14L

    private val videoLinkBooking =
      VideoLinkBooking(
        offenderBookingId = 1,
        courtName = COURT_NAME,
        courtId = COURT_ID,
        courtHearingType = COURT_HEARING_TYPE,
        id = 100,
        prisonId = "WRI",
      ).apply {
        addPreAppointment(
          appointmentId = preAppointmentId,
          id = 111,
          locationId = 10L,
          startDateTime = startDateTime,
          endDateTime = endDateTime,
        )
        addMainAppointment(
          appointmentId = mainAppointmentId,
          id = 222,
          locationId = 10L,
          startDateTime = startDateTime,
          endDateTime = endDateTime,
        )
        addPostAppointment(
          appointmentId = postAppointmentId,
          id = 333,
          locationId = 10L,
          startDateTime = startDateTime,
          endDateTime = endDateTime,
        )
      }

    private val preAppointment = PrisonAppointment(
      bookingId = 1,
      eventId = preAppointmentId,
      startTime = LocalDateTime.of(2020, 12, 2, 12, 0, 0),
      endTime = LocalDateTime.of(2020, 12, 2, 13, 0, 0),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 10,
      comment = "any comment",
    )

    private val mainAppointment = PrisonAppointment(
      bookingId = 1,
      eventId = mainAppointmentId,
      startTime = LocalDateTime.of(2020, 12, 2, 13, 0, 0),
      endTime = LocalDateTime.of(2020, 12, 2, 14, 0, 0),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 9,
      comment = "any comment",
    )

    private val postAppointment = PrisonAppointment(
      bookingId = 1,
      eventId = postAppointmentId,
      startTime = LocalDateTime.of(2020, 12, 2, 14, 0, 0),
      endTime = LocalDateTime.of(2020, 12, 2, 15, 0, 0),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 5,
      comment = "any comment",
    )

    @Test
    fun `When there is no video link booking it throws an exception`() {
      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.empty())
      Assertions.assertThrows(EntityNotFoundException::class.java) {
        service.getVideoLinkBooking(videoLinkBooking.id!!)
      }
    }

    @Test
    fun `When there is a video link booking with pre, main and post`() {
      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(videoLinkBooking))

      whenever(prisonApiService.getPrisonAppointment(mainAppointmentId)).thenReturn(mainAppointment)
      whenever(prisonApiService.getPrisonAppointment(preAppointmentId)).thenReturn(preAppointment)
      whenever(prisonApiService.getPrisonAppointment(postAppointmentId)).thenReturn(postAppointment)
      val result = service.getVideoLinkBooking(videoLinkBooking.id!!)

      assertThat(result).isEqualTo(
        VideoLinkBookingResponse(
          videoLinkBookingId = 100,
          bookingId = 1,
          agencyId = "WWI",
          court = COURT_NAME,
          courtId = COURT_ID,
          courtHearingType = COURT_HEARING_TYPE,
          comment = "any comment",
          pre = VideoLinkBookingResponse.LocationTimeslot(
            locationId = preAppointment.eventLocationId,
            startTime = preAppointment.startTime,
            endTime = preAppointment.endTime!!,
          ),
          main = VideoLinkBookingResponse.LocationTimeslot(
            locationId = mainAppointment.eventLocationId,
            startTime = mainAppointment.startTime,
            endTime = mainAppointment.endTime!!,
          ),
          post = VideoLinkBookingResponse.LocationTimeslot(
            locationId = postAppointment.eventLocationId,
            startTime = postAppointment.startTime,
            endTime = postAppointment.endTime!!,
          ),
        ),
      )
    }

    @Test
    fun `When there is a video link booking with main appointment only`() {
      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(videoLinkBooking))
      whenever(prisonApiService.getPrisonAppointment(mainAppointmentId)).thenReturn(mainAppointment)
      whenever(prisonApiService.getPrisonAppointment(preAppointmentId)).thenReturn(null)
      whenever(prisonApiService.getPrisonAppointment(postAppointmentId)).thenReturn(null)
      val result = service.getVideoLinkBooking(videoLinkBooking.id!!)
      assertThat(result).isEqualTo(
        VideoLinkBookingResponse(
          videoLinkBookingId = 100,
          bookingId = 1,
          agencyId = "WWI",
          court = COURT_NAME,
          courtId = COURT_ID,
          courtHearingType = COURT_HEARING_TYPE,
          comment = "any comment",
          main = VideoLinkBookingResponse.LocationTimeslot(
            locationId = mainAppointment.eventLocationId,
            startTime = mainAppointment.startTime,
            endTime = mainAppointment.endTime!!,
          ),
        ),
      )
    }

    @Test
    fun `When there is a video link booking with pre and post appointments and no main appointment`() {
      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(videoLinkBooking))
      whenever(prisonApiService.getPrisonAppointment(mainAppointmentId)).thenReturn(null)
      whenever(prisonApiService.getPrisonAppointment(preAppointmentId)).thenReturn(preAppointment)
      whenever(prisonApiService.getPrisonAppointment(postAppointmentId)).thenReturn(postAppointment)
      Assertions.assertThrows(EntityNotFoundException::class.java) {
        service.getVideoLinkBooking(videoLinkBooking.id!!)
      }
    }
  }

  @Nested
  inner class UpdateVideoLinkBookingComment {

    @Test
    fun `Happy path - Pre, main and post appointments`() {
      val service = service()
      val newComment = "New comment"

      whenever(videoLinkBookingRepository.findById(1L))
        .thenReturn(
          Optional.of(
            VideoLinkBooking(id = 1L, offenderBookingId = 999L, courtName = "The Court", prisonId = "WWI").apply {
              addPreAppointment(
                id = 100L,
                appointmentId = 10L,
                locationId = 10L,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
              )
              addMainAppointment(
                id = 101L,
                appointmentId = 11L,
                locationId = 10L,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
              )
              addPostAppointment(
                id = 102L,
                appointmentId = 12L,
                locationId = 10L,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
              )
            },
          ),
        )

      service.updateVideoLinkBookingComment(1L, newComment)

      verify(prisonApiService).updateAppointmentComment(10L, newComment, EventPropagation.DENY)
      verify(prisonApiService).updateAppointmentComment(11L, newComment, EventPropagation.DENY)
      verify(prisonApiService).updateAppointmentComment(12L, newComment, EventPropagation.DENY)
      verifyNoMoreInteractions(prisonApiService)
    }

    @Test
    fun `Happy path - main appointment only, no comment`() {
      val service = service()
      val newComment = ""

      whenever(videoLinkBookingRepository.findById(1L))
        .thenReturn(
          Optional.of(
            VideoLinkBooking(id = 1L, offenderBookingId = 999L, courtName = "The Court", prisonId = "WWI").apply {
              addMainAppointment(
                id = 101L,
                appointmentId = 11L,
                locationId = 10L,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
              )
            },
          ),
        )

      service.updateVideoLinkBookingComment(1L, newComment)

      verify(prisonApiService).updateAppointmentComment(11L, newComment, EventPropagation.DENY)
      verifyNoMoreInteractions(prisonApiService)
    }
  }

  @Nested
  inner class CancelVideoLinkBooking {
    val service = service()

    private val mainAppointmentId = 12L
    private val preAppointmentId = 13L
    private val postAppointmentId = 14L

    private val videoLinkBooking =
      VideoLinkBooking(offenderBookingId = 1, courtName = COURT_NAME, id = 100, prisonId = "WRI").apply {
        addPreAppointment(
          appointmentId = preAppointmentId,
          id = 111,
          locationId = 10L,
          startDateTime = startDateTime,
          endDateTime = endDateTime,
        )
        addMainAppointment(
          appointmentId = mainAppointmentId,
          id = 222,
          locationId = 10L,
          startDateTime = startDateTime,
          endDateTime = endDateTime,
        )
        addPostAppointment(
          appointmentId = postAppointmentId,
          id = 333,
          locationId = 10L,
          startDateTime = startDateTime,
          endDateTime = endDateTime,
        )
      }

    @Test
    fun `Should not delete BVL appointment if release reason is anything other than Released or Transferred`() {
      val message = ReleasedOffenderEventMessage(
        occurredAt = "2023-11-20T17:07:58Z",
        additionalInformation = AdditionalInformation(
          prisonId = "SWI",
          nomsNumber = "A7215DZ",
          reason = Reason.RELEASED_TO_HOSPITAL,
        ),
      )
      service.deleteAppointments(message)
      verify(videoLinkBookingRepository, never()).deleteById(any())
      verify(videoLinkBookingEventListener, never()).bookingDeleted(any())
    }

    @Test
    fun `Should delete BVL appointment when release reason is TRANSFERRED`() {
      val message = ReleasedOffenderEventMessage(
        occurredAt = "2023-11-20T17:07:58Z",
        additionalInformation = AdditionalInformation(
          prisonId = "SWI",
          nomsNumber = "A7215DZ",
          reason = Reason.TRANSFERRED,
        ),
      )

      whenever(
        videoLinkAppointmentRepository.findAllByHearingTypeIsAndStartDateTimeIsAfterAndVideoLinkBookingOffenderBookingIdIsAndVideoLinkBookingPrisonIdIs(
          any(),
          any(),
          any(),
          any(),
        ),
      )
        .thenReturn(setOf(DataHelpers.makeVideoLinkAppointment()))

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(videoLinkBooking))

      service.deleteAppointments(message)
      verify(videoLinkBookingRepository, times(1)).deleteById(any())
      verify(videoLinkBookingEventListener, times(1)).bookingDeleted(any())
    }

    @Test
    fun `Should delete BVL appointment when release reason is RELEASED`() {
      val message = ReleasedOffenderEventMessage(
        occurredAt = "2023-11-20T17:07:58Z",
        additionalInformation = AdditionalInformation(
          prisonId = "SWI",
          nomsNumber = "A7215DZ",
          reason = Reason.RELEASED,
        ),
      )

      whenever(
        videoLinkAppointmentRepository.findAllByHearingTypeIsAndStartDateTimeIsAfterAndVideoLinkBookingOffenderBookingIdIsAndVideoLinkBookingPrisonIdIs(
          any(),
          any(),
          any(),
          any(),
        ),
      )
        .thenReturn(setOf(DataHelpers.makeVideoLinkAppointment()))

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(videoLinkBooking))

      service.deleteAppointments(message)
      verify(videoLinkBookingRepository, times(1)).deleteById(any())
      verify(videoLinkBookingEventListener, times(1)).bookingDeleted(any())
    }
  }

  private fun service() = VideoLinkBookingService(
    courtService,
    prisonApiService,
    prisonApiServiceAuditable,
    videoLinkAppointmentRepository,
    videoLinkBookingRepository,
    clock,
    videoLinkBookingEventListener,
  )

  companion object {
    const val COURT_NAME = "York Crown Court"
    const val COURT_ID = "YRKCC"
    val COURT_HEARING_TYPE = CourtHearingType.APPEAL
    const val VLB_APPOINTMENT_TYPE = "VLB"
    const val AGENCY_WANDSWORTH = "WWI"

    fun locationDto(locationId: Long) = LocationDto(
      locationId = locationId,
      locationType = "VIDE",
      agencyId = "WWI",
      description = null,
      parentLocationId = null,
      currentOccupancy = null,
      locationPrefix = null,
      internalLocationCode = null,
      userDescription = null,
      locationUsage = null,
      operationalCapacity = null,
    )

    fun scheduledAppointments(
      type: String,
      agencyId: String,
      firstId: Long = 1L,
      lastId: Long = 30L,
    ): List<ScheduledAppointmentDto> =
      (firstId..lastId).map {
        ScheduledAppointmentDto(
          id = it,
          agencyId = agencyId,
          locationId = it + 1000,
          appointmentTypeCode = type,
          startTime = LocalDateTime.of(2020, 1, 1, 0, 0).plusHours(it),
          endTime = LocalDateTime.of(2020, 1, 1, 0, 0).plusHours(it + 1),
          offenderNo = "A1234AA",
        )
      }

    fun videoLinkBookings(
      court: String?,
      courtId: String?,
      firstAppointmentId: Long = 1L,
      lastAppointmentId: Long = 30L,
      locationId: Long,
      startDateTime: LocalDateTime,
      endDateTime: LocalDateTime,
    ): List<VideoLinkBooking> =
      (firstAppointmentId..lastAppointmentId).map {
        VideoLinkBooking(
          id = it + 1000L,
          offenderBookingId = it + 1000,
          courtName = court,
          courtId = courtId,
          prisonId = "WWI",
        ).apply {
          addPreAppointment(
            id = it + 10000,
            appointmentId = it + 1000,
            locationId = locationId,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
          )
          addMainAppointment(
            id = it + 20000,
            appointmentId = it,
            locationId = locationId,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
          )
          addPostAppointment(
            id = it + 30000,
            appointmentId = it + 2000,
            locationId = locationId,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
          )
        }
      }
  }
}
