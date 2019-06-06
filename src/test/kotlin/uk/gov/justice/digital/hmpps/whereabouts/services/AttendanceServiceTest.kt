package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.runners.MockitoJUnitRunner
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CaseNoteDto
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
    @Mock
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

    @Test
    fun `should find attendance given some criteria`() {

        val now = LocalDateTime.now()

        `when`(attendanceRepository
                .findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, today, TimePeriod.AM))
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
                                .offenderBookingId(100)
                                .eventDate(today)
                                .createUserId("user")
                                .createDateTime(now)
                                .caseNoteId(1)
                                .build()
                ))

        val service =  AttendanceService(attendanceRepository, nomisService)

        val result = service.getAttendance("LEI" , 1, today, TimePeriod.AM)

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

        verify(attendanceRepository)?.save(Attendance.
                builder()
                .attended(true)
                .paid(true)
                .eventId(2)
                .eventLocationId(3)
                .period(TimePeriod.AM)
                .prisonId("LEI")
                .offenderBookingId(100)
                .eventDate(today)
                .comments("hello")
                .build())

    }

    @Test
    fun `should record paid attendance` () {

        val service = AttendanceService(attendanceRepository, nomisService)

        val attendance = testAttendanceDto
                .toBuilder()
                .attended(true)
                .paid(true)
                .absentReason(null)
                .build()

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("ATT", "STANDARD"))

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
                attendance.eventId, EventOutcome("ACCAB", null))
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
                attendance.eventId, EventOutcome("NREQ", null))
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
                attendance.eventId, EventOutcome("CANC", null))
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
                attendance.eventId, EventOutcome("REST", null))
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
                attendance.eventId, EventOutcome("REST", null))
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
                attendance.eventId, EventOutcome("REST", null))
    }

    @Test
    fun `should record unpaid absence as 'Refused'`() {

        `when`(nomisService.postCaseNote(
                anyLong(), anyString(), anyString(),
                anyString(), anyObject())).thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.Refused)
                .attended(false)
                .paid(false)
                .build()

        val service = AttendanceService(attendanceRepository, nomisService)

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("UNACAB", null))
    }
    @Test
    fun `should record unpaid absence for 'Unacceptable absence'`() {

        `when`(nomisService.postCaseNote(
                anyLong(), anyString(), anyString(),
                anyString(), anyObject())).thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        val attendance = testAttendanceDto
                .toBuilder()
                .absentReason(AbsentReason.UnacceptableAbsence)
                .attended(false)
                .paid(false)
                .build()


        val service = AttendanceService(attendanceRepository, nomisService)

        service.createAttendance(attendance)

        verify(nomisService).putAttendance(attendance.bookingId,
                attendance.eventId, EventOutcome("UNACAB", null))
    }

    @Test
    fun `should create negative case note for 'Unacceptable absence'`() {

        `when`(nomisService.postCaseNote(
                anyLong(), anyString(), anyString(),
                anyString(), anyObject())).thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

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
                        eq("hello"),
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

        `when`(nomisService.postCaseNote(
                anyLong(), anyString(), anyString(),
                anyString(), anyObject())).thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        val service = AttendanceService(attendanceRepository, nomisService)

        service.createAttendance(attendance)

        verify(nomisService)
                .postCaseNote(
                        eq(attendance.bookingId),
                        eq("NEG"),
                        eq("IEP_WARN"),
                        eq("hello"),
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

        `when`(nomisService.postCaseNote(anyLong(), anyString(), anyString(), anyString(), anyObject()))
                .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

        service.createAttendance(attendance)

        verify(nomisService)
                .postCaseNote(
                        eq(attendance.bookingId),
                        eq("NEG"),
                        eq("IEP_WARN"),
                        eq("test comment"),
                        isA(LocalDateTime::class.java))
    }

    @Test
    fun `should save case note id returned from the postCaseNote api call`() {

        `when`(nomisService.postCaseNote(anyLong(), anyString(), anyString(), anyString(), anyObject()))
                .thenReturn(CaseNoteDto.builder().caseNoteId(1020L).build())

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
                .offenderBookingId(100L)
                .caseNoteId(1020L)
                .eventDate(today)
                .comments("hello, world")
                .build())

    }

    @Test
    fun `should update select fields only` () {

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
                        .offenderBookingId(1)
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
                .offenderBookingId(1)
                .period(TimePeriod.AM)
                .build())
    }

    @Test
    fun `should go from unpaid none attendance to paid attendance, updating `() {

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
                        .offenderBookingId(1)
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
                .offenderBookingId(1)
                .period(TimePeriod.AM)
                .build())
    }

    @Test
    fun `should throw an AttendanceNotFoundException`() {
       `when`(attendanceRepository.findById(1)).thenReturn(Optional.empty())

        val service = AttendanceService(attendanceRepository, nomisService)

        assertThatThrownBy {
            service.updateAttendance(1, UpdateAttendanceDto.builder().build())
        }.isExactlyInstanceOf(AttendanceNotFound::class.java)
    }

}
