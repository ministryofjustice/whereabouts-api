package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateBookingAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateVideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.main
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.time.LocalDateTime
import java.util.Optional
import javax.persistence.EntityNotFoundException

const val YORK_CC = "York Crown Court"
const val VLB_APPOINTMENT_TYPE = "VLB"
const val AGENCY_WANDSWORTH = "WWI"

class CourtServiceTest {

  private val prisonApiService: PrisonApiService = mock()
  private val authenticationFacade: AuthenticationFacade = mock()
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository = mock()
  private val videoLinkBookingRepository: VideoLinkBookingRepository = mock()
  private val telemetryClient: TelemetryClient = mock()

  @Test
  fun `should push an appointment to elite2`() {
    val service = service(YORK_CC)
    val bookingId: Long = 1

    whenever(prisonApiService.postAppointment(anyLong(), any())).thenReturn(Event(1L, AGENCY_WANDSWORTH))

    service.createVideoLinkAppointment(
      CreateVideoLinkAppointment(
        bookingId = bookingId,
        locationId = 1,
        comment = "test",
        startTime = LocalDateTime.of(2019, 10, 10, 10, 0),
        endTime = LocalDateTime.of(2019, 10, 10, 11, 0),
        court = YORK_CC
      )
    )

    verify(prisonApiService).postAppointment(
      bookingId,
      CreateBookingAppointment(
        appointmentType = VLB_APPOINTMENT_TYPE,
        locationId = 1,
        comment = "test",
        startTime = "2019-10-10T10:00",
        endTime = "2019-10-10T11:00"
      )
    )
  }

  @Test
  fun `should create video link appointment using the event id returned from elite2`() {
    val service = service(YORK_CC)
    val bookingId: Long = 1

    whenever(prisonApiService.postAppointment(anyLong(), any())).thenReturn(Event(1L, AGENCY_WANDSWORTH))
    whenever(authenticationFacade.currentUsername).thenReturn("username1")

    service.createVideoLinkAppointment(
      CreateVideoLinkAppointment(
        bookingId = bookingId,
        locationId = 1,
        comment = "test",
        startTime = LocalDateTime.of(2019, 10, 10, 10, 0),
        endTime = LocalDateTime.of(2019, 10, 10, 11, 0),
        court = YORK_CC
      )
    )

    verify(videoLinkAppointmentRepository).save(
      VideoLinkAppointment(
        appointmentId = 1,
        court = YORK_CC,
        bookingId = bookingId,
        hearingType = HearingType.MAIN,
        createdByUsername = "username1"
      )
    )
  }

  @Test
  fun `should return NO video link appointments`() {
    val service = service("")
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
    val service = service("")
    val appointments = service.getVideoLinkAppointments(setOf(3, 4))

    assertThat(appointments)
      .extracting("id", "bookingId", "appointmentId", "hearingType", "court", "madeByTheCourt")
      .containsExactlyInAnyOrder(
        Tuple.tuple(1L, 2L, 3L, HearingType.MAIN, "YORK", true),
        Tuple.tuple(2L, 3L, 4L, HearingType.PRE, "YORK", false)
      )
  }

  @Test
  fun `should record if the appointment was made by the court`() {
    val service = service(YORK_CC)
    val bookingId: Long = 1

    whenever(prisonApiService.postAppointment(anyLong(), any())).thenReturn(Event(1L, AGENCY_WANDSWORTH))
    whenever(authenticationFacade.currentUsername).thenReturn("username1")

    service.createVideoLinkAppointment(
      CreateVideoLinkAppointment(
        bookingId = bookingId,
        locationId = 1,
        comment = "test",
        startTime = LocalDateTime.of(2019, 10, 10, 10, 0),
        endTime = LocalDateTime.of(2019, 10, 10, 11, 0),
        court = YORK_CC
      )
    )

    verify(videoLinkAppointmentRepository).save(
      VideoLinkAppointment(
        appointmentId = 1,
        court = YORK_CC,
        bookingId = bookingId,
        hearingType = HearingType.MAIN,
        createdByUsername = "username1",
        madeByTheCourt = true
      )
    )
  }

  @Test
  fun `should record if the appointment was made by the prison on behalf of the court`() {
    val service = service(YORK_CC)
    val bookingId: Long = 1

    whenever(prisonApiService.postAppointment(anyLong(), any())).thenReturn(Event(1L, AGENCY_WANDSWORTH))
    whenever(authenticationFacade.currentUsername).thenReturn("username1")

    service.createVideoLinkAppointment(
      CreateVideoLinkAppointment(
        bookingId = bookingId,
        locationId = 1,
        comment = "test",
        startTime = LocalDateTime.of(2019, 10, 10, 10, 0),
        endTime = LocalDateTime.of(2019, 10, 10, 11, 0),
        court = YORK_CC,
        madeByTheCourt = false
      )
    )

    verify(videoLinkAppointmentRepository).save(
      VideoLinkAppointment(
        appointmentId = 1,
        court = YORK_CC,
        bookingId = bookingId,
        hearingType = HearingType.MAIN,
        createdByUsername = "username1",
        madeByTheCourt = false
      )
    )
  }

