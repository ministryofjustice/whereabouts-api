package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.whereabouts.dto.*
import uk.gov.justice.digital.hmpps.whereabouts.dto.elite.CaseNoteDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors

@RunWith(MockitoJUnitRunner::class)
class AttendanceServiceTest {
    @Spy
    private lateinit var attendanceRepository: AttendanceRepository

    @Mock
    private lateinit var elite2ApiService: Elite2ApiService

    @Mock
    private lateinit var caseNotesService: CaseNotesService

    private val today: LocalDate = LocalDate.now()
    private val testAttendanceDto: CreateAttendanceDto =
            CreateAttendanceDto
                    .builder()
                    .attended(false)
                    .paid(false)
                    .absentReason(AbsentReason.Refused)
                    .eventId(2)
                    .eventLocationId(3)
                    .period(TimePeriod.AM)
                    .prisonId("LEI")
                    .bookingId(100)
                    .eventDate(today)
                    .comments("hello")
                    .build()
    private lateinit var service: AttendanceService

    init {
        SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("user", "pw")
    }

    @Before
    fun before() {
        // return the attendance entity on save
        doAnswer { it.getArgument(0) as Attendance }.`when`(attendanceRepository).save(any())
        service = AttendanceService(attendanceRepository, elite2ApiService, caseNotesService)
    }

    @Test
    fun `should find attendance given some criteria`() {

        val now = LocalDateTime.now()

        `when`(attendanceRepository
                .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, today, TimePeriod.AM))
                .thenReturn(setOf(
                        Attendance.builder()
                                .id(1)
                                .absentReason(AbsentReason.Refused)
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
                                .build()
                ))

        val result = service.getAttendanceForEventLocation("LEI", 1, today, TimePeriod.AM)

