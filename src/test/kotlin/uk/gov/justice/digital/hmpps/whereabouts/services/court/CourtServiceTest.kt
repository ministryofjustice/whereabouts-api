package uk.gov.justice.digital.hmpps.whereabouts.services.court

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
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
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.LocationDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.ValidationException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional
import javax.persistence.EntityNotFoundException

const val YORK_CC = "York Crown Court"
const val VLB_APPOINTMENT_TYPE = "VLB"
const val AGENCY_WANDSWORTH = "WWI"

class CourtServiceTest {

  private val prisonApiService: PrisonApiService = mock()
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository = mock()
  private val videoLinkBookingRepository: VideoLinkBookingRepository = mock()
  private val videoLinkBookingEventListener: VideoLinkBookingEventListener = mock()
  private val clock: Clock = Clock.fixed(Instant.parse("2020-10-01T00:00:00Z"), ZoneId.of("UTC"))

  @Test
  fun `should return NO video link appointments`() {
    val service = service("", "")
    val appointments = service.getVideoLinkAppointments(setOf(1, 2))

    verify(videoLinkAppointmentRepository).findVideoLinkAppointmentByAppointmentIdIn(setOf(1, 2))
    assertThat(appointments).isEmpty()
  }

  @Test
  fun `should return and map video link appointments`() {
    whenever(videoLinkAppointmentRepository.findVideoLinkAppointmentByAppointmentIdIn(setOf(3, 4))).thenReturn(
      setOf(
        VideoLinkAppointment(id = 1, bookingId = 2, appointmentId = 3, hearingType = HearingType.MAIN, court = "YORK"),
        VideoLinkAppointment(
          id = 2,
          bookingId = 3,
          appointmentId = 4,
          hearingType = HearingType.PRE,
          court = "YORK",
          madeByTheCourt = false
        )
      )
    )
    val service = service("", "")
    val appointments = service.getVideoLinkAppointments(setOf(3, 4))

    assertThat(appointments)
      .extracting("id", "bookingId", "appointmentId", "hearingType", "court", "madeByTheCourt")
      .containsExactlyInAnyOrder(
        Tuple.tuple(1L, 2L, 3L, HearingType.MAIN, "YORK", true),
        Tuple.tuple(2L, 3L, 4L, HearingType.PRE, "YORK", false)
      )
  }