  @Nested
  inner class CreateVideoLinkBooking {
    val service = service("")

    private val startTime = LocalDateTime.of(2020, 10, 9, 10, 30)

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

    @Test
    fun `happy flow - Main appointment only - madeByTheCourt`() {

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

      val vlbBookingId = service.createVideoLinkBooking(
        VideoLinkBookingSpecification(
          bookingId = 1L,
          court = YORK_CC,
          comment = "Comment",
          madeByTheCourt = true,
          main = VideoLinkAppointmentSpecification(
            locationId = 2L,
            startTime = startTime,
            endTime = startTime.plusMinutes(30)
          )
        )
      )

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
      verifyNoMoreInteractions(prisonApiService)

      verify(videoLinkBookingRepository).save(booking.copy(id = null))
    }

    @Test
    fun `happy flow - telemetry`() {

      val endTime = startTime.plusMinutes(20)

      whenever(prisonApiService.postAppointment(anyLong(), any())).thenReturn(
        Event(
          mainAppointmentId,
          AGENCY_WANDSWORTH
        )
      )
      whenever(videoLinkBookingRepository.save(any()))
        .thenReturn(
          VideoLinkBooking(
            id = expectedVideoLinkBookingId,
            main = mainVideoLinkAppointment.copy(id = 12L)
          )
        )
      whenever(authenticationFacade.currentUsername).thenReturn("A_USER")

      service.createVideoLinkBooking(
        VideoLinkBookingSpecification(
          bookingId = 1L,
          court = YORK_CC,
          comment = "Comment",
          madeByTheCourt = true,
          main = VideoLinkAppointmentSpecification(
            locationId = 2L,
            startTime = startTime,
            endTime = endTime
          )
        )
      )

      verify(telemetryClient).trackEvent(
        "VideoLinkBookingCreated",
        mapOf(
          "id" to "11",
          "bookingId" to "1",
          "court" to "York Crown Court",
          "agencyId" to AGENCY_WANDSWORTH,
          "user" to "A_USER",
          "madeByTheCourt" to "true",
          "mainAppointmentId" to "12",
          "mainId" to "12",
          "mainStart" to startTime.toString(),
          "mainEnd" to endTime.toString(),
        ),
        null
      )
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

      whenever(authenticationFacade.currentUsername).thenReturn("A_USER")

      val vlbBookingId = service.createVideoLinkBooking(
        VideoLinkBookingSpecification(
          bookingId = offenderBookingId,
          court = YORK_CC,
          comment = "Comment",
          madeByTheCourt = false,
          pre = VideoLinkAppointmentSpecification(
            locationId = 1L,
            startTime = startTime.minusMinutes(20),
            endTime = startTime
          ),
          main = VideoLinkAppointmentSpecification(
            locationId = 2L,
            startTime = startTime,
            endTime = startTime.plusMinutes(30)
          ),
          post = VideoLinkAppointmentSpecification(
            locationId = 3L,
            startTime = startTime.plusMinutes(30),
            endTime = startTime.plusMinutes(50)
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

      verify(telemetryClient).trackEvent(
        "VideoLinkBookingCreated",
        mapOf(
          "id" to "11",
          "bookingId" to "1",
          "court" to "York Crown Court",
          "agencyId" to AGENCY_WANDSWORTH,
          "user" to "A_USER",
          "madeByTheCourt" to "false",

          "mainAppointmentId" to "12",
          "mainId" to "21",
          "mainStart" to startTime.toString(),
          "mainEnd" to startTime.plusMinutes(30).toString(),

          "preAppointmentId" to "13",
          "preId" to "20",
          "preStart" to "2020-10-09T10:10",
          "preEnd" to "2020-10-09T10:30",

          "postAppointmentId" to "14",
          "postId" to "22",
          "postStart" to "2020-10-09T11:00",
          "postEnd" to "2020-10-09T11:20",
        ),
        null
      )
    }
  }

  @Nested
  inner class DeleteVideoLinkBooking {
    val service = service("")

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

    }
  }

  private fun service(courts: String) = CourtService(
    authenticationFacade,
    prisonApiService,
    videoLinkAppointmentRepository,
    videoLinkBookingRepository,
    telemetryClient,
    courts
  )
}
