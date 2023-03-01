@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.whereabouts.services

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anySet
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.whereabouts.dto.BookingActivity
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderBooking
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.PrisonerScheduleDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendAllDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceHistoryDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceSummary
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendancesDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.CreateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.OffenderAttendance
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.AttendanceChange
import uk.gov.justice.digital.hmpps.whereabouts.model.AttendanceChangeValues
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceChangesRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Collectors

class AttendanceServiceTest {
  private val attendanceRepository: AttendanceRepository = spy()
  private val attendanceChangesRepository: AttendanceChangesRepository = spy()
  private val iepWarningService: IEPWarningService = mock()
  private val prisonApiService: PrisonApiService = mock()
  private val nomisEventOutcomeMapper: NomisEventOutcomeMapper = mock()
  private val telemetryClient: TelemetryClient = mock()

  private val today: LocalDate = LocalDate.now()
  private val testAttendanceDto: CreateAttendanceDto =
    CreateAttendanceDto(
      attended = false,
      paid = false,
      absentReason = AbsentReason.Refused,
      absentSubReason = AbsentSubReason.ExternalMoves,
      eventId = 2,
      eventLocationId = 3,
      period = TimePeriod.AM,
      prisonId = "LEI",
      bookingId = 100,
      eventDate = today,
      comments = "hello",
    )

  private val START = LocalDate.of(2021, 3, 14)
  private val MOORLAND = "MDI"
  private val testAttendanceHistoryDto =
    AttendanceHistoryDto(
      eventDate = START,
      comments = "Test comment",
      location = MOORLAND,
      activity = "a",
      activityDescription = "d",
    )

  private val service = AttendanceService(
    attendanceRepository,
    attendanceChangesRepository,
    prisonApiService,
    iepWarningService,
    nomisEventOutcomeMapper,
    telemetryClient,
  )

  init {
    SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("user", "pw")
  }

  @BeforeEach
  fun before() {
    // return the attendance entity on save
    doAnswer { it.getArgument(0) as Attendance }.whenever(attendanceRepository)
      .save(ArgumentMatchers.any(Attendance::class.java))
  }

  @Test
  fun `should find attendance given some criteria`() {
    val now = LocalDateTime.now()

    whenever(
      attendanceRepository
        .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, today, TimePeriod.AM),
    )
      .thenReturn(
        setOf(
          Attendance.builder()
            .id(1)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
            .attended(false)
            .paid(false)
            .eventId(2)
            .eventLocationId(3)
            .period(TimePeriod.AM)
            .prisonId("LEI")
            .bookingId(100)
            .eventDate(today)
            .createUserId("user")
            .createDateTime(now)
            .caseNoteId(1)
            .build(),
        ),
      )

    val result = service.getAttendanceForEventLocation("LEI", 1, today, TimePeriod.AM)