  @Nested
  inner class CreateVideoLinkBooking {
    val service = service("", "")

    private val referenceTime = LocalDateTime
      .now(clock)
      .plusDays(8)
      .plusHours(10)
      .plusMinutes(30)

    private val referenceNow = LocalDateTime.now(clock)

    private val mainAppointmentId = 12L
    private val mainVideoLinkAppointment = VideoLinkAppointment(
      appointmentId = mainAppointmentId,
      bookingId = 1,
      court = YORK_CC,
      hearingType = HearingType.MAIN
    )

    private val preAppointmentId = 13L
    private val preVideoLinkAppointment = VideoLinkAppointment(
      appointmentId = preAppointmentId,
      bookingId = 1,
      court = YORK_CC,
      hearingType = HearingType.PRE
    )

    private val postAppointmentId = 14L
    private val postVideoLinkAppointment = VideoLinkAppointment(
      appointmentId = postAppointmentId,
      bookingId = 1,
      court = YORK_CC,
      hearingType = HearingType.POST
    )

    private val expectedVideoLinkBookingId = 11L

    @BeforeEach
    fun stubLocations() {
      whenever(prisonApiService.getLocation(anyLong())).thenAnswer { locationDto(it.arguments[0] as Long) }
    }

    @Test
    fun `happy flow - Main appointment only - madeByTheCourt`() {

      val specification = VideoLinkBookingSpecification(
        bookingId = 1L,
        court = YORK_CC,
        comment = "Comment",
        madeByTheCourt = true,
        main = VideoLinkAppointmentSpecification(
          locationId = 2L,
          startTime = referenceTime,
          endTime = referenceTime.plusMinutes(30)
        )
      )

      val booking = VideoLinkBooking(
        id = expectedVideoLinkBookingId,
        main = mainVideoLinkAppointment
      )

      whenever(prisonApiService.postAppointment(anyLong(), any())).thenReturn(
        Event(
          mainAppointmentId,
          AGENCY_WANDSWORTH
        )
      )
      whenever(videoLinkBookingRepository.save(any())).thenReturn(booking)

      val vlbBookingId = service.createVideoLinkBooking(specification)

      assertThat(vlbBookingId).isEqualTo(expectedVideoLinkBookingId)

      verify(prisonApiService).postAppointment(
        1L,
        CreateBookingAppointment(
          appointmentType = VLB_APPOINTMENT_TYPE,
          locationId = 2L,
          comment = "Comment",
          startTime = "2020-10-09T10:30",
          endTime = "2020-10-09T11:00"
        )
      )

      verify(videoLinkBookingRepository).save(booking.copy(id = null))
      verify(videoLinkBookingEventListener).bookingCreated(booking, specification, AGENCY_WANDSWORTH)
    }

    @Test
    fun `Validation failure - Main appointment starts in past`() {
      assertThatThrownBy {
        service.createVideoLinkBooking(
          VideoLinkBookingSpecification(
            bookingId = 1L,
            court = YORK_CC,
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
            court = YORK_CC,
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
            court = YORK_CC,
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
            court = YORK_CC,
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
            court = YORK_CC,
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
            court = YORK_CC,
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
            court = YORK_CC,
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
    fun `happy flow - pre, main and post appointments - Not madeByTheCourt`() {

      val offenderBookingId = 1L

      val booking = VideoLinkBooking(
        id = expectedVideoLinkBookingId,
        pre = preVideoLinkAppointment.copy(madeByTheCourt = false, id = 20L),
        main = mainVideoLinkAppointment.copy(madeByTheCourt = false, id = 21L),
        post = postVideoLinkAppointment.copy(madeByTheCourt = false, id = 22L)
      )

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

      whenever(prisonApiService.postAppointment(offenderBookingId, mainCreateAppointment)).thenReturn(
        Event(
          mainAppointmentId,
          AGENCY_WANDSWORTH
        )
      )
      whenever(prisonApiService.postAppointment(offenderBookingId, preCreateAppointment)).thenReturn(
        Event(
          preAppointmentId,
          AGENCY_WANDSWORTH
        )
      )
      whenever(prisonApiService.postAppointment(offenderBookingId, postCreateAppointment)).thenReturn(
        Event(
          postAppointmentId,
          AGENCY_WANDSWORTH
        )
      )

      whenever(videoLinkBookingRepository.save(any())).thenReturn(booking)

      val vlbBookingId = service.createVideoLinkBooking(
        VideoLinkBookingSpecification(
          bookingId = offenderBookingId,
          court = YORK_CC,
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

      verify(prisonApiService).postAppointment(offenderBookingId, mainCreateAppointment)
      verify(prisonApiService).postAppointment(offenderBookingId, preCreateAppointment)
      verify(prisonApiService).postAppointment(offenderBookingId, postCreateAppointment)

      verify(videoLinkBookingRepository).save(
        booking.copy(
          id = null,
          pre = booking.pre?.copy(id = null),
          main = booking.main.copy(id = null),
          post = booking.post?.copy(id = null)
        )
      )
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
    fun `Happy flow - update main`() {
      val service = service("", "")

      val theBooking = VideoLinkBooking(
        id = 1L,
        main = VideoLinkAppointment(
          id = 2L,
          bookingId = 30L,
          appointmentId = 40L,
          court = "The court",
          hearingType = HearingType.MAIN,
          madeByTheCourt = true
        )
      )

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(theBooking))
      whenever(prisonApiService.postAppointment(anyLong(), any())).thenReturn(Event(3L, "WRI"))

      val updateSpecification = VideoLinkBookingUpdateSpecification(
        comment = "New Comment",
        main = VideoLinkAppointmentSpecification(
          locationId = 99L,
          startTime = referenceTime,
          endTime = referenceTime.plusMinutes(30),
        )
      )

      service.updateVideoLinkBooking(1L, updateSpecification)

      verify(prisonApiService).deleteAppointment(40L)
      verify(prisonApiService).postAppointment(
        30L,
        CreateBookingAppointment(
          appointmentType = "VLB",
          locationId = 99L,
          startTime = "2020-10-09T10:30",
          endTime = "2020-10-09T11:00",
          comment = "New Comment"
        )
      )

      val expectedAfterUpdate = VideoLinkBooking(
        id = 1L,
        main = VideoLinkAppointment(
          bookingId = 30L,
          appointmentId = 3L,
          court = "The court",
          hearingType = HearingType.MAIN,
          madeByTheCourt = true
        )
      )

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
      val service = service("", "")
      assertThatThrownBy {
        service.updateVideoLinkBooking(
          1L,
          VideoLinkBookingUpdateSpecification(
            comment = "New Comment",
            main = VideoLinkAppointmentSpecification(
              locationId = 99L,
              startTime = referenceNow.minusSeconds(1),
              endTime = referenceNow.plusSeconds(1),
            )
          )
        )
      }
        .isInstanceOf(ValidationException::class.java)
        .hasMessage("Main appointment start time must be in the future.")
    }

    @Test
    fun `Happy flow - update pre, main and post`() {
      val service = service("", "")

      val theBooking = VideoLinkBooking(
        id = 1L,
        pre = VideoLinkAppointment(
          id = 2L,
          bookingId = 30L,
          appointmentId = 40L,
          court = "The court",
          hearingType = HearingType.PRE,
          madeByTheCourt = true
        ),
        main = VideoLinkAppointment(
          id = 3L,
          bookingId = 30L,
          appointmentId = 41L,
          court = "The court",
          hearingType = HearingType.MAIN,
          madeByTheCourt = true
        ),
        post = VideoLinkAppointment(
          id = 4L,
          bookingId = 30L,
          appointmentId = 42L,
          court = "The court",
          hearingType = HearingType.POST,
          madeByTheCourt = true
        )
      )

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(theBooking))
      whenever(prisonApiService.postAppointment(anyLong(), any())).thenReturn(Event(3L, "WRI"))

      service.updateVideoLinkBooking(
        1L,
        VideoLinkBookingUpdateSpecification(
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
          )
        )
      )

      verify(prisonApiService).deleteAppointment(40L)
      verify(prisonApiService).deleteAppointment(41L)
      verify(prisonApiService).deleteAppointment(42L)

      verify(prisonApiService).postAppointment(
        30L,
        CreateBookingAppointment(
          appointmentType = "VLB",
          locationId = 99L,
          startTime = "2020-10-09T10:30",
          endTime = "2020-10-09T11:00",
          comment = "New Comment"
        )
      )

      verify(prisonApiService).postAppointment(
        30L,
        CreateBookingAppointment(
          appointmentType = "VLB",
          locationId = 98L,
          startTime = "2020-10-09T11:00",
          endTime = "2020-10-09T11:30",
          comment = "New Comment"
        )
      )

      verify(prisonApiService).postAppointment(
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
          VideoLinkBooking(
            id = 1L,
            pre = VideoLinkAppointment(
              bookingId = 30L,
              appointmentId = 3L,
              court = "The court",
              hearingType = HearingType.PRE,
              madeByTheCourt = true
            ),
            main = VideoLinkAppointment(
              bookingId = 30L,
              appointmentId = 3L,
              court = "The court",
              hearingType = HearingType.MAIN,
              madeByTheCourt = true
            ),
            post = VideoLinkAppointment(
              bookingId = 30L,
              appointmentId = 3L,
              court = "The court",
              hearingType = HearingType.POST,
              madeByTheCourt = true
            )
          )
        )
    }
  }

  @Nested
  inner class DeleteVideoLinkBooking {
    val service = service("", "")

    private val mainAppointmentId = 12L
    private val mainVideoLinkAppointment = VideoLinkAppointment(
      id = 222,
      appointmentId = mainAppointmentId,
      bookingId = 1,
      court = YORK_CC,
      hearingType = HearingType.MAIN
    )

    private val preAppointmentId = 13L
    private val preVideoLinkAppointment = VideoLinkAppointment(
      id = 111,
      appointmentId = preAppointmentId,
      bookingId = 1,
      court = YORK_CC,
      hearingType = HearingType.PRE
    )

    private val postAppointmentId = 14L
    private val postVideoLinkAppointment = VideoLinkAppointment(
      id = 333,
      appointmentId = postAppointmentId,
      bookingId = 1,
      court = YORK_CC,
      hearingType = HearingType.POST
    )

    private val videoLinkBooking = VideoLinkBooking(
      pre = preVideoLinkAppointment,
      main = mainVideoLinkAppointment,
      post = postVideoLinkAppointment,
      id = 100
    )

    @Test
    fun `when there is no video link booking it throws an exception`() {

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.empty())
      Assertions.assertThrows(EntityNotFoundException::class.java) {
        service.deleteVideoLinkBooking(videoLinkBooking.id!!)
      }
    }

    @Test
    fun `happy path`() {

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(videoLinkBooking))

      service.deleteVideoLinkBooking(videoLinkBooking.id!!)

      verify(prisonApiService).deleteAppointment(preVideoLinkAppointment.appointmentId)
      verify(prisonApiService).deleteAppointment(mainVideoLinkAppointment.appointmentId)
      verify(prisonApiService).deleteAppointment(postVideoLinkAppointment.appointmentId)

      verify(videoLinkBookingRepository).deleteById(videoLinkBooking.id!!)
      verify(videoLinkBookingEventListener).bookingDeleted(videoLinkBooking)
    }
  }

  @Nested
  inner class GetVideoLinkBooking {
    val service = service("", "")

    private val mainAppointmentId = 12L
    private val mainVideoLinkAppointment = VideoLinkAppointment(
      id = 222,
      appointmentId = mainAppointmentId,
      bookingId = 1,
      court = YORK_CC,
      hearingType = HearingType.MAIN
    )

    private val preAppointmentId = 13L
    private val preVideoLinkAppointment = VideoLinkAppointment(
      id = 111,
      appointmentId = preAppointmentId,
      bookingId = 1,
      court = YORK_CC,
      hearingType = HearingType.PRE
    )

    private val postAppointmentId = 14L
    private val postVideoLinkAppointment = VideoLinkAppointment(
      id = 333,
      appointmentId = postAppointmentId,
      bookingId = 1,
      court = YORK_CC,
      hearingType = HearingType.POST
    )

    private val videoLinkBooking = VideoLinkBooking(
      pre = preVideoLinkAppointment,
      main = mainVideoLinkAppointment,
      post = postVideoLinkAppointment,
      id = 100
    )

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
      comment = "any comment"
    )

    @Test
    fun `when there is no video link booking it throws an exception`() {

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.empty())
      Assertions.assertThrows(EntityNotFoundException::class.java) {
        service.getVideoLinkBooking(videoLinkBooking.id!!)
      }
    }

    @Test
    fun `when there is a video link booking with pre, main and post`() {

      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(videoLinkBooking))

      whenever(prisonApiService.getPrisonAppointment(videoLinkBooking.main.appointmentId)).thenReturn(mainAppointment)
      whenever(prisonApiService.getPrisonAppointment(videoLinkBooking.pre!!.appointmentId)).thenReturn(preAppointment)
      whenever(prisonApiService.getPrisonAppointment(videoLinkBooking.post!!.appointmentId)).thenReturn(postAppointment)
      val result = service.getVideoLinkBooking(videoLinkBooking.id!!)

      assertThat(result).isEqualTo(
        VideoLinkBookingResponse(
          videoLinkBookingId = 100,
          bookingId = 1,
          agencyId = "WWI",
          court = mainVideoLinkAppointment.court,
          comment = "any comment",
          pre = VideoLinkBookingResponse.LocationTimeslot(
            locationId = preAppointment.eventLocationId,
            startTime = preAppointment.startTime,
            endTime = preAppointment.endTime
          ),
          main = VideoLinkBookingResponse.LocationTimeslot(
            locationId = mainAppointment.eventLocationId,
            startTime = mainAppointment.startTime,
            endTime = mainAppointment.endTime
          ),
          post = VideoLinkBookingResponse.LocationTimeslot(
            locationId = postAppointment.eventLocationId,
            startTime = postAppointment.startTime,
            endTime = postAppointment.endTime
          ),
        )
      )
    }