        assertThat(result).containsAnyElementsOf(mutableListOf(
                AttendanceDto
                        .builder()
                        .id(1)
                        .absentReason(AbsentReason.Refused)
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
                        .build()
        ))
    }

    @Test
    fun `should return locked true when attendance unpaid 7 days ago`() {
        val sevenDaysAgoTime = LocalDateTime.now().minusDays(7)

        `when`(attendanceRepository
                .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, sevenDaysAgoTime.toLocalDate(), TimePeriod.AM))
                .thenReturn(setOf(
                        Attendance.builder()
                                .id(1)
                                .absentReason(AbsentReason.Refused)
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
                                .build()
                ))

        val result = service.getAttendanceForEventLocation("LEI", 1, sevenDaysAgoTime.toLocalDate(), TimePeriod.AM)

        assertThat(result).containsAnyElementsOf(mutableListOf(
                AttendanceDto
                        .builder()
                        .id(1)
                        .absentReason(AbsentReason.Refused)
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
                        .build()
        ))
    }

    @Test
    fun `should return locked false when attendance unpaid under 7 days ago`() {
        val sixDaysAgoTime = LocalDateTime.now().minusDays(6)

        `when`(attendanceRepository
                .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, sixDaysAgoTime.toLocalDate(), TimePeriod.AM))
                .thenReturn(setOf(
                        Attendance.builder()
                                .id(1)
                                .absentReason(AbsentReason.Refused)
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
                                .build()
                ))

        val result = service.getAttendanceForEventLocation("LEI", 1, sixDaysAgoTime.toLocalDate(), TimePeriod.AM)

        assertThat(result).containsAnyElementsOf(mutableListOf(
                AttendanceDto
                        .builder()
                        .id(1)
                        .absentReason(AbsentReason.Refused)
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
                        .build()
        ))
    }

    @Test
    fun `should return locked true when attendance paid yesterday`() {
        val yesterdayTime = LocalDateTime.now().minusDays(1)

        `when`(attendanceRepository
                .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, yesterdayTime.toLocalDate(), TimePeriod.AM))
                .thenReturn(setOf(
                        Attendance.builder()
                                .id(1)
                                .absentReason(AbsentReason.Refused)
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
                                .build()
                ))

        val result = service.getAttendanceForEventLocation("LEI", 1, yesterdayTime.toLocalDate(), TimePeriod.AM)

        assertThat(result).containsAnyElementsOf(mutableListOf(
                AttendanceDto
                        .builder()
                        .id(1)
                        .absentReason(AbsentReason.Refused)
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
                        .build()
        ))
    }

    @Test
    fun `should create an attendance record`() {

        service.createAttendance(
                testAttendanceDto
                        .toBuilder()
                        .paid(true)
                        .attended(true)
                        .absentReason(null)
                        .build()
        )

        verify(attendanceRepository)?.save(Attendance.builder()
                .attended(true)
                .paid(true)
                .eventId(2)
                .eventLocationId(3)
                .period(TimePeriod.AM)
                .prisonId("LEI")
                .bookingId(100)
                .eventDate(today)
                .comments("hello")
                .build())

    }

    @Test
    fun `should record paid attendance`() {

        val attendance = testAttendanceDto
                .toBuilder()
                .attended(true)
                .paid(true)
                .absentReason(null)
                .build()

        service.createAttendance(attendance)

        verify(elite2ApiService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("ATT", "STANDARD", attendance.comments))

    }


    @Test
    fun `should record paid absence for 'acceptable absence'`() {

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.AcceptableAbsence)
                .attended(false)
                .paid(true)
                .build()

        service.createAttendance(attendance)

        verify(elite2ApiService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("ACCAB", null, "hello"))
    }

    @Test
    fun `should record paid absence for 'not required'`() {

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.NotRequired)
                .attended(false)
                .paid(true)
                .build()

        service.createAttendance(attendance)

        verify(elite2ApiService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("NREQ", null, "hello"))
    }

    @Test
    fun `should record unpaid absence for 'session cancelled'`() {

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.SessionCancelled)
                .attended(false)
                .paid(false)
                .build()

        service.createAttendance(attendance)

        verify(elite2ApiService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("CANC", null, "hello"))
    }

    @Test
    fun `should record unpaid absence for 'rest in cell'`() {

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.RestInCell)
                .attended(false)
                .paid(false)
                .build()

        service.createAttendance(attendance)

        verify(elite2ApiService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("REST", null, "hello"))
    }

    @Test
    fun `should record unpaid absence for 'Sick'`() {

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.Sick)
                .attended(false)
                .paid(false)
                .build()

        service.createAttendance(attendance)

        verify(elite2ApiService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("REST", null, "hello"))
    }

    @Test
    fun `should record unpaid absence for 'Rest day'`() {

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.RestDay)
                .attended(false)
                .paid(false)
                .build()

        service.createAttendance(attendance)

        verify(elite2ApiService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("REST", null, "hello"))
    }

    @Test
    fun `should record unpaid absence as 'Refused'`() {

        `when`(caseNotesService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())
        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.Refused)
                .attended(false)
                .paid(false)
                .build()

        service.createAttendance(attendance)

        verify(elite2ApiService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("UNACAB", null, "hello"))
    }

    @Test
    fun `should record unpaid absence for 'Unacceptable absence'`() {

        `when`(caseNotesService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())
        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.UnacceptableAbsence)
                .attended(false)
                .paid(false)
                .build()


        service.createAttendance(attendance)

        verify(elite2ApiService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("UNACAB", null, "hello"))
    }

    @Test
    fun `should create negative case note for 'Unacceptable absence'`() {

        `when`(caseNotesService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.UnacceptableAbsence)
                .attended(false)
                .paid(false)
                .build()

        service.createAttendance(attendance)

        verify(caseNotesService).postCaseNote(
                eq("AB1234C"),
                eq("NEG"),
                eq("IEP_WARN"),
                eq("Unacceptable absence - hello"),
                isA(LocalDateTime::class.java))
    }


    @Test
    fun `should create negative case note for 'Refused'`() {

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.Refused)
                .attended(false)
                .paid(false)
                .build()

        `when`(caseNotesService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())
        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        service.createAttendance(attendance)

        verify(caseNotesService)
                .postCaseNote(
                        eq("AB1234C"),
                        eq("NEG"),
                        eq("IEP_WARN"),
                        eq("Refused - hello"),
                        isA(LocalDateTime::class.java))
    }

    @Test
    fun `should create a negative case note using user supplied comment`() {

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.Refused)
                .attended(false)
                .comments("test comment")
                .paid(false)
                .build()

        `when`(caseNotesService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())
        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        service.createAttendance(attendance)

        verify(caseNotesService)
                .postCaseNote(
                        eq("AB1234C"),
                        eq("NEG"),
                        eq("IEP_WARN"),
                        eq("Refused - test comment"),
                        isA(LocalDateTime::class.java))
    }


    @Test
    fun `should record paid absence for 'Approved course'`() {
        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.ApprovedCourse)
                .attended(false)
                .paid(true)
                .build()


        service.createAttendance(attendance)

        verify(elite2ApiService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("ACCAB", null, "hello"))
    }


    @Test
    fun `should save case note id returned from the postCaseNote api call`() {

        `when`(caseNotesService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())
        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        service.createAttendance(testAttendanceDto
                .toBuilder()
                .attended(false)
                .paid(false)
                .absentReason(AbsentReason.Refused)
                .eventId(2)
                .eventLocationId(3)
                .period(TimePeriod.AM)
                .prisonId("LEI")
                .bookingId(100L)
                .eventDate(today)
                .comments("hello, world")
                .build())

        verify(attendanceRepository).save(Attendance
                .builder()
                .attended(false)
                .paid(false)
                .absentReason(AbsentReason.Refused)
                .eventId(2)
                .eventLocationId(3)
                .period(TimePeriod.AM)
                .prisonId("LEI")
                .bookingId(100L)
                .caseNoteId(100L)
                .eventDate(today)
                .comments("hello, world")
                .build())
    }

    @Test
    fun `should throw an AttendanceNotFoundException`() {
        `when`(attendanceRepository.findById(1)).thenReturn(Optional.empty())

        assertThatThrownBy {
            service.updateAttendance(1, UpdateAttendanceDto.builder().build())
        }.isExactlyInstanceOf(AttendanceNotFound::class.java)
    }

    @Test
    fun `should update select fields only`() {

        `when`(attendanceRepository.findById(1)).thenReturn(
                Optional.of(Attendance
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
                        .build()))

        service.updateAttendance(1, UpdateAttendanceDto
                .builder()
                .absentReason(AbsentReason.SessionCancelled)
                .attended(false)
                .paid(false)
                .comments("Session cancelled due to riot")
                .build()
        )

        verify(attendanceRepository).save(
                Attendance
                        .builder()
                        .id(1)
                        .attended(false)
                        .paid(false)
                        .comments("Session cancelled due to riot")
                        .absentReason(AbsentReason.SessionCancelled)
                        .eventId(1)
                        .eventLocationId(2)
                        .eventDate(LocalDate.now())
                        .prisonId("LEI")
                        .bookingId(1)
                        .period(TimePeriod.AM)
                        .build())
    }

    @Test
    fun `should go from unpaid none attendance to paid attendance `() {

        `when`(attendanceRepository.findById(1)).thenReturn(
                Optional.of(Attendance
                        .builder()
                        .id(1)
                        .attended(false)
                        .paid(false)
                        .absentReason(AbsentReason.Refused)
                        .comments("test comments")
                        .caseNoteId(100)
                        .eventId(1)
                        .eventLocationId(2)
                        .eventDate(LocalDate.now())
                        .prisonId("LEI")
                        .bookingId(1)
                        .period(TimePeriod.AM)
                        .build()))

        service.updateAttendance(1, UpdateAttendanceDto
                .builder()
                .attended(true)
                .paid(true)
                .build()
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
                        .build())
    }

    @Test
    fun `should throw an AttendanceLocked when modified date is yesterday`() {
        val yesterday = LocalDate.now().minusDays(1)
        val today = LocalDate.now()
        val lastWeek = LocalDate.now().minusWeeks(1)

        `when`(attendanceRepository.findById(1)).thenReturn(
                Optional.of(Attendance
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
                        .build()))

        assertThatThrownBy {
            service.updateAttendance(1, UpdateAttendanceDto.builder().paid(false).attended(false).build())
        }.isExactlyInstanceOf(AttendanceLocked::class.java)
    }

    @Test
    fun `should throw an AttendanceLocked when created date is yesterday`() {
        val yesterday = LocalDate.now().minusDays(1)
        val today = LocalDate.now()

        `when`(attendanceRepository.findById(1)).thenReturn(
                Optional.of(Attendance
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
                        .build()))

        assertThatThrownBy {
            service.updateAttendance(1, UpdateAttendanceDto.builder().paid(false).attended(false).build())
        }.isExactlyInstanceOf(AttendanceLocked::class.java)
    }

    @Test
    fun `should not throw an AttendanceLocked`() {
        val yesterday = LocalDate.now().minusDays(1)
        val today = LocalDate.now()

        `when`(attendanceRepository.findById(1)).thenReturn(
                Optional.of(Attendance
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
                        .build()))

        service.updateAttendance(1,
                UpdateAttendanceDto.builder().paid(true).attended(false).absentReason(AbsentReason.ApprovedCourse).build())
    }

    @Test
    fun `should return attendance dto on creation`() {

        `when`(caseNotesService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())
        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        val created = service.createAttendance(CreateAttendanceDto
                .builder()
                .absentReason(AbsentReason.Refused)
                .attended(false)
                .paid(false)
                .bookingId(1)
                .comments("test comments")
                .eventId(1)
                .eventLocationId(2)
                .period(TimePeriod.AM)
                .prisonId("LEI")
                .eventDate(LocalDate.now())
                .build())

        assertThat(created).isEqualTo(AttendanceDto
                .builder()
                .attended(false)
                .paid(false)
                .comments(null)
                .caseNoteId(100L)
                .absentReason(AbsentReason.Refused)
                .eventId(1)
                .eventLocationId(2)
                .eventDate(LocalDate.now())
                .prisonId("LEI")
                .bookingId(1)
                .period(TimePeriod.AM)
                .eventDate(LocalDate.now())
                .comments("test comments")
                .locked(false)
                .build())
    }


    @Test
    fun `should post case note amendment going from unpaid absent refused to paid attendance`() {

        `when`(attendanceRepository.findById(1))
                .thenReturn(Optional.of(
                        Attendance.builder()
                                .bookingId(1)
                                .eventLocationId(1)
                                .eventId(1)
                                .comments("Refused to turn up")
                                .paid(false)
                                .attended(false)
                                .absentReason(AbsentReason.Refused)
                                .eventDate(today)
                                .caseNoteId(1)
                                .build()))
        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        service.updateAttendance(1, UpdateAttendanceDto.builder().attended(true).paid(true).build())

        verify(caseNotesService)
                .putCaseNoteAmendment("AB1234C", 1, "IEP rescinded: attended")
    }

    @Test
    fun `should post IEP reinstated case note amendment if going from unpaid (IEP warning) to paid attendance (IEP rescinded) to unpaid absent unacceptable`() {

        `when`(attendanceRepository.findById(1))
                .thenReturn(Optional.of(
                        Attendance.builder()
                                .bookingId(1)
                                .eventLocationId(1)
                                .eventId(1)
                                .paid(true)
                                .attended(true)
                                .eventDate(today)
                                .caseNoteId(1)
                                .build()))
        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .paid(false)
                .absentReason(AbsentReason.UnacceptableAbsence)
                .comments("Unacceptable absence - No show.")
                .build())

        verify(caseNotesService)
                .putCaseNoteAmendment("AB1234C", 1, "IEP reinstated: Unacceptable absence")
    }

    @Test
    fun `should not post a case note amendment going from paid attendance to unpaid absent refused`() {

        `when`(caseNotesService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(1).build())
        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        `when`(attendanceRepository.findById(1))
                .thenReturn(Optional.of(Attendance.builder()
                        .bookingId(1)
                        .eventLocationId(1)
                        .eventId(1)
                        .paid(true)
                        .attended(true)
                        .eventDate(today)
                        .build()))

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .paid(false)
                .absentReason(AbsentReason.Refused)
                .comments("Refused!")
                .build())

        verify(caseNotesService, never()).putCaseNoteAmendment(anyString(), anyLong(), ArgumentMatchers.anyString())
        verify(caseNotesService).postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
    }

    @Test
    fun `should sentence case absent reasons`() {
        `when`(attendanceRepository.findById(1))
                .thenReturn(Optional.of(Attendance.builder()
                        .bookingId(1)
                        .eventLocationId(1)
                        .eventId(1)
                        .paid(false)
                        .attended(false)
                        .caseNoteId(1)
                        .absentReason(AbsentReason.Refused)
                        .eventDate(today)
                        .build()))
        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .paid(true)
                .absentReason(AbsentReason.NotRequired)
                .comments("not required to work today")
                .build())

        verify(caseNotesService).putCaseNoteAmendment("AB1234C", 1, "IEP rescinded: Not required")
    }

    @Test
    fun `should not raise case note or amendment IEP warnings`() {

        `when`(attendanceRepository.findById(1))
                .thenReturn(Optional.of(Attendance.builder()
                        .bookingId(1)
                        .eventLocationId(1)
                        .eventId(1)
                        .eventDate(today)
                        .paid(true)
                        .attended(true)
                        .build()))

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .absentReason(AbsentReason.NotRequired)
                .paid(true)
                .build())

        verify(caseNotesService, never()).putCaseNoteAmendment(anyString(), anyLong(), anyString())
        verify(caseNotesService, never()).postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
    }

    @Test
    fun `should not raise case note or amendment IEP warnings going from unpaid to unpaid`() {

        `when`(attendanceRepository.findById(1))
                .thenReturn(Optional.of(Attendance.builder()
                        .bookingId(1)
                        .eventLocationId(1)
                        .eventId(1)
                        .eventDate(today)
                        .absentReason(AbsentReason.AcceptableAbsence)
                        .paid(false)
                        .attended(false)
                        .build()))

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .absentReason(AbsentReason.NotRequired)
                .paid(true)
                .build())

        verify(caseNotesService, never()).putCaseNoteAmendment(anyString(), anyLong(), anyString())
        verify(caseNotesService, never()).postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
    }

    @Test
    fun `should not trigger an IEP warning if one was previously triggered`() {

        val attendanceEntity = Attendance.builder()
                .bookingId(1)
                .eventLocationId(1)
                .eventId(1)
                .eventDate(today)
                .paid(false)
                .attended(false)
                .caseNoteId(1)
                .comments("do not overwrite")
                .absentReason(AbsentReason.Refused)
                .build()

        `when`(attendanceRepository.findById(1)).thenReturn(Optional.of(attendanceEntity.toBuilder().build()))

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .absentReason(AbsentReason.Refused)
                .comments("Never turned up")
                .paid(false)
                .build())

        verify(caseNotesService, never()).putCaseNoteAmendment(anyString(), anyLong(), anyString())
        verify(caseNotesService, never()).postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
        verify(attendanceRepository).save(attendanceEntity.toBuilder().comments("Never turned up").build())
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
                .build()

        `when`(attendanceRepository.findById(1)).thenReturn(Optional.of(attendanceEntity.toBuilder().build()))
        `when`(elite2ApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(true)
                .paid(true)
                .build())

        verify(caseNotesService).putCaseNoteAmendment("AB1234C", 1, "IEP rescinded: attended")
        verify(caseNotesService, never()).postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
        verify(attendanceRepository).save(attendanceEntity
                .toBuilder()
                .comments(null)
                .absentReason(null)
                .attended(true)
                .paid(true)
                .build()
        )
    }

    @Test
    fun `should load attendance details for a set of booking ids`() {
        val today = LocalDate.now()
        val bookingIds = setOf(1L, 2L)

        service.getAttendanceForBookings("LEI", bookingIds, today, TimePeriod.AM)

        verify(attendanceRepository).findByPrisonIdAndBookingIdInAndEventDateAndPeriod("LEI", setOf(1, 2),
                today, TimePeriod.AM)
    }

    @Test
    fun `should find attendance with absent reason given some criteria`() {

        val now = LocalDateTime.now()

        `when`(attendanceRepository
                .findByPrisonIdAndEventDateAndPeriodAndAbsentReasonNotNull("LEI", today, TimePeriod.AM))
                .thenReturn(setOf(
                        Attendance.builder()
                                .id(1)
                                .absentReason(AbsentReason.Refused)
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
                        Attendance.builder()
                                .id(1)
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
                                .build()
                ))

        val result = service.getAbsences("LEI", today, TimePeriod.AM)

        assertThat(result).containsAnyElementsOf(mutableListOf(
                AttendanceDto
                        .builder()
                        .id(1)
                        .absentReason(AbsentReason.Refused)
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
                        .build()
        ))
    }

    @Test
    fun `should create an attendance record locally and via elite for multiple bookings`() {
        val bookingIds = setOf(1L, 2L)

        val bookingActivities = bookingIds
                .stream()
                .map { BookingActivity.builder().activityId(1L).bookingId(it).build() }
                .collect(Collectors.toSet())

        val savedAttendanceDetails = service.attendAll(
                AttendAllDto
                        .builder()
                        .eventDate(LocalDate.now().minusDays(1))
                        .eventLocationId(2L)
                        .prisonId("LEI")
                        .period(TimePeriod.AM)
                        .bookingActivities(bookingActivities)
                        .build())

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
                        .build())

        verify(attendanceRepository).saveAll(anySet())
        verify(elite2ApiService).putAttendanceForMultipleBookings(bookingActivities,
                EventOutcome("ATT", "STANDARD", ""))
    }

    @Test
    fun `should request all scheduled activity for date and period`() {
        val agencyId = "LEI"
        val date = LocalDate.now()
        val period = TimePeriod.AM

        service.getAttendanceForOffendersThatHaveScheduledActivity(agencyId, date, period)

        verify(elite2ApiService).getBookingIdsForScheduleActivities(agencyId, date, period)
    }

    @Test
    fun `should fetch attendance records for selected booking ids`() {
        val prisonId = "LEI"
        val date = LocalDate.now()
        val period = TimePeriod.AM

        `when`(elite2ApiService.getBookingIdsForScheduleActivities(prisonId, date, period)).thenReturn(setOf(1L, 2L))

        service.getAttendanceForOffendersThatHaveScheduledActivity(prisonId, date, period)
        verify(attendanceRepository).findByPrisonIdAndBookingIdInAndEventDateAndPeriod(prisonId, setOf(1L, 2L), date, period)
    }

    @Test
    fun `should return all attendance records for offenders that have scheduled activities`() {
        val prisonId = "LEI"
        val date = LocalDate.now()
        val period = TimePeriod.AM

        `when`(elite2ApiService.getBookingIdsForScheduleActivities(prisonId, date, period)).thenReturn(setOf(1L, 2L))

        `when`(attendanceRepository
                .findByPrisonIdAndBookingIdInAndEventDateAndPeriod(prisonId, setOf(1L, 2L), date, period))
                .thenReturn(setOf(
                        Attendance.builder()
                                .id(1)
                                .absentReason(AbsentReason.Refused)
                                .attended(false)
                                .paid(false)
                                .eventId(2)
                                .eventLocationId(3)
                                .period(TimePeriod.AM)
                                .prisonId("MDI")
                                .bookingId(1L)
                                .eventDate(today)
                                .createUserId("user")
                                .caseNoteId(1)
                                .build(),
                        Attendance.builder()
                                .id(2)
                                .attended(true)
                                .paid(true)
                                .eventId(2)
                                .eventLocationId(3)
                                .period(TimePeriod.AM)
                                .prisonId("MDI")
                                .bookingId(2L)
                                .eventDate(today)
                                .createUserId("user")
                                .build()
                ))

        val attendances =
                service.getAttendanceForOffendersThatHaveScheduledActivity(prisonId, date, period)

        assertThat(attendances).containsExactlyInAnyOrderElementsOf(
                setOf(
                        AttendanceDto
                                .builder()
                                .id(2)
                                .attended(true)
                                .paid(true)
                                .eventId(2)
                                .eventLocationId(3)
                                .period(TimePeriod.AM)
                                .prisonId("MDI")
                                .bookingId(2L)
                                .eventDate(date)
                                .createUserId("user")
                                .locked(false)
                                .build(),
                        AttendanceDto
                                .builder()
                                .id(1)
                                .absentReason(AbsentReason.Refused)
                                .attended(false)
                                .paid(false)
                                .eventId(2)
                                .eventLocationId(3)
                                .period(TimePeriod.AM)
                                .prisonId("MDI")
                                .bookingId(1L)
                                .eventDate(date)
                                .createUserId("user")
                                .locked(false)
                                .caseNoteId(1)
                                .build()
                )
        )
    }
}
