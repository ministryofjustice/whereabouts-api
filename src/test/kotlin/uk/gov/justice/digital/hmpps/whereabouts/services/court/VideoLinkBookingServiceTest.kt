package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.tuple
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isA
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
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
import uk.gov.justice.digital.hmpps.whereabouts.model.Court
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.model.eqByProps
import uk.gov.justice.digital.hmpps.whereabouts.repository.CourtRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiServiceAuditable
import uk.gov.justice.digital.hmpps.whereabouts.services.ValidationException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional
import javax.persistence.EntityNotFoundException

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
      madeByTheCourt = true,
      prisonId = "WWI"
    )
    val mainAppointment = VideoLinkAppointment(
      id = 1,
      appointmentId = 3,
      locationId = 6,
      hearingType = HearingType.MAIN,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
      videoLinkBooking = videoLinkBooking
    )
    val preAppointment = VideoLinkAppointment(
      id = 2,
      appointmentId = 4,
      locationId = 7,
      hearingType = HearingType.PRE,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
      videoLinkBooking = videoLinkBooking
    )
    val postAppointment = VideoLinkAppointment(
      id = 3,
      appointmentId = 5,
      locationId = 8,
      hearingType = HearingType.POST,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
      videoLinkBooking = videoLinkBooking
    )
    videoLinkBooking.appointments[HearingType.PRE] = preAppointment
    videoLinkBooking.appointments[HearingType.MAIN] = mainAppointment
    videoLinkBooking.appointments[HearingType.POST] = postAppointment

    whenever(videoLinkAppointmentRepository.findVideoLinkAppointmentByAppointmentIdIn(setOf(3, 4))).thenReturn(
      setOf(
        preAppointment,
        mainAppointment,
        postAppointment
      )
    )
    val service = service()
    val appointments = service.getVideoLinkAppointments(setOf(3, 4))

    assertThat(appointments)
      .extracting("id", "bookingId", "appointmentId", "videoLinkBookingId", "mainAppointmentId", "hearingType", "court", "courtId", "madeByTheCourt")
      .containsExactlyInAnyOrder(
        Tuple.tuple(1L, 3L, 3L, 21L, 3L, HearingType.MAIN, COURT_NAME, null, true),
        Tuple.tuple(2L, 3L, 4L, 21L, 3L, HearingType.PRE, COURT_NAME, null, true),
        Tuple.tuple(3L, 3L, 5L, 21L, 3L, HearingType.POST, COURT_NAME, null, true)
      )
  }

  @Test
  fun `Should return and map pre video link appointments without an associated main appointment`() {
    val videoLinkBooking = VideoLinkBooking(
      id = 21L,
      offenderBookingId = 3L,
      courtName = COURT_NAME,
      courtId = COURT_ID,
      madeByTheCourt = false,
      prisonId = "WWI"
    )
    val preAppointment = VideoLinkAppointment(
      id = 2,
      appointmentId = 4,
      locationId = 6,
      hearingType = HearingType.PRE,
      videoLinkBooking = videoLinkBooking,
      startDateTime = startDateTime,
      endDateTime = endDateTime
    )
    videoLinkBooking.appointments[HearingType.PRE] = preAppointment

    whenever(videoLinkAppointmentRepository.findVideoLinkAppointmentByAppointmentIdIn(setOf(3, 4))).thenReturn(
      setOf(
        preAppointment
      )
    )
    val service = service()
    val appointments = service.getVideoLinkAppointments(setOf(3, 4))

    assertThat(appointments)
      .extracting("id", "bookingId", "appointmentId", "videoLinkBookingId", "mainAppointmentId", "hearingType", "court", "courtId", "madeByTheCourt")
      .containsExactlyInAnyOrder(
        Tuple.tuple(2L, 3L, 4L, 21L, null, HearingType.PRE, COURT_NAME, COURT_ID, false)
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
        prisonId = "WWI"
      ).apply { addMainAppointment(mainAppointmentId, 10L, startDateTime, endDateTime) }

      val specification = VideoLinkBookingSpecification(
        bookingId = 1L,
        court = COURT_NAME,
        courtId = COURT_ID,
        comment = "Comment",
        madeByTheCourt = true,
        main = VideoLinkAppointmentSpecification(
          locationId = 2L,
          startTime = referenceTime,
          endTime = referenceTime.plusMinutes(30)
        )
      )

      whenever(prisonApiServiceAuditable.postAppointment(anyLong(), any())).thenReturn(
        Event(
          mainAppointmentId,
          AGENCY_WANDSWORTH,
          10L,
          startDateTime,
          endDateTime
        )
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
          endTime = "2020-10-09T11:00"
        )
      )

      verify(videoLinkBookingRepository).save(eqByProps(makeBooking(null)))

      verify(videoLinkBookingEventListener).bookingCreated(
        eqByProps(makeBooking(expectedVideoLinkBookingId)),
        eq(specification)
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
            comment = "Comment",
            madeByTheCourt = true,
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime.plusMinutes(30)
            )
          )
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
            comment = "Comment",
            madeByTheCourt = true,
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.minusSeconds(1),
              endTime = referenceNow.plusSeconds(1)
            )
          )
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
            comment = "Comment",
            madeByTheCourt = true,
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime
            )
          )
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
            comment = "Comment",
            madeByTheCourt = true,
            pre = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.minusSeconds(1),
              endTime = referenceNow.plusSeconds(1)
            ),
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime.plusSeconds(1)
            )
          )
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
            comment = "Comment",
            madeByTheCourt = true,
            pre = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.plusSeconds(1),
              endTime = referenceNow
            ),
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime.plusSeconds(1)
            )
          )
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
            comment = "Comment",
            madeByTheCourt = true,
            post = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.minusSeconds(1),
              endTime = referenceNow.plusSeconds(1)
            ),
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime.plusSeconds(1)
            )
          )
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
            comment = "Comment",
            madeByTheCourt = true,
            post = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.plusSeconds(1),
              endTime = referenceNow
            ),
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceTime,
              endTime = referenceTime.plusSeconds(1)
            )
          )
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
            comment = "Comment",
            madeByTheCourt = true,
            main = VideoLinkAppointmentSpecification(
              locationId = 2L,
              startTime = referenceNow.plusSeconds(1),
              endTime = referenceNow.plusSeconds(2)
            )
          )
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
          madeByTheCourt = false,
          prisonId = "WWI"
        ).apply {
          addPreAppointment(appointmentId = preAppointmentId, locationId = 10L, id = 20L, startDateTime = startDateTime, endDateTime = endDateTime)
          addMainAppointment(appointmentId = mainAppointmentId, locationId = 10L, id = 21L, startDateTime = startDateTime, endDateTime = endDateTime)
          addPostAppointment(appointmentId = postAppointmentId, locationId = 10L, id = 22L, startDateTime = startDateTime, endDateTime = endDateTime)
        }

      val mainCreateAppointment = CreateBookingAppointment(
        appointmentType = VLB_APPOINTMENT_TYPE,
        locationId = 2L,
        comment = "Comment",
        startTime = "2020-10-09T10:30",
        endTime = "2020-10-09T11:00"
      )

      val preCreateAppointment = CreateBookingAppointment(
        appointmentType = VLB_APPOINTMENT_TYPE,
        locationId = 1L,
        comment = "Comment",
        startTime = "2020-10-09T10:10",
        endTime = "2020-10-09T10:30"
      )

      val postCreateAppointment = CreateBookingAppointment(
        appointmentType = VLB_APPOINTMENT_TYPE,
        locationId = 3L,
        comment = "Comment",
        startTime = "2020-10-09T11:00",
        endTime = "2020-10-09T11:20"
      )

      whenever(prisonApiServiceAuditable.postAppointment(offenderBookingId, mainCreateAppointment)).thenReturn(
        Event(
          mainAppointmentId,
          AGENCY_WANDSWORTH,
          10L,
          startDateTime,
          endDateTime
        )
      )
      whenever(prisonApiServiceAuditable.postAppointment(offenderBookingId, preCreateAppointment)).thenReturn(
        Event(
          preAppointmentId,
          AGENCY_WANDSWORTH,
          10L,
          startDateTime,
          endDateTime
        )
      )
      whenever(prisonApiServiceAuditable.postAppointment(offenderBookingId, postCreateAppointment)).thenReturn(
        Event(
          postAppointmentId,
          AGENCY_WANDSWORTH,
          10L,
          startDateTime,
          endDateTime
        )
      )

      whenever(videoLinkBookingRepository.save(any())).thenReturn(makeBooking(expectedVideoLinkBookingId))

      val vlbBookingId = service.createVideoLinkBooking(
        VideoLinkBookingSpecification(
          bookingId = offenderBookingId,
          court = COURT_NAME,
          courtId = COURT_ID,
          comment = "Comment",
          madeByTheCourt = false,
          pre = VideoLinkAppointmentSpecification(
            locationId = 1L,
            startTime = referenceTime.minusMinutes(20),
            endTime = referenceTime
          ),
          main = VideoLinkAppointmentSpecification(
            locationId = 2L,
            startTime = referenceTime,
            endTime = referenceTime.plusMinutes(30)
          ),
          post = VideoLinkAppointmentSpecification(
            locationId = 3L,
            startTime = referenceTime.plusMinutes(30),
            endTime = referenceTime.plusMinutes(50)
          )
        )
      )

      assertThat(vlbBookingId).isEqualTo(11)

      verify(prisonApiServiceAuditable).postAppointment(offenderBookingId, mainCreateAppointment)
      verify(prisonApiServiceAuditable).postAppointment(offenderBookingId, preCreateAppointment)
      verify(prisonApiServiceAuditable).postAppointment(offenderBookingId, postCreateAppointment)

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
        prisonId = "WRI"
      )
      theBooking.addMainAppointment(appointmentId = 40L, id = 2L, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(theBooking))
      whenever(prisonApiServiceAuditable.postAppointment(anyLong(), any())).thenReturn(Event(3L, "WRI", 10L, startDateTime, endDateTime))

      val updateSpecification = VideoLinkBookingUpdateSpecification(
        courtId = "TSTCRT",
        comment = "New Comment",
        main = VideoLinkAppointmentSpecification(
          locationId = 99L,
          startTime = referenceTime,
          endTime = referenceTime.plusMinutes(30)
        )
      )

      service.updateVideoLinkBooking(1L, updateSpecification)

      verify(prisonApiService).deleteAppointment(40L)
      verify(prisonApiServiceAuditable).postAppointment(
        30L,
        CreateBookingAppointment(
          appointmentType = "VLB",
          locationId = 99L,
          startTime = "2020-10-09T10:30",
          endTime = "2020-10-09T11:00",
          comment = "New Comment"
        )
      )

      val expectedAfterUpdate =
        VideoLinkBooking(id = 1L, offenderBookingId = 30L, courtName = null, courtId = "TSTCRT", madeByTheCourt = true, prisonId = "WRI")
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
              endTime = referenceNow.plusSeconds(1)
            )
          )
        )
      }
        .isInstanceOf(ValidationException::class.java)
        .hasMessage("Main appointment start time must be in the future.")
    }

    @Test
    fun `Happy path - update pre, main and post`() {
      val service = service()

      val theBooking =
        VideoLinkBooking(id = 1L, offenderBookingId = 30L, courtName = "The court", madeByTheCourt = true, prisonId = "WRI")
      theBooking.addPreAppointment(appointmentId = 40L, id = 2L, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
      theBooking.addMainAppointment(appointmentId = 41L, id = 3L, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
      theBooking.addPostAppointment(appointmentId = 42L, id = 4L, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(theBooking))
      whenever(prisonApiServiceAuditable.postAppointment(anyLong(), any())).thenReturn(Event(9999L, "WRI", 10L, startDateTime, endDateTime))

      service.updateVideoLinkBooking(
        1L,
        VideoLinkBookingUpdateSpecification(
          courtId = "TSTCRT",
          comment = "New Comment",
          pre = VideoLinkAppointmentSpecification(
            locationId = 99L,
            startTime = referenceTime,
            endTime = referenceTime.plusMinutes(30)
          ),
          main = VideoLinkAppointmentSpecification(
            locationId = 98L,
            startTime = referenceTime.plusMinutes(30),
            endTime = referenceTime.plusMinutes(60)
          ),
          post = VideoLinkAppointmentSpecification(
            locationId = 97L,
            startTime = referenceTime.plusMinutes(60),
            endTime = referenceTime.plusMinutes(90)
          )
        )
      )

      verify(prisonApiService).deleteAppointment(40L)
      verify(prisonApiService).deleteAppointment(41L)
      verify(prisonApiService).deleteAppointment(42L)

      verify(prisonApiServiceAuditable).postAppointment(
        30L,
        CreateBookingAppointment(
          appointmentType = "VLB",
          locationId = 99L,
          startTime = "2020-10-09T10:30",
          endTime = "2020-10-09T11:00",
          comment = "New Comment"
        )
      )

      verify(prisonApiServiceAuditable).postAppointment(
        30L,
        CreateBookingAppointment(
          appointmentType = "VLB",
          locationId = 98L,
          startTime = "2020-10-09T11:00",
          endTime = "2020-10-09T11:30",
          comment = "New Comment"
        )
      )

      verify(prisonApiServiceAuditable).postAppointment(
        30L,
        CreateBookingAppointment(
          appointmentType = "VLB",
          locationId = 97L,
          startTime = "2020-10-09T11:30",
          endTime = "2020-10-09T12:00",
          comment = "New Comment"
        )
      )

      assertThat(theBooking)
        .usingRecursiveComparison()
        .isEqualTo(
          VideoLinkBooking(id = 1L, offenderBookingId = 30L, courtName = null, courtId = "TSTCRT", madeByTheCourt = true, prisonId = "WRI").apply {
            addPreAppointment(appointmentId = 9999L, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
            addMainAppointment(appointmentId = 9999L, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
            addPostAppointment(appointmentId = 9999L, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
          }
        )
    }
  }

  @Nested
  inner class DeleteVideoLinkBooking {
    val service = service()

    private val mainAppointmentId = 12L
    private val preAppointmentId = 13L
    private val postAppointmentId = 14L

    private val videoLinkBooking = VideoLinkBooking(offenderBookingId = 1, courtName = COURT_NAME, id = 100, prisonId = "WRI").apply {
      addPreAppointment(appointmentId = preAppointmentId, id = 111, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
      addMainAppointment(appointmentId = mainAppointmentId, id = 222, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
      addPostAppointment(appointmentId = postAppointmentId, id = 333, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
    }

    @Test
    fun `Wen there is no video link booking it throws an exception`() {
      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.empty())
      Assertions.assertThrows(EntityNotFoundException::class.java) {
        service.deleteVideoLinkBooking(videoLinkBooking.id!!)
      }
    }

    @Test
    fun `Happy path`() {
      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(videoLinkBooking))

      service.deleteVideoLinkBooking(videoLinkBooking.id!!)

      verify(prisonApiService).deleteAppointment(preAppointmentId)
      verify(prisonApiService).deleteAppointment(mainAppointmentId)
      verify(prisonApiService).deleteAppointment(postAppointmentId)

      verify(videoLinkBookingRepository).deleteById(videoLinkBooking.id!!)
      verify(videoLinkBookingEventListener).bookingDeleted(videoLinkBooking)
    }
  }

  @Nested
  inner class GetVideoLinkBooking {
    val service = service()

    private val mainAppointmentId = 12L
    private val preAppointmentId = 13L
    private val postAppointmentId = 14L

    private val videoLinkBooking =
      VideoLinkBooking(offenderBookingId = 1, courtName = COURT_NAME, courtId = COURT_ID, id = 100, prisonId = "WRI").apply {
        addPreAppointment(appointmentId = preAppointmentId, id = 111, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
        addMainAppointment(appointmentId = mainAppointmentId, id = 222, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
        addPostAppointment(appointmentId = postAppointmentId, id = 333, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
      }

    private val preAppointment = PrisonAppointment(
      bookingId = 1,
      eventId = preAppointmentId,
      startTime = LocalDateTime.of(2020, 12, 2, 12, 0, 0),
      endTime = LocalDateTime.of(2020, 12, 2, 13, 0, 0),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 10,
      comment = "any comment"
    )

    private val mainAppointment = PrisonAppointment(
      bookingId = 1,
      eventId = mainAppointmentId,
      startTime = LocalDateTime.of(2020, 12, 2, 13, 0, 0),
      endTime = LocalDateTime.of(2020, 12, 2, 14, 0, 0),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 9,
      comment = "any comment"
    )

    private val postAppointment = PrisonAppointment(
      bookingId = 1,
      eventId = postAppointmentId,
      startTime = LocalDateTime.of(2020, 12, 2, 14, 0, 0),
      endTime = LocalDateTime.of(2020, 12, 2, 15, 0, 0),
      eventSubType = "VLB",
      agencyId = "WWI",
      eventLocationId = 5,
      comment = "any comment"
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
          comment = "any comment",
          pre = VideoLinkBookingResponse.LocationTimeslot(
            locationId = preAppointment.eventLocationId,
            startTime = preAppointment.startTime,
            endTime = preAppointment.endTime!!
          ),
          main = VideoLinkBookingResponse.LocationTimeslot(
            locationId = mainAppointment.eventLocationId,
            startTime = mainAppointment.startTime,
            endTime = mainAppointment.endTime!!
          ),
          post = VideoLinkBookingResponse.LocationTimeslot(
            locationId = postAppointment.eventLocationId,
            startTime = postAppointment.startTime,
            endTime = postAppointment.endTime!!
          )
        )
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
          comment = "any comment",
          main = VideoLinkBookingResponse.LocationTimeslot(
            locationId = mainAppointment.eventLocationId,
            startTime = mainAppointment.startTime,
            endTime = mainAppointment.endTime!!
          )
        )
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
  inner class GetVideoLinkBookingsForDateAndCourt {
    val service = service()
    val date: LocalDate = LocalDate.of(2020, 12, 25)

    @Test
    fun `No prison appointments, no VLBs`() {
      whenever(prisonApiService.getScheduledAppointments(anyString(), any())).thenReturn(listOf())
      whenever(
        videoLinkBookingRepository.findByAppointmentIdsAndHearingType(any(), eq(HearingType.MAIN), any(), any())
      ).thenReturn(listOf())

      val bookings = service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, null, null)
      assertThat(bookings).isEmpty()
      verify(prisonApiService).getScheduledAppointments("WWI", date)
    }

    @Test
    fun `Happy path`() {
      whenever(prisonApiService.getScheduledAppointments(anyString(), any()))
        .thenReturn(
          scheduledAppointments("VLB", "WWI", 1, 10) +
            scheduledAppointments("VLB", "WWI", 1000, 1010) +
            scheduledAppointments("VLB", "WWI", 2000, 2010) +
            scheduledAppointments("XXX", "WWI", 3000, 3010)
        )
      whenever(
        videoLinkBookingRepository.findByAppointmentIdsAndHearingType(
          any(),
          eq(HearingType.MAIN),
          isNull(),
          isNull()
        )
      )
        .thenReturn(videoLinkBookings("Wimbledon", null, 1, 10, 10L, startDateTime, endDateTime))

      val bookings = service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, null, null)
      assertThat(bookings)
        .extracting("main.locationId", "pre.locationId", "post.locationId")
        .containsExactlyInAnyOrder(
          tuple(1001L, 2001L, 3001L),
          tuple(1002L, 2002L, 3002L),
          tuple(1003L, 2003L, 3003L),
          tuple(1004L, 2004L, 3004L),
          tuple(1005L, 2005L, 3005L),
          tuple(1006L, 2006L, 3006L),
          tuple(1007L, 2007L, 3007L),
          tuple(1008L, 2008L, 3008L),
          tuple(1009L, 2009L, 3009L),
          tuple(1010L, 2010L, 3010L)
        )

      verify(videoLinkBookingRepository).findByAppointmentIdsAndHearingType(
        rangesAsList(
          (1L..10L),
          (1000L..1010L),
          (2000L..2010L)
        ),
        HearingType.MAIN
      )
    }

    private fun rangesAsList(vararg ranges: LongRange) = ranges.asList().flatMap { range -> range.map { it } }

    @Test
    fun `Happy path - filter appointment type`() {
      whenever(prisonApiService.getScheduledAppointments(anyString(), any()))
        .thenReturn(scheduledAppointments("NOWT", "WWI", 1, 10))

      whenever(videoLinkBookingRepository.findByAppointmentIdsAndHearingType(any(), eq(HearingType.MAIN), any(), any()))
        .thenReturn(emptyList())

      service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, null, null)

      verify(videoLinkBookingRepository).findByAppointmentIdsAndHearingType(emptyList(), HearingType.MAIN)
    }

    @Test
    fun `Appointments with missing end times are filtered out`() {
      whenever(prisonApiService.getScheduledAppointments(anyString(), any()))
        .thenReturn(
          listOf(
            ScheduledAppointmentDto(
              id = 1001,
              agencyId = "WEI",
              locationId = 1001,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 9, 40),
              endTime = LocalDateTime.of(2020, 1, 1, 10, 0),
              "A1234AA"
            ),
            ScheduledAppointmentDto(
              id = 1002,
              agencyId = "WEI",
              locationId = 1002,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 10, 0),
              endTime = LocalDateTime.of(2020, 1, 1, 10, 30),
              "B2345BB"
            ),
            ScheduledAppointmentDto(
              id = 1003,
              agencyId = "WEI",
              locationId = 1003,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 10, 30),
              endTime = null,
              "C3456CC"
            )
          )
        )

      whenever(
        videoLinkBookingRepository.findByAppointmentIdsAndHearingType(
          any(),
          eq(HearingType.MAIN),
          isNull(),
          isNull()
        )
      )
        .thenReturn(
          listOf(
            VideoLinkBooking(
              id = 1000L,
              offenderBookingId = 1000,
              courtName = "Wimbledon",
              prisonId = "WEI"
            ).apply {
              addPreAppointment(id = 10001, appointmentId = 1001, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
              addMainAppointment(id = 20000, appointmentId = 1002, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
              addPostAppointment(id = 30000, appointmentId = 1003, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
            }
          )
        )

      val bookings = service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, null, null)
      assertThat(bookings).hasSize(1)
      assertThat(bookings[0].pre).isNotNull
      assertThat(bookings[0].main).isNotNull
      assertThat(bookings[0].post).isNull()
    }

    @Test
    fun `Filter by courtName`() {
      whenever(prisonApiService.getScheduledAppointments(anyString(), any())).thenReturn(emptyList())
      whenever(
        videoLinkBookingRepository.findByAppointmentIdsAndHearingType(
          any(),
          eq(HearingType.MAIN),
          any(),
          isNull()
        )
      )
        .thenReturn(emptyList())

      service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, "The court", null)
      verify(videoLinkBookingRepository).findByAppointmentIdsAndHearingType(
        emptyList(),
        HearingType.MAIN,
        "The court",
        null
      )
    }

    @Test
    fun `Filter by courtId`() {
      whenever(prisonApiService.getScheduledAppointments(anyString(), any())).thenReturn(emptyList())
      whenever(
        videoLinkBookingRepository.findByAppointmentIdsAndHearingType(
          any(),
          eq(HearingType.MAIN),
          isNull(),
          any()
        )
      )
        .thenReturn(emptyList())

      service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, null, "COURTID")
      verify(videoLinkBookingRepository).findByAppointmentIdsAndHearingType(
        emptyList(),
        HearingType.MAIN,
        null,
        "COURTID"
      )
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
              addPreAppointment(id = 100L, appointmentId = 10L, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
              addMainAppointment(id = 101L, appointmentId = 11L, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
              addPostAppointment(id = 102L, appointmentId = 12L, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
            }
          )
        )

      service.updateVideoLinkBookingComment(1L, newComment)

      verify(prisonApiService).updateAppointmentComment(10L, newComment)
      verify(prisonApiService).updateAppointmentComment(11L, newComment)
      verify(prisonApiService).updateAppointmentComment(12L, newComment)
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
              addMainAppointment(id = 101L, appointmentId = 11L, locationId = 10L, startDateTime = startDateTime, endDateTime = endDateTime)
            }
          )
        )

      service.updateVideoLinkBookingComment(1L, newComment)

      verify(prisonApiService).updateAppointmentComment(11L, newComment)
      verifyNoMoreInteractions(prisonApiService)
    }
  }

  private fun service() = VideoLinkBookingService(
    courtService,
    prisonApiService,
    prisonApiServiceAuditable,
    videoLinkAppointmentRepository,
    videoLinkBookingRepository,
    clock,
    videoLinkBookingEventListener
  )

  companion object {
    const val COURT_NAME = "York Crown Court"
    const val COURT_ID = "YRKCC"
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
      operationalCapacity = null
    )

    fun scheduledAppointments(
      type: String,
      agencyId: String,
      firstId: Long = 1L,
      lastId: Long = 30L
    ): List<ScheduledAppointmentDto> =
      (firstId..lastId).map {
        ScheduledAppointmentDto(
          id = it,
          agencyId = agencyId,
          locationId = it + 1000,
          appointmentTypeCode = type,
          startTime = LocalDateTime.of(2020, 1, 1, 0, 0).plusHours(it),
          endTime = LocalDateTime.of(2020, 1, 1, 0, 0).plusHours(it + 1),
          offenderNo = "A1234AA"
        )
      }

    fun videoLinkBookings(
      court: String?,
      courtId: String?,
      firstAppointmentId: Long = 1L,
      lastAppointmentId: Long = 30L,
      locationId: Long,
      startDateTime: LocalDateTime,
      endDateTime: LocalDateTime
    ): List<VideoLinkBooking> =
      (firstAppointmentId..lastAppointmentId).map {
        VideoLinkBooking(id = it + 1000L, offenderBookingId = it + 1000, courtName = court, courtId = courtId, prisonId = "WWI").apply {
          addPreAppointment(id = it + 10000, appointmentId = it + 1000, locationId = locationId, startDateTime = startDateTime, endDateTime = endDateTime)
          addMainAppointment(id = it + 20000, appointmentId = it, locationId = locationId, startDateTime = startDateTime, endDateTime = endDateTime)
          addPostAppointment(id = it + 30000, appointmentId = it + 2000, locationId = locationId, startDateTime = startDateTime, endDateTime = endDateTime)
        }
      }
  }
}