    @Test
    fun `when there is a video link booking with main appointment only`() {
      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(videoLinkBooking))
      whenever(prisonApiService.getPrisonAppointment(videoLinkBooking.main.appointmentId)).thenReturn(mainAppointment)
      whenever(prisonApiService.getPrisonAppointment(videoLinkBooking.pre!!.appointmentId)).thenReturn(null)
      whenever(prisonApiService.getPrisonAppointment(videoLinkBooking.post!!.appointmentId)).thenReturn(null)
      val result = service.getVideoLinkBooking(videoLinkBooking.id!!)
      assertThat(result).isEqualTo(
        VideoLinkBookingResponse(
          videoLinkBookingId = 100,
          bookingId = 1,
          agencyId = "WWI",
          court = mainVideoLinkAppointment.court,
          comment = "any comment",
          main = VideoLinkBookingResponse.LocationTimeslot(
            locationId = mainAppointment.eventLocationId,
            startTime = mainAppointment.startTime,
            endTime = mainAppointment.endTime
          ),
        )
      )
    }

    @Test
    fun `when there is a video link booking with pre and post appointments and no main appointment`() {
      whenever(videoLinkBookingRepository.findById(anyLong())).thenReturn(Optional.of(videoLinkBooking))
      whenever(prisonApiService.getPrisonAppointment(videoLinkBooking.main.appointmentId)).thenReturn(null)
      whenever(prisonApiService.getPrisonAppointment(videoLinkBooking.pre!!.appointmentId)).thenReturn(preAppointment)
      whenever(prisonApiService.getPrisonAppointment(videoLinkBooking.post!!.appointmentId)).thenReturn(postAppointment)
      Assertions.assertThrows(EntityNotFoundException::class.java) {
        service.getVideoLinkBooking(videoLinkBooking.id!!)
      }
    }
  }

  @Nested
  inner class GetVideoLinkBookingsForDateAndCourt {
    val service = service("", "")
    val date: LocalDate = LocalDate.of(2020, 12, 25)

    @Test
    fun `no prison appointments, no VLBs`() {
      whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any(), anyOrNull(), anyOrNull())).thenReturn(listOf())
      whenever(videoLinkBookingRepository.findByMainAppointmentIds(any())).thenReturn(listOf())

      val bookings = service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, null)
      assertThat(bookings).isEmpty()
      verify(prisonApiService).getScheduledAppointmentsByAgencyAndDate("WWI", date, null, null)
    }

    @Test
    fun `happy flow`() {
      whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any(), anyOrNull(), anyOrNull()))
        .thenReturn(
          scheduledAppointments("VLB", "WWI", 1, 10) +
            scheduledAppointments("VLB", "WWI", 1000, 1010) +
            scheduledAppointments("VLB", "WWI", 2000, 2010)
        )
      whenever(videoLinkBookingRepository.findByMainAppointmentIds(any()))
        .thenReturn(videoLinkBookings("Wimbledon", 1, 10))

      val bookings = service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, null)
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
          tuple(1010L, 2010L, 3010L),
        )

      verify(videoLinkBookingRepository).findByMainAppointmentIds(
        rangesAsList(
          (1L..10L),
          (1000L..1010L),
          (2000L..2010L)
        )
      )
    }

    private fun rangesAsList(vararg ranges: LongRange) = ranges.asList().flatMap { range -> range.map { it } }

    @Test
    fun `happy flow - filter by court`() {
      whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any(), anyOrNull(), anyOrNull()))
        .thenReturn(
          scheduledAppointments("VLB", "WWI", 1, 10)
        )
      whenever(videoLinkBookingRepository.findByMainAppointmentIds(any()))
        .thenReturn(
          videoLinkBookings("Wimbledon", 1, 5) +
            videoLinkBookings("Windsor", 6, 10)
        )

      val bookings = service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, "Wimbledon")
      assertThat(bookings)
        .extracting("main.locationId").containsExactlyInAnyOrder(1001L, 1002L, 1003L, 1004L, 1005L)
    }

    @Test
    fun `happy flow - filter appointment type`() {
      whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any(), anyOrNull(), anyOrNull()))
        .thenReturn(scheduledAppointments("NOWT", "WWI", 1, 10))
      whenever(videoLinkBookingRepository.findByMainAppointmentIds(any()))
        .thenReturn(videoLinkBookings("Wimbledon", 1, 10))

      val bookings = service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, null)
      assertThat(bookings).isEmpty()
    }

    @Test
    fun `appointments with missing end times are filtered out`() {
      whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any(), anyOrNull(), anyOrNull()))
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
      whenever(videoLinkBookingRepository.findByMainAppointmentIds(any()))
        .thenReturn(
          listOf(
            VideoLinkBooking(
              id = 1000L,
              pre = VideoLinkAppointment(
                id = 10001,
                bookingId = 1000,
                appointmentId = 1001,
                court = "Wimbledon",
                hearingType = HearingType.PRE
              ),
              main = VideoLinkAppointment(
                id = 20000,
                bookingId = 1000,
                appointmentId = 1002,
                court = "Wimbledon",
                hearingType = HearingType.MAIN
              ),
              post = VideoLinkAppointment(
                id = 30000,
                bookingId = 1000,
                appointmentId = 1003,
                court = "Wimbledon",
                hearingType = HearingType.POST
              )
            )
          )
        )

      val bookings = service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, "Wimbledon")
      assertThat(bookings).hasSize(1)
      assertThat(bookings[0].pre).isNotNull
      assertThat(bookings[0].main).isNotNull
      assertThat(bookings[0].post).isNull()
    }

    @Test
    fun `bookings with with missing end times on main appointment are filtered out entirely`() {
      whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any(), anyOrNull(), anyOrNull()))
        .thenReturn(
          listOf(
            ScheduledAppointmentDto(
              id = 1001,
              agencyId = "WEI",
              locationId = 1001,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 9, 40),
              endTime = LocalDateTime.of(2020, 1, 1, 10, 0),
              offenderNo = "A1234AA"
            ),
            ScheduledAppointmentDto(
              id = 1002,
              agencyId = "WEI",
              locationId = 1002,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 10, 0),
              endTime = null,
              offenderNo = "B2345BB"
            ),
            ScheduledAppointmentDto(
              id = 1003,
              agencyId = "WEI",
              locationId = 1003,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 10, 30),
              endTime = null,
              offenderNo = "C3456CC"
            )
          )
        )
      whenever(videoLinkBookingRepository.findByMainAppointmentIds(any()))
        .thenReturn(
          listOf(
            VideoLinkBooking(
              id = 1000L,
              pre = VideoLinkAppointment(
                id = 10001,
                bookingId = 1000,
                appointmentId = 1001,
                court = "Wimbledon",
                hearingType = HearingType.PRE
              ),
              main = VideoLinkAppointment(
                id = 20000,
                bookingId = 1000,
                appointmentId = 1002,
                court = "Wimbledon",
                hearingType = HearingType.MAIN
              ),
              post = VideoLinkAppointment(
                id = 30000,
                bookingId = 1000,
                appointmentId = 1003,
                court = "Wimbledon",
                hearingType = HearingType.POST
              )
            )
          )
        )

      val bookings = service.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, "Wimbledon")
      assertThat(bookings).isEmpty()
    }
  }

  @Nested
  inner class UpdateVideoLinkBookingComment {

    @Test
    fun `Happy flow - Pre, main and post appointments`() {
      val service = service("", "")
      val newComment = "New comment"

      whenever(videoLinkBookingRepository.findById(1L))
        .thenReturn(
          Optional.of(
            VideoLinkBooking(
              id = 1L,
              pre = VideoLinkAppointment(
                id = 100L,
                appointmentId = 10L,
                bookingId = 999L,
                hearingType = HearingType.PRE,
                court = "The Court"
              ),
              main = VideoLinkAppointment(
                id = 101L,
                appointmentId = 11L,
                bookingId = 999L,
                hearingType = HearingType.MAIN,
                court = "The Court"
              ),
              post = VideoLinkAppointment(
                id = 102L,
                appointmentId = 12L,
                bookingId = 999L,
                hearingType = HearingType.POST,
                court = "The Court"
              ),
            )
          )
        )

      service.updateVideoLinkBookingComment(1L, newComment)

      verify(prisonApiService).updateAppointmentComment(10L, newComment)
      verify(prisonApiService).updateAppointmentComment(11L, newComment)
      verify(prisonApiService).updateAppointmentComment(12L, newComment)
      verifyNoMoreInteractions(prisonApiService)
    }

    @Test
    fun `Happy flow - main appointment only, no comment`() {
      val service = service("", "")
      val newComment = ""

      whenever(videoLinkBookingRepository.findById(1L))
        .thenReturn(
          Optional.of(
            VideoLinkBooking(
              id = 1L,
              main = VideoLinkAppointment(
                id = 101L,
                appointmentId = 11L,
                bookingId = 999L,
                hearingType = HearingType.MAIN,
                court = "The Court"
              ),
            )
          )
        )

      service.updateVideoLinkBookingComment(1L, newComment)

      verify(prisonApiService).updateAppointmentComment(11L, newComment)
      verifyNoMoreInteractions(prisonApiService)
    }
  }

  private fun service(courts: String, courtIds: String) = CourtService(
    prisonApiService,
    videoLinkAppointmentRepository,
    videoLinkBookingRepository,
    clock,
    videoLinkBookingEventListener,
    courts,
    courtIds
  )

  companion object {
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
      court: String,
      firstAppointmentId: Long = 1L,
      lastAppointmentId: Long = 30L
    ): List<VideoLinkBooking> =
      (firstAppointmentId..lastAppointmentId).map {
        VideoLinkBooking(
          id = it + 1000L,
          pre = VideoLinkAppointment(
            id = it + 10000,
            bookingId = it + 1000,
            appointmentId = it + 1000,
            court = court,
            hearingType = HearingType.PRE
          ),
          main = VideoLinkAppointment(
            id = it + 20000,
            bookingId = it + 1000,
            appointmentId = it,
            court = court,
            hearingType = HearingType.MAIN
          ),
          post = VideoLinkAppointment(
            id = it + 30000,
            bookingId = it + 1000,
            appointmentId = it + 2000,
            court = court,
            hearingType = HearingType.POST
          )
        )
      }
  }
}
