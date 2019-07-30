package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.elite.CaseNoteDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class AttendanceServiceTest {
    @Spy
    lateinit var attendanceRepository: AttendanceRepository

    @Mock
    private lateinit var nomisService: NomisService

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

    init {
        SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("user", "pw")
    }

    @Before
    fun before() {
        // return the attendance entity on save
        doAnswer { it.getArgument(0) as Attendance }.`when`(attendanceRepository).save(any())
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

        val service = AttendanceService(attendanceRepository, nomisService)

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
                        Attendance.
                                builder()
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

        val service =  AttendanceService(attendanceRepository, nomisService)

        val result = service.getAttendanceForEventLocation("LEI" , 1, sevenDaysAgoTime.toLocalDate(), TimePeriod.AM)

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
                        Attendance.
                                builder()
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

        val service =  AttendanceService(attendanceRepository, nomisService)

        val result = service.getAttendanceForEventLocation("LEI" , 1, sixDaysAgoTime.toLocalDate(), TimePeriod.AM)

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
                        Attendance.
                                builder()
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

        val service =  AttendanceService(attendanceRepository, nomisService)

        val result = service.getAttendanceForEventLocation("LEI" , 1, yesterdayTime.toLocalDate(), TimePeriod.AM)

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

        val service = AttendanceService(attendanceRepository, nomisService)

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

        val service = AttendanceService(attendanceRepository, nomisService)

        val attendance = testAttendanceDto
                .toBuilder()
                .attended(true)
                .paid(true)
                .absentReason(null)
                .build()

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("ATT", "STANDARD", attendance.comments))

    }


    @Test
    fun `should record paid absence for 'acceptable absence'`() {

        val service = AttendanceService(attendanceRepository, nomisService)

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.AcceptableAbsence)
                .attended(false)
                .paid(true)
                .build()

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("ACCAB", null, "hello"))
    }

    @Test
    fun `should record paid absence for 'not required'`() {

        val service = AttendanceService(attendanceRepository, nomisService)

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.NotRequired)
                .attended(false)
                .paid(true)
                .build()

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("NREQ", null, "hello"))
    }

    @Test
    fun `should record unpaid absence for 'session cancelled'`() {

        val service = AttendanceService(attendanceRepository, nomisService)

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.SessionCancelled)
                .attended(false)
                .paid(false)
                .build()

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("CANC", null, "hello"))
    }

    @Test
    fun `should record unpaid absence for 'rest in cell'`() {

        val service = AttendanceService(attendanceRepository, nomisService)

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.RestInCell)
                .attended(false)
                .paid(false)
                .build()

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("REST", null, "hello"))
    }

    @Test
    fun `should record unpaid absence for 'Sick'`() {

        val service = AttendanceService(attendanceRepository, nomisService)

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.Sick)
                .attended(false)
                .paid(false)
                .build()

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("REST", null, "hello"))
    }

    @Test
    fun `should record unpaid absence for 'Rest day'`() {

        val service = AttendanceService(attendanceRepository, nomisService)

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.RestDay)
                .attended(false)
                .paid(false)
                .build()

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("REST", null, "hello"))
    }

    @Test
    fun `should record unpaid absence as 'Refused'`() {

        `when`(nomisService.postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.Refused)
                .attended(false)
                .paid(false)
                .build()

        val service = AttendanceService(attendanceRepository, nomisService)

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("UNACAB", null, "hello"))
    }

    @Test
    fun `should record unpaid absence for 'Unacceptable absence'`() {

        `when`(nomisService.postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.UnacceptableAbsence)
                .attended(false)
                .paid(false)
                .build()


        val service = AttendanceService(attendanceRepository, nomisService)

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("UNACAB", null, "hello"))
    }

    @Test
    fun `should create negative case note for 'Unacceptable absence'`() {

        `when`(nomisService.postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.UnacceptableAbsence)
                .attended(false)
                .paid(false)
                .build()

        val service = AttendanceService(attendanceRepository, nomisService)

        service.createAttendance(attendance)

        verify(nomisService)
                .postCaseNote(
                        eq(attendance.bookingId),
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

        `when`(nomisService.postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        val service = AttendanceService(attendanceRepository, nomisService)

        service.createAttendance(attendance)

        verify(nomisService)
                .postCaseNote(
                        eq(attendance.bookingId),
                        eq("NEG"),
                        eq("IEP_WARN"),
                        eq("Refused - hello"),
                        isA(LocalDateTime::class.java))
    }

    @Test
    fun `should create a negative case note using user supplied comment`() {

        val service = AttendanceService(attendanceRepository, nomisService)

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.Refused)
                .attended(false)
                .comments("test comment")
                .paid(false)
                .build()

        `when`(nomisService.postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        service.createAttendance(attendance)

        verify(nomisService)
                .postCaseNote(
                        eq(attendance.bookingId),
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


        val service = AttendanceService(attendanceRepository, nomisService)

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("ACCAB", null, "hello"))
    }


    @Test
    fun `should save case note id returned from the postCaseNote api call`() {

        `when`(nomisService.postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        val service = AttendanceService(attendanceRepository, nomisService)

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

        val service = AttendanceService(attendanceRepository, nomisService)

        Assertions.assertThatThrownBy {
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

        val service = AttendanceService(attendanceRepository, nomisService)

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

        val service = AttendanceService(attendanceRepository, nomisService)

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
    fun `should throw an AttendanceLocked`() {
        val yesterday = LocalDate.now().minusDays(1)

        `when`(attendanceRepository.findById(1)).thenReturn(
                Optional.of(Attendance
                        .builder()
                        .id(1)
                        .attended(true)
                        .paid(true)
                        .eventId(1)
                        .eventLocationId(2)
                        .eventDate(yesterday)
                        .prisonId("LEI")
                        .bookingId(1)
                        .period(TimePeriod.AM)
                        .build()))

        val service = AttendanceService(attendanceRepository, nomisService)

        Assertions.assertThatThrownBy {
            service.updateAttendance(1, UpdateAttendanceDto.builder().paid(false).attended(false).build())
        }.isExactlyInstanceOf(AttendanceLocked::class.java)
    }

    @Test
    fun `should return attendance dto on creation`() {

        `when`(nomisService.postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        val service = AttendanceService(attendanceRepository, nomisService)
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

        val service = AttendanceService(attendanceRepository, nomisService)

        service.updateAttendance(1, UpdateAttendanceDto.builder().attended(true).paid(true).build())

        verify(nomisService)
                .putCaseNoteAmendment(1, 1, "IEP rescinded: attended")
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

        val service = AttendanceService(attendanceRepository, nomisService)

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .paid(false)
                .absentReason(AbsentReason.UnacceptableAbsence)
                .comments("Unacceptable absence - No show.")
                .build())

        verify(nomisService)
                .putCaseNoteAmendment(1, 1, "IEP reinstated: Unacceptable absence")
    }

    @Test
    fun `should not post a case note amendment going from paid attendance to unpaid absent refused`() {

        `when`(nomisService.postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
                .thenReturn(CaseNoteDto.builder().caseNoteId(1).build())

        `when`(attendanceRepository.findById(1))
                .thenReturn(Optional.of(Attendance.builder()
                        .bookingId(1)
                        .eventLocationId(1)
                        .eventId(1)
                        .paid(true)
                        .attended(true)
                        .eventDate(today)
                        .build()))

        val service = AttendanceService(attendanceRepository, nomisService)

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .paid(false)
                .absentReason(AbsentReason.Refused)
                .comments("Refused!")
                .build())

        verify(nomisService, never()).putCaseNoteAmendment(ArgumentMatchers.anyLong(), anyLong(), ArgumentMatchers.anyString())
        verify(nomisService).postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
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

        val service = AttendanceService(attendanceRepository, nomisService)

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .paid(true)
                .absentReason(AbsentReason.NotRequired)
                .comments("not required to work today")
                .build())

        verify(nomisService).putCaseNoteAmendment(1, 1, "IEP rescinded: Not required")
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

        val service = AttendanceService(attendanceRepository, nomisService)

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .absentReason(AbsentReason.NotRequired)
                .paid(true)
                .build())

        verify(nomisService, never()).putCaseNoteAmendment(anyLong(), anyLong(), anyString())
        verify(nomisService, never()).postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
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

        val service = AttendanceService(attendanceRepository, nomisService)

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .absentReason(AbsentReason.NotRequired)
                .paid(true)
                .build())

        verify(nomisService, never()).putCaseNoteAmendment(anyLong(), anyLong(), anyString())
        verify(nomisService, never()).postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
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

        val service = AttendanceService(attendanceRepository, nomisService)

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(false)
                .absentReason(AbsentReason.Refused)
                .comments("Never turned up")
                .paid(false)
                .build())

        verify(nomisService, never()).putCaseNoteAmendment(anyLong(), anyLong(), anyString())
        verify(nomisService, never()).postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
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

        val service = AttendanceService(attendanceRepository, nomisService)

        service.updateAttendance(1, UpdateAttendanceDto.builder()
                .attended(true)
                .paid(true)
                .build())

        verify(nomisService).putCaseNoteAmendment(anyLong(), anyLong(), anyString())
        verify(nomisService, never()).postCaseNote(anyLong(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
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
        val service = AttendanceService(attendanceRepository, nomisService)
        val today = LocalDate.now()

        service.getAttendanceForBookings("LEI", setOf(1,2), today, TimePeriod.AM)

        verify(attendanceRepository).findByPrisonIdAndBookingIdInAndEventDateAndPeriod("LEI", setOf(1,2),
                today, TimePeriod.AM)
    }

    @Test
    fun `should find attendance with absent reason given some criteria`() {

        val now = LocalDateTime.now()

        `when`(attendanceRepository
                .findByPrisonIdAndEventDateAndPeriodAndAbsentReasonNotNull("LEI",  today, TimePeriod.AM))
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

        val service = AttendanceService(attendanceRepository, nomisService)

        val result = service.getAbsence("LEI", today, TimePeriod.AM)

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
}