    assertThat(result).containsAnyElementsOf(
      mutableListOf(
        AttendanceDto
          .builder()
          .id(1)
          .absentReason(AbsentReason.Refused)
          .absentSubReason(AbsentSubReason.ExternalMoves)
          .attended(false)
          .paid(false)
          .eventId(2)
          .eventLocationId(3)
          .period(TimePeriod.AM)
          .prisonId("LEI")
          .bookingId(100)
          .eventDate(today)
          .createUserId("user")
          .createDateTime(now)
          .caseNoteId(1)
          .locked(false)
          .build(),
      ),
    )
  }

  @Test
  fun `should return locked true when attendance unpaid 7 days ago`() {
    val sevenDaysAgoTime = LocalDateTime.now().minusDays(7)

    whenever(
      attendanceRepository
        .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, sevenDaysAgoTime.toLocalDate(), TimePeriod.AM),
    )
      .thenReturn(
        setOf(
          Attendance.builder()
            .id(1)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
            .attended(false)
            .paid(false)
            .eventId(2)
            .eventLocationId(3)
            .period(TimePeriod.AM)
            .prisonId("LEI")
            .bookingId(100)
            .eventDate(sevenDaysAgoTime.toLocalDate())
            .createUserId("user")
            .createDateTime(sevenDaysAgoTime)
            .caseNoteId(1)
            .build(),
        ),
      )

    val result = service.getAttendanceForEventLocation("LEI", 1, sevenDaysAgoTime.toLocalDate(), TimePeriod.AM)

    assertThat(result).containsAnyElementsOf(
      mutableListOf(
        AttendanceDto
          .builder()
          .id(1)
          .absentReason(AbsentReason.Refused)
          .absentSubReason(AbsentSubReason.ExternalMoves)
          .attended(false)
          .paid(false)
          .eventId(2)
          .eventLocationId(3)
          .period(TimePeriod.AM)
          .prisonId("LEI")
          .bookingId(100)
          .eventDate(sevenDaysAgoTime.toLocalDate())
          .createUserId("user")
          .createDateTime(sevenDaysAgoTime)
          .caseNoteId(1)
          .locked(true)
          .build(),
      ),
    )
  }

  @Test
  fun `should return locked false when attendance unpaid under 7 days ago`() {
    val sixDaysAgoTime = LocalDateTime.now().minusDays(6)

    whenever(
      attendanceRepository
        .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, sixDaysAgoTime.toLocalDate(), TimePeriod.AM),
    )
      .thenReturn(
        setOf(
          Attendance.builder()
            .id(1)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
            .attended(false)
            .paid(false)
            .eventId(2)
            .eventLocationId(3)
            .period(TimePeriod.AM)
            .prisonId("LEI")
            .bookingId(100)
            .eventDate(sixDaysAgoTime.toLocalDate())
            .createUserId("user")
            .createDateTime(sixDaysAgoTime)
            .caseNoteId(1)
            .build(),
        ),
      )

    val result = service.getAttendanceForEventLocation("LEI", 1, sixDaysAgoTime.toLocalDate(), TimePeriod.AM)

    assertThat(result).containsAnyElementsOf(
      mutableListOf(
        AttendanceDto
          .builder()
          .id(1)
          .absentReason(AbsentReason.Refused)
          .absentSubReason(AbsentSubReason.ExternalMoves)
          .attended(false)
          .paid(false)
          .eventId(2)
          .eventLocationId(3)
          .period(TimePeriod.AM)
          .prisonId("LEI")
          .bookingId(100)
          .eventDate(sixDaysAgoTime.toLocalDate())
          .createUserId("user")
          .createDateTime(sixDaysAgoTime)
          .caseNoteId(1)
          .locked(false)
          .build(),
      ),
    )
  }

  @Test
  fun `should return locked true when attendance paid yesterday`() {
    val yesterdayTime = LocalDateTime.now().minusDays(1)

    whenever(
      attendanceRepository
        .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, yesterdayTime.toLocalDate(), TimePeriod.AM),
    )
      .thenReturn(
        setOf(
          Attendance.builder()
            .id(1)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
            .attended(false)
            .paid(true)
            .eventId(2)
            .eventLocationId(3)
            .period(TimePeriod.AM)
            .prisonId("LEI")
            .bookingId(100)
            .eventDate(yesterdayTime.toLocalDate())
            .createUserId("user")
            .createDateTime(yesterdayTime)
            .caseNoteId(1)
            .build(),
        ),
      )

    val result = service.getAttendanceForEventLocation("LEI", 1, yesterdayTime.toLocalDate(), TimePeriod.AM)

    assertThat(result).containsAnyElementsOf(
      mutableListOf(
        AttendanceDto
          .builder()
          .id(1)
          .absentReason(AbsentReason.Refused)
          .absentSubReason(AbsentSubReason.ExternalMoves)
          .attended(false)
          .paid(true)
          .eventId(2)
          .eventLocationId(3)
          .period(TimePeriod.AM)
          .prisonId("LEI")
          .bookingId(100)
          .eventDate(yesterdayTime.toLocalDate())
          .createUserId("user")
          .createDateTime(yesterdayTime)
          .caseNoteId(1)
          .locked(true)
          .build(),
      ),
    )
  }

  @Test
  fun `should create an attendance record`() {
    service.createAttendance(
      testAttendanceDto.copy(
        paid = true,
        attended = true,
        absentReason = null,
        absentSubReason = null,
      ),
    )

    verify(attendanceRepository).save(
      Attendance.builder()
        .attended(true)
        .paid(true)
        .eventId(2)
        .eventLocationId(3)
        .period(TimePeriod.AM)
        .prisonId("LEI")
        .bookingId(100)
        .eventDate(today)
        .comments("hello")
        .build(),
    )
  }

  @Test
  fun `should call nomisEventOutcomeMapper with the correct parameters`() {
    whenever(nomisEventOutcomeMapper.getEventOutcome(any(), any(), any(), any(), any())).thenReturn(
      EventOutcome(
        "ACCAB",
        null,
        "hello",
      ),
    )

    val attendance = testAttendanceDto.copy(
      absentReason = AbsentReason.AcceptableAbsence,
      attended = false,
      paid = true,
    )

    service.createAttendance(attendance)

    verify(nomisEventOutcomeMapper).getEventOutcome(
      AbsentReason.AcceptableAbsence,
      AbsentSubReason.ExternalMoves,
      false,
      true,
      "hello",
    )
  }

  @Test
  fun `should save case note id returned from the postCaseNote api call`() {
    whenever(
      iepWarningService.postIEPWarningIfRequired(
        any(),
        anyOrNull(),
        any(),
        any(),
        any(),
        any(),
      ),
    ).thenReturn(Optional.of(100L))

    service.createAttendance(
      testAttendanceDto
        .copy(
          attended = false,
          paid = false,
          absentReason = AbsentReason.Refused,
          absentSubReason = AbsentSubReason.ExternalMoves,
          eventId = 2,
          eventLocationId = 3,
          period = TimePeriod.AM,
          prisonId = "LEI",
          bookingId = 100L,
          eventDate = today,
          comments = "hello, world",
        ),
    )

    verify(attendanceRepository).save(
      Attendance
        .builder()
        .attended(false)
        .paid(false)
        .absentReason(AbsentReason.Refused)
        .absentSubReason(AbsentSubReason.ExternalMoves)
        .eventId(2)
        .eventLocationId(3)
        .period(TimePeriod.AM)
        .prisonId("LEI")
        .bookingId(100L)
        .caseNoteId(100L)
        .eventDate(today)
        .comments("hello, world")
        .build(),
    )
  }

  @Test
  fun `should throw an AttendanceNotFoundException`() {
    whenever(attendanceRepository.findById(1)).thenReturn(Optional.empty())

    assertThatThrownBy {
      service.updateAttendance(1, UpdateAttendanceDto(attended = false, paid = false))
    }.isExactlyInstanceOf(AttendanceNotFound::class.java)
  }

  @Test
  fun `should throw an AttendanceExistsException when attendance already created`() {
    whenever(
      attendanceRepository.findByPrisonIdAndBookingIdAndEventIdAndEventDateAndPeriod(
        "LEI",
        1,
        1,
        LocalDate.now(),
        TimePeriod.AM,
      ),
    ).thenReturn(
      setOf(
        Attendance
          .builder()
          .id(1)
          .attended(true)
          .paid(true)
          .eventId(1)
          .eventLocationId(2)
          .eventDate(LocalDate.now())
          .prisonId("LEI")
          .bookingId(1)
          .period(TimePeriod.AM)
          .build(),
      ),
    )

    assertThatThrownBy {
      service.createAttendance(
        CreateAttendanceDto(
          absentReason = AbsentReason.Refused,
          absentSubReason = AbsentSubReason.ExternalMoves,
          attended = false,
          paid = false,
          bookingId = 1,
          comments = "test comments",
          eventId = 1,
          eventLocationId = 2,
          period = TimePeriod.AM,
          prisonId = "LEI",
          eventDate = LocalDate.now(),
        ),
      )
    }.isExactlyInstanceOf(AttendanceExists::class.java)
  }

  @Test
  fun `should update select fields only`() {
    whenever(attendanceRepository.findById(1)).thenReturn(
      Optional.of(
        Attendance
          .builder()
          .id(1)
          .attended(true)
          .paid(true)
          .eventId(1)
          .eventLocationId(2)
          .eventDate(LocalDate.now())
          .prisonId("LEI")
          .bookingId(1)
          .period(TimePeriod.AM)
          .build(),
      ),
    )

    service.updateAttendance(
      1,
      UpdateAttendanceDto(
        absentReason = AbsentReason.SessionCancelled,
        absentSubReason = AbsentSubReason.ExternalMoves,
        attended = false,
        paid = false,
        comments = "Session cancelled due to riot",
      ),
    )

    verify(attendanceRepository).save(
      Attendance
        .builder()
        .id(1)
        .attended(false)
        .paid(false)
        .comments("Session cancelled due to riot")
        .absentReason(AbsentReason.SessionCancelled)
        .absentSubReason(AbsentSubReason.ExternalMoves)
        .eventId(1)
        .eventLocationId(2)
        .eventDate(LocalDate.now())
        .prisonId("LEI")
        .bookingId(1)
        .period(TimePeriod.AM)
        .build(),
    )
  }

  @Test
  fun `should go from unpaid none attendance to paid attendance `() {
    whenever(iepWarningService.handleIEPWarningScenarios(any(), any())).thenReturn(Optional.empty())

    whenever(attendanceRepository.findById(1)).thenReturn(
      Optional.of(
        Attendance
          .builder()
          .id(1)
          .attended(false)
          .paid(false)
          .absentReason(AbsentReason.Refused)
          .absentSubReason(AbsentSubReason.ExternalMoves)
          .comments("test comments")
          .caseNoteId(100)
          .eventId(1)
          .eventLocationId(2)
          .eventDate(LocalDate.now())
          .prisonId("LEI")
          .bookingId(1)
          .period(TimePeriod.AM)
          .build(),
      ),
    )

    service.updateAttendance(
      1,
      UpdateAttendanceDto(
        attended = true,
        paid = true,
      ),
    )

    verify(attendanceRepository).save(
      Attendance
        .builder()
        .id(1)
        .attended(true)
        .paid(true)
        .comments(null)
        .caseNoteId(100)
        .absentReason(null)
        .eventId(1)
        .eventLocationId(2)
        .eventDate(LocalDate.now())
        .prisonId("LEI")
        .bookingId(1)
        .period(TimePeriod.AM)
        .build(),
    )

    verify(iepWarningService).handleIEPWarningScenarios(any(), any())
  }

  @Test
  fun `should throw an AttendanceLocked when modified date is yesterday`() {
    val yesterday = LocalDate.now().minusDays(1)
    val today = LocalDate.now()
    val lastWeek = LocalDate.now().minusWeeks(1)

    whenever(attendanceRepository.findById(1)).thenReturn(
      Optional.of(
        Attendance
          .builder()
          .id(1)
          .attended(true)
          .paid(true)
          .eventId(1)
          .eventLocationId(2)
          .eventDate(today)
          .modifyDateTime(yesterday.atTime(10, 10))
          .createDateTime(lastWeek.atTime(10, 10))
          .prisonId("LEI")
          .bookingId(1)
          .period(TimePeriod.AM)
          .build(),
      ),
    )

    assertThatThrownBy {
      service.updateAttendance(1, UpdateAttendanceDto(paid = false, attended = false))
    }.isExactlyInstanceOf(AttendanceLocked::class.java)
  }

  @Test
  fun `should throw an AttendanceLocked when created date is yesterday`() {
    val yesterday = LocalDate.now().minusDays(1)
    val today = LocalDate.now()

    whenever(attendanceRepository.findById(1)).thenReturn(
      Optional.of(
        Attendance
          .builder()
          .id(1)
          .attended(true)
          .paid(true)
          .eventId(1)
          .eventLocationId(2)
          .eventDate(today)
          .createDateTime(yesterday.atTime(10, 10))
          .prisonId("LEI")
          .bookingId(1)
          .period(TimePeriod.AM)
          .build(),
      ),
    )

    assertThatThrownBy {
      service.updateAttendance(1, UpdateAttendanceDto(paid = false, attended = false))
    }.isExactlyInstanceOf(AttendanceLocked::class.java)
  }

  @Test
  fun `should not throw an AttendanceLocked`() {
    val yesterday = LocalDate.now().minusDays(1)
    val today = LocalDate.now()

    whenever(attendanceRepository.findById(1)).thenReturn(
      Optional.of(
        Attendance
          .builder()
          .id(1)
          .attended(true)
          .paid(true)
          .eventId(1)
          .eventLocationId(2)
          .eventDate(yesterday)
          .createDateTime(today.atTime(10, 10))
          .prisonId("LEI")
          .bookingId(1)
          .period(TimePeriod.AM)
          .build(),
      ),
    )

    service.updateAttendance(
      1,
      UpdateAttendanceDto(paid = true, attended = false, absentReason = AbsentReason.ApprovedCourse),
    )
  }

  @Test
  fun `should return attendance dto on creation`() {
    whenever(
      iepWarningService.postIEPWarningIfRequired(
        any(),
        anyOrNull(),
        any(),
        any(),
        any(),
        any(),
      ),
    ).thenReturn(Optional.of(100L))

    val created = service.createAttendance(
      CreateAttendanceDto(
        absentReason = AbsentReason.Refused,
        absentSubReason = AbsentSubReason.ExternalMoves,
        attended = false,
        paid = false,
        bookingId = 1,
        comments = "test comments",
        eventId = 1,
        eventLocationId = 2,
        period = TimePeriod.AM,
        prisonId = "LEI",
        eventDate = LocalDate.now(),
      ),
    )

    assertThat(created).isEqualTo(
      AttendanceDto
        .builder()
        .attended(false)
        .paid(false)
        .comments(null)
        .caseNoteId(100L)
        .absentReason(AbsentReason.Refused)
        .absentSubReason(AbsentSubReason.ExternalMoves)
        .eventId(1)
        .eventLocationId(2)
        .eventDate(LocalDate.now())
        .prisonId("LEI")
        .bookingId(1)
        .period(TimePeriod.AM)
        .eventDate(LocalDate.now())
        .comments("test comments")
        .locked(false)
        .build(),
    )
  }

  @Test
  fun `should remove previous comment when no longer required`() {
    val attendanceEntity = Attendance.builder()
      .bookingId(1)
      .eventLocationId(1)
      .eventId(1)
      .eventDate(today)
      .paid(false)
      .attended(false)
      .caseNoteId(1)
      .comments("Refused")
      .absentReason(AbsentReason.Refused)
      .absentSubReason(AbsentSubReason.ExternalMoves)
      .build()

    whenever(attendanceRepository.findById(1)).thenReturn(Optional.of(attendanceEntity.toBuilder().id(1).build()))

    service.updateAttendance(1, UpdateAttendanceDto(attended = true, paid = true))

    verify(attendanceRepository).save(
      attendanceEntity
        .toBuilder()
        .id(1)
        .comments(null)
        .absentReason(null)
        .absentSubReason(null)
        .attended(true)
        .paid(true)
        .build(),
    )
  }

  @Test
  fun `should load attendance details for a set of booking ids`() {
    val today = LocalDate.now()
    val bookingIds = setOf(1L, 2L)

    service.getAttendanceForBookings("LEI", bookingIds, today, TimePeriod.AM)

    verify(attendanceRepository).findByPrisonIdAndBookingIdInAndEventDateAndPeriod(
      "LEI",
      setOf(1, 2),
      today,
      TimePeriod.AM,
    )
  }

  @Test
  fun `should create an attendance record locally and via elite for multiple bookings for attend-all`() {
    whenever(nomisEventOutcomeMapper.getEventOutcome(anyOrNull(), anyOrNull(), any(), any(), any())).thenReturn(
      EventOutcome(
        "ATT",
        "STANDARD",
        "",
      ),
    )

    val bookingIds = setOf(1L, 2L)

    val bookingActivities = bookingIds
      .stream()
      .map { BookingActivity(activityId = 1L, bookingId = it) }
      .collect(Collectors.toSet())

    val savedAttendanceDetails = service.attendAll(
      AttendAllDto(
        eventDate = LocalDate.now().minusDays(1),
        eventLocationId = 2L,
        prisonId = "LEI",
        period = TimePeriod.AM,
        bookingActivities = bookingActivities,
      ),
    )

    assertThat(savedAttendanceDetails).containsExactlyInAnyOrder(
      AttendanceDto
        .builder()
        .bookingId(1L)
        .eventDate(LocalDate.now().minusDays(1))
        .eventId(1L)
        .eventLocationId(2L)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .locked(false)
        .attended(true)
        .paid(true)
        .build(),
      AttendanceDto
        .builder()
        .bookingId(2L)
        .eventDate(LocalDate.now().minusDays(1))
        .eventId(1L)
        .eventLocationId(2L)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .locked(false)
        .paid(true)
        .attended(true)
        .build(),
    )

    verify(attendanceRepository).saveAll(anySet())
    verify(prisonApiService).putAttendanceForMultipleBookings(
      bookingActivities,
      EventOutcome("ATT", "STANDARD", ""),
    )
  }

  @Nested
  inner class getPrisonersUnaccountedFor {
    val prisonId = "LEI"
    val date: LocalDate = LocalDate.now()
    val period = TimePeriod.AM

    @Test
    fun `should fetch scheduled activities and attendances`() {
      service.getPrisonersUnaccountedFor(prisonId, date, period)

      verify(prisonApiService).getScheduledActivities(prisonId, date, period)
      verify(attendanceRepository).findByPrisonIdAndPeriodAndEventDateBetween(prisonId, period, date, date)
    }

    @Test
    fun `should remove attendances from scheduled activities`() {
      whenever(prisonApiService.getScheduledActivities(any(), any(), any())).thenReturn(
        listOf(
          scheduleDto.copy(bookingId = 1, eventId = 2, offenderNo = "MATCH1"),
          scheduleDto.copy(bookingId = 2, eventId = 2, offenderNo = "MATCH2"),
          scheduleDto.copy(bookingId = 3, eventId = 1, offenderNo = "MATCH3"),
          scheduleDto.copy(bookingId = 3, eventId = 2, offenderNo = "NO_MATCH"),
        ),
      )
      whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(any(), any(), any(), any()))
        .thenReturn(
          setOf(
            attendance.toBuilder().bookingId(1).eventId(2).build(),
            attendance.toBuilder().bookingId(3).eventId(1).build(),
            attendance.toBuilder().bookingId(2).eventId(2).build(),
          ),
        )

      val scheduled = service.getPrisonersUnaccountedFor(prisonId, date, period)
      assertThat(scheduled.map { it.offenderNo }).containsExactly("NO_MATCH")
    }
  }

  @Test
  fun `should create an attendance records locally and push event out-comes up to elite for multiple bookings`() {
    whenever(nomisEventOutcomeMapper.getEventOutcome(any(), anyOrNull(), any(), any(), any())).thenReturn(
      EventOutcome(
        "ACCAB",
        null,
        "test",
      ),
    )

    val bookingIds = setOf(1L, 2L)

    val bookingActivities = bookingIds
      .stream()
      .map { BookingActivity(activityId = 1L, bookingId = it) }
      .collect(Collectors.toSet())

    val savedAttendanceDetails = service.createAttendances(
      AttendancesDto(
        eventDate = LocalDate.now().minusDays(1),
        eventLocationId = 2L,
        prisonId = "LEI",
        period = TimePeriod.AM,
        reason = AbsentReason.AcceptableAbsence,
        comments = "test",
        attended = false,
        paid = true,
        bookingActivities = bookingActivities,
      ),
    )

    assertThat(savedAttendanceDetails).containsExactlyInAnyOrder(
      AttendanceDto
        .builder()
        .bookingId(1L)
        .eventDate(LocalDate.now().minusDays(1))
        .eventId(1L)
        .eventLocationId(2L)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .locked(false)
        .attended(false)
        .paid(true)
        .comments("test")
        .absentReason(AbsentReason.AcceptableAbsence)
        .build(),
      AttendanceDto
        .builder()
        .bookingId(2L)
        .eventDate(LocalDate.now().minusDays(1))
        .eventId(1L)
        .eventLocationId(2L)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .locked(false)
        .paid(true)
        .attended(false)
        .comments("test")
        .absentReason(AbsentReason.AcceptableAbsence)
        .build(),
    )

    verify(attendanceRepository).saveAll(anySet())
    verify(prisonApiService).putAttendanceForMultipleBookings(
      bookingActivities,
      EventOutcome("ACCAB", null, "test"),
    )
  }

  @Nested
  inner class getAbsencesForReason {
    @Test
    fun `should not make a request for schedule activity if no attendances`() {
      val prison = "MDI"
      val fromDate = LocalDate.now()
      val toDate = LocalDate.now().plusDays(20)
      val reason = AbsentReason.Refused
      val period = TimePeriod.PM

      service.getAbsencesForReason(prison, reason, fromDate, toDate, period)

      verifyNoInteractions(prisonApiService)
      verify(attendanceRepository).findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(
        prison,
        fromDate,
        toDate,
        setOf(period),
        reason,
      )
    }

    @Test
    fun `should make requests for schedule activity over a date range`() {
      val prison = "MDI"
      val fromDate = LocalDate.now()
      val toDate = LocalDate.now().plusDays(20)
      val reason = AbsentReason.Refused

      service.getAbsencesForReason(prison, reason, fromDate, toDate, null)

      verify(attendanceRepository).findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(
        prison,
        fromDate,
        toDate,
        setOf(TimePeriod.AM, TimePeriod.PM),
        reason,
      )
    }

    @Test
    fun `should call Prison API with eventIds`() {
      val eventDate = LocalDate.now()
      val prison = "MDI"

      whenever(
        attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(
          any(),
          any(),
          any(),
          any(),
          any(),
        ),
      ).thenReturn(
        setOf(
          Attendance.builder().bookingId(1).eventId(1).attended(false).paid(false).prisonId(prison)
            .period(TimePeriod.AM)
            .absentReason(AbsentReason.Refused).build(),
          Attendance.builder().bookingId(1).eventId(2).attended(false).paid(false).prisonId(prison)
            .period(TimePeriod.AM)
            .absentReason(AbsentReason.Refused).build(),
          Attendance.builder().bookingId(1).eventId(3).attended(false).paid(false).prisonId(prison)
            .period(TimePeriod.AM)
            .absentReason(AbsentReason.RestDay).build(),
        ),
      )

      service.getAbsencesForReason(prison, AbsentReason.Refused, eventDate, eventDate, TimePeriod.AM)

      verify(prisonApiService).getScheduleActivityOffenderData("MDI", setOf(1L, 2L, 3L))
    }

    @Test
    fun `should return absent dto populated with attendance and offender information`() {
      val eventDate = LocalDate.now()
      val prison = "MDI"

      whenever(prisonApiService.getScheduleActivityOffenderData(any(), any())).thenReturn(
        listOf(
          OffenderDetails(
            bookingId = 1,
            offenderNo = "A12345",
            eventId = 2,
            cellLocation = "cell1",
            eventDate = eventDate,
            timeSlot = "AM",
            comment = "Gym",
            firstName = "john",
            lastName = "doe",
            suspended = true,
          ),
          OffenderDetails(
            bookingId = 1,
            offenderNo = "A12345",
            eventId = 3,
            cellLocation = "cell2",
            eventDate = eventDate,
            timeSlot = "AM",
            comment = "Workshop 1",
            firstName = "john",
            lastName = "doe",
            suspended = false,
          ),
          OffenderDetails(
            bookingId = 2,
            offenderNo = "A12346",
            eventId = 4,
            eventDate = eventDate,
            timeSlot = "PM",
            firstName = "dave",
            lastName = "doe1",
            suspended = true,
          ),
          OffenderDetails(
            bookingId = 2,
            offenderNo = "A12346",
            eventId = 5,
            cellLocation = "cell4",
            eventDate = eventDate,
            timeSlot = "PM",
            firstName = "dave",
            lastName = "doe1",
            suspended = false,
          ),
        ),
      )

      whenever(
        attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(
          any(),
          any(),
          any(),
          any(),
          any(),
        ),
      ).thenReturn(
        setOf(
          Attendance.builder()
            .id(1)
            .bookingId(1)
            .eventId(2)
            .eventLocationId(1)
            .attended(false)
            .paid(false)
            .prisonId(prison)
            .period(TimePeriod.AM)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
            .comments("comment1")
            .eventDate(eventDate)
            .build(),
          Attendance.builder()
            .id(2)
            .bookingId(1)
            .eventId(3)
            .eventLocationId(1)
            .attended(false)
            .paid(false)
            .prisonId(prison)
            .period(TimePeriod.AM)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
            .comments("comment2")
            .eventDate(eventDate)
            .build(),
          Attendance.builder()
            .id(3)
            .bookingId(2)
            .eventId(4)
            .eventLocationId(1)
            .attended(false)
            .paid(false)
            .prisonId(prison)
            .period(TimePeriod.PM)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
            .comments("comment3")
            .eventDate(eventDate)
            .build(),
          Attendance.builder()
            .id(4)
            .bookingId(2)
            .eventId(5)
            .eventLocationId(1)
            .attended(false)
            .paid(false)
            .prisonId(prison)
            .period(TimePeriod.PM)
            .absentReason(AbsentReason.Refused)
            .absentSubReason(AbsentSubReason.ExternalMoves)
            .comments("comment4")
            .eventDate(eventDate)
            .build(),
        ),
      )

      val attendances = service.getAbsencesForReason(prison, AbsentReason.Refused, eventDate, eventDate, null)

      assertThat(attendances).extracting(
        "attendanceId",
        "bookingId",
        "offenderNo",
        "eventId",
        "eventLocationId",
        "eventDate",
        "period",
        "reason",
        "subReason",
        "eventDescription",
        "comments",
        "cellLocation",
        "firstName",
        "lastName",
        "suspended",
      )
        .containsExactlyInAnyOrder(
          Tuple.tuple(
            1L,
            1L,
            "A12345",
            2L,
            1L,
            eventDate,
            TimePeriod.AM,
            AbsentReason.Refused,
            AbsentSubReason.ExternalMoves,
            "Gym",
            "comment1",
            "cell1",
            "john",
            "doe",
            true,
          ),
          Tuple.tuple(
            2L,
            1L,
            "A12345",
            3L,
            1L,
            eventDate,
            TimePeriod.AM,
            AbsentReason.Refused,
            AbsentSubReason.ExternalMoves,
            "Workshop 1",
            "comment2",
            "cell2",
            "john",
            "doe",
            false,
          ),

          Tuple.tuple(
            3L,
            2L,
            "A12346",
            4L,
            1L,
            eventDate,
            TimePeriod.PM,
            AbsentReason.Refused,
            AbsentSubReason.ExternalMoves,
            null,
            "comment3",
            null,
            "dave",
            "doe1",
            true,
          ),
          Tuple.tuple(
            4L,
            2L,
            "A12346",
            5L,
            1L,
            eventDate,
            TimePeriod.PM,
            AbsentReason.Refused,
            AbsentSubReason.ExternalMoves,
            null,
            "comment4",
            "cell4",
            "dave",
            "doe1",
            false,
          ),
        )
    }

    @Test
    fun `should substitute toDate with fromDate when toDate is null`() {
      val date = LocalDate.now().atStartOfDay().toLocalDate()

      val prison = "MDI"
      val reason = AbsentReason.Refused
      val period = TimePeriod.AM

      service.getAbsencesForReason(prison, reason, date, null, period)

      verify(attendanceRepository).findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(
        prison,
        date,
        date,
        setOf(period),
        reason,
      )
    }
  }

  @Test
  fun `should substitute toDate with fromDate when toDate is null for attendance over date range`() {
    val date = LocalDate.now().atStartOfDay().toLocalDate()

    val prison = "MDI"
    val period = TimePeriod.AM

    service.getAttendanceForBookingsOverDateRange(prison, setOf(1, 2), date, null, period)

    verify(attendanceRepository).findByPrisonIdAndBookingIdInAndEventDateBetweenAndPeriodIn(
      prison,
      setOf(1, 2),
      date,
      date,
      setOf(period),
    )
  }

  @Test
  fun `should substitute empty period with a set of AM and PM for attendance over date range`() {
    val date = LocalDate.now().atStartOfDay().toLocalDate()

    val prison = "MDI"

    service.getAttendanceForBookingsOverDateRange(prison, setOf(1, 2), date, null, null)

    verify(attendanceRepository).findByPrisonIdAndBookingIdInAndEventDateBetweenAndPeriodIn(
      prison,
      setOf(1, 2),
      date,
      date,
      setOf(TimePeriod.AM, TimePeriod.PM),
    )
  }

  @Test
  fun `should attempt to delete two attendance records and raise telemetry event`() {
    val offenderNo = "A12345"

    whenever(attendanceRepository.findByBookingId(1)).thenReturn(
      setOf(
        Attendance.builder().id(1).bookingId(1).build(),
        Attendance.builder().id(2).bookingId(1).build(),
        Attendance.builder().id(3).bookingId(1).build(),
      ),
    )

    service.deleteAttendancesForOffenderDeleteEvent(offenderNo, listOf(1L))

    verify(attendanceRepository).findByBookingId(eq(1))
    verify(attendanceRepository).deleteAll(
      eq(
        setOf(
          Attendance.builder().id(1).bookingId(1).build(),
          Attendance.builder().id(2).bookingId(1).build(),
          Attendance.builder().id(3).bookingId(1).build(),
        ),
      ),
    )
    verify(telemetryClient).trackEvent("OffenderDelete", mapOf("offenderNo" to "A12345", "count" to "3"), null)
  }

  @Test
  fun `should record changes made to attendance record when absent`() {
    val attendance = Attendance.builder()
      .id(1)
      .absentReason(AbsentReason.Refused)
      .absentSubReason(AbsentSubReason.ExternalMoves)
      .attended(false)
      .paid(false)
      .eventId(2)
      .eventLocationId(3)
      .period(TimePeriod.AM)
      .prisonId("LEI")
      .bookingId(100)
      .createUserId("user")
      .caseNoteId(1)
      .build()

    whenever(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance))

    service.updateAttendance(
      1L,
      UpdateAttendanceDto(absentReason = AbsentReason.NotRequired, attended = false, paid = false),
    )

    verify(attendanceChangesRepository).save(
      AttendanceChange(
        attendance = attendance,
        changedFrom = AttendanceChangeValues.Refused,
        changedTo = AttendanceChangeValues.NotRequired,
      ),
    )
  }

  @Test
  fun `should record changes made to attendance record going from absent to attended`() {
    val attendance = Attendance.builder()
      .id(1)
      .absentReason(AbsentReason.Refused)
      .attended(false)
      .paid(false)
      .eventId(2)
      .eventLocationId(3)
      .period(TimePeriod.AM)
      .prisonId("LEI")
      .bookingId(100)
      .createUserId("user")
      .caseNoteId(1)
      .build()

    whenever(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance))

    service.updateAttendance(1L, UpdateAttendanceDto(attended = true, paid = true))

    verify(attendanceChangesRepository).save(
      AttendanceChange(
        attendance = attendance,
        changedFrom = AttendanceChangeValues.Refused,
        changedTo = AttendanceChangeValues.Attended,
      ),
    )
  }

  @Test
  fun `should call findAttendanceChangeByCreateDateTime when toDateTime is null`() {
    val fromDateTime = LocalDateTime.now()

    service.getAttendanceChanges(fromDateTime, null)

    verify(attendanceChangesRepository).findAttendanceChangeByCreateDateTime(fromDateTime)
  }

  @Test
  fun `should call findAttendanceChangeByCreateDateTimeBetween when fromDateTime and toDateTime are present`() {
    val fromDateTime = LocalDateTime.now()
    val toDateTime = LocalDateTime.now()

    service.getAttendanceChanges(fromDateTime, toDateTime)

    verify(attendanceChangesRepository).findAttendanceChangeByCreateDateTimeBetween(fromDateTime, toDateTime)
  }

  @Test
  fun `should map to attendance change to correctly`() {
    val createdDateTime = LocalDateTime.now()
    val attendance = Attendance.builder()
      .id(1)
      .absentReason(AbsentReason.Refused)
      .attended(false)
      .paid(false)
      .eventId(2)
      .eventLocationId(3)
      .period(TimePeriod.AM)
      .prisonId("LEI")
      .bookingId(100)
      .createUserId("user")
      .caseNoteId(1)
      .build()

    val attendanceChange = AttendanceChange(
      id = 1,
      attendance = attendance,
      changedFrom = AttendanceChangeValues.Attended,
      changedTo = AttendanceChangeValues.Refused,
      createDateTime = createdDateTime,
      createUserId = "ITAG_USER",
    )

    whenever(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance))
    whenever(attendanceChangesRepository.findAttendanceChangeByCreateDateTime(any())).thenReturn(setOf(attendanceChange))

    val change = service.getAttendanceChanges(createdDateTime, null).first()

    assertThat(change.changedOn).isEqualTo(createdDateTime)
    assertThat(change.attendanceId).isEqualTo(1)
    assertThat(change.bookingId).isEqualTo(100)
    assertThat(change.changedFrom).isEqualTo(AttendanceChangeValues.Attended)
    assertThat(change.changedTo).isEqualTo(AttendanceChangeValues.Refused)
    assertThat(change.eventId).isEqualTo(2)
    assertThat(change.eventLocationId).isEqualTo(3)
    assertThat(change.changedBy).isEqualTo("ITAG_USER")
    assertThat(change.prisonId).isEqualTo("LEI")
  }

  val pageable = Pageable.ofSize(10)

  private fun createOffenderAttendance(eventDate: String, outcome: String?): OffenderAttendance =
    OffenderAttendance(eventDate, outcome, prisonId = "MDI", activity = "a", description = "d")

  @Test
  fun `should get attendance summary data for offender`() {
    val offenderNo = "A1234AA"

    whenever(
      prisonApiService.getAttendanceForOffender(
        offenderNo,
        LocalDate.now().minusYears(1),
        LocalDate.now(),
        null,
        Pageable.unpaged(),
      ),
    ).thenReturn(
      PageImpl(
        listOf(
          createOffenderAttendance(eventDate = "2021-06-01", outcome = "ATT"),
          createOffenderAttendance(eventDate = "2021-06-01", outcome = "ATT"),
          createOffenderAttendance(eventDate = "2021-06-01", outcome = "ABS"),
          createOffenderAttendance(eventDate = "2021-08-01", outcome = "ATT"),
          createOffenderAttendance(eventDate = "2021-08-01", outcome = "UNACAB"),
          createOffenderAttendance(eventDate = "2021-08-01", outcome = "ATT"),
          createOffenderAttendance(eventDate = "2021-08-01", outcome = ""),
          createOffenderAttendance(eventDate = "2021-05-01", outcome = null),
        ),
      ),
    )

    val result =
      service.getAttendanceAbsenceSummaryForOffender(offenderNo, LocalDate.now().minusYears(1), LocalDate.now())

    assertThat(result).isEqualTo(AttendanceSummary(acceptableAbsence = 1, unacceptableAbsence = 1, total = 6))
  }

  @Test
  fun `should get attendance data by calling prisonApi`() {
    val offenderNo = "A1234AA"

    whenever(
      prisonApiService.getAttendanceForOffender(
        offenderNo,
        LocalDate.now().minusYears(1),
        LocalDate.now(),
        "UNACAB",
        pageable,
      ),
    ).thenReturn(
      PageImpl(
        listOf(
          OffenderAttendance(
            "2021-03-11",
            "ATT",
            prisonId = "WWI",
            activity = "a1",
            description = "d1",
            comment = "Test comment 1",
          ),
          OffenderAttendance(
            "2021-03-14",
            "UNACAB",
            prisonId = "MDI",
            activity = "a",
            description = "d",
            comment = "Test comment",
          ),
        ),
      ),
    )

    val result =
      service.getAttendanceDetailsForOffender(
        offenderNo,
        LocalDate.now().minusYears(1),
        LocalDate.now(),
        pageable,
      )

    assertThat(result.content).hasSize(2)
    assertThat(result.content.get(1)).isEqualTo(testAttendanceHistoryDto)
  }

  // disabled for now: @Test
  fun `should get attendance data by calling whereabouts-api with valid booking Ids`() {
    val offenderNo = "A1234AA"

    val attendance = Attendance.builder()
      .id(1)
      .absentReason(AbsentReason.NotRequired)
      .attended(false)
      .paid(false)
      .eventId(2)
      .eventLocationId(3)
      .period(TimePeriod.AM)
      .prisonId("LEI")
      .bookingId(100)
      .createUserId("user")
      .caseNoteId(1)
      .eventDate(testAttendanceHistoryDto.eventDate)
      .period((TimePeriod.AM))
      .comments(testAttendanceHistoryDto.comments)
      .build()

    whenever(
      prisonApiService.getOffenderDetailsFromOffenderNos(
        listOf(offenderNo),
      ),
    ).thenReturn(
      listOf(
        OffenderBooking(
          101L,
          "1001",
          offenderNo,
          "Jon",
          "Doe",
          "AGC",
          testAttendanceHistoryDto.eventDate, null, null,
        ),
      ),
    )

    whenever(
      attendanceRepository.findByBookingIdInAndEventDateBetween(any(), any(), any()),
    ).thenReturn(
      listOf(attendance),
    )

    val result =
      service.getAttendanceDetailsForOffender(
        offenderNo,
        LocalDate.now().minusYears(1),
        LocalDate.now(),
        pageable,
      )

    assertThat(result.content).hasSize(1)
    assertThat(result.content.get(0).eventDate).isEqualTo(attendance.eventDate)
    assertThat(result.content.get(0).comments).isEqualTo(attendance.comments)
  }

  private val scheduleDto = PrisonerScheduleDto(
    offenderNo = "A123BC",
    eventId = 2L,
    bookingId = 1L,
    locationId = 3L,
    firstName = "Bob",
    lastName = "Smith",
    cellLocation = "loc 1",
    event = "some event",
    eventType = "some type",
    eventDescription = "some event desc",
    eventLocation = "some loc",
    eventLocationId = 4L,
    eventStatus = "stat",
    comment = "some comment",
    LocalDateTime.now(),
    endTime = LocalDateTime.now().plusHours(1),
    eventOutcome = "some outcome",
    performance = "perf",
    outcomeComment = "out comment",
    paid = false,
    payRate = null,
    excluded = false,
    timeSlot = TimePeriod.AM,
    locationCode = "some loc code",
    suspended = false,
  )

  private val attendance = Attendance.builder()
    .id(1)
    .absentReason(AbsentReason.Refused)
    .attended(false)
    .paid(false)
    .eventId(2)
    .eventLocationId(3)
    .period(TimePeriod.AM)
    .prisonId("LEI")
    .bookingId(100)
    .createUserId("user")
    .caseNoteId(1)
    .build()
}
