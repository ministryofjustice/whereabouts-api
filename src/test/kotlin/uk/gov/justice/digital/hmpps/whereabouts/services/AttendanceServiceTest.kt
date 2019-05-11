package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.runners.MockitoJUnitRunner
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate


@RunWith(MockitoJUnitRunner::class)
class AttendanceServiceTest {
    @Mock
    lateinit var attendanceRepository: AttendanceRepository

    @Mock
    lateinit var nomisService: NomisService

    private val today: LocalDate = LocalDate.now()
    private val testAttendanceDto: AttendanceDto =
            AttendanceDto
            .builder()
            .id(1)
            .attended(false)
            .paid(false)
            .absentReason(AbsentReason.Refused)
            .eventId(2)
            .eventLocationId(3)
            .period(TimePeriod.AM.toString())
            .prisonId("LEI")
            .bookingId(100)
            .eventDate(today)
            .build()

    @Test
    fun `should find attendance given some criteria`() {
        `when`(attendanceRepository.findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, today, TimePeriod.AM))
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
                                .build()
                ))

        val service =  AttendanceService(attendanceRepository, nomisService)

        val result = service.getAttendance("LEI" , 1, today, TimePeriod.AM)

        assertThat(result).containsAnyElementsOf(mutableListOf( testAttendanceDto ))
    }

    @Test
    fun `should create an attendance record`() {
        val service = AttendanceService(attendanceRepository, nomisService)

        service.updateOffenderAttendance(testAttendanceDto)

        verify(attendanceRepository)?.save(Attendance.
                builder()
                .absentReason(AbsentReason.Refused)
                .attended(false)
                .paid(false)
                .eventId(2)
                .eventLocationId(3)
                .period(TimePeriod.AM)
                .prisonId("LEI")
                .offenderBookingId(100)
                .eventDate(today)
                .build())

    }

    @Test
    fun `should record paid attendance` () {
        val service = AttendanceService(attendanceRepository, nomisService)

        val paidAttendance = testAttendanceDto
                .toBuilder()
                .offenderNo("A1234546")
                .attended(true)
                .paid(true)
                .absentReason(null)
                .build()

        service.updateOffenderAttendance(paidAttendance)

        verify(nomisService).updateAttendance(paidAttendance.offenderNo,
                paidAttendance.eventId,"ATT", "STANDARD")

    }


    @Test
    fun `should record paid absence for 'acceptable absence'`() {
        val service = AttendanceService(attendanceRepository, nomisService)

        val paidAttendance = testAttendanceDto
                .toBuilder()
                .offenderNo("A1234546")
                .absentReason(AbsentReason.AcceptableAbsence)
                .attended(false)
                .paid(true)
                .build()

        service.updateOffenderAttendance(paidAttendance)

        verify(nomisService).updateAttendance(paidAttendance.offenderNo,
                paidAttendance.eventId,"ACCAB", "")
    }

    @Test
    fun `should record paid absence for 'not required'`() {
        val service = AttendanceService(attendanceRepository, nomisService)

        val paidAttendance = testAttendanceDto
                .toBuilder()
                .offenderNo("A1234546")
                .absentReason(AbsentReason.NotRequired)
                .attended(false)
                .paid(true)
                .build()

        service.updateOffenderAttendance(paidAttendance)

        verify(nomisService).updateAttendance(paidAttendance.offenderNo,
                paidAttendance.eventId,"NREQ", "")
    }

    @Test
    fun `should record unpaid absence for 'session cancelled'`() {
        val service = AttendanceService(attendanceRepository, nomisService)

        val paidAttendance = testAttendanceDto
                .toBuilder()
                .offenderNo("A1234546")
                .absentReason(AbsentReason.SessionCancelled)
                .attended(false)
                .paid(false)
                .build()

        service.updateOffenderAttendance(paidAttendance)

        verify(nomisService).updateAttendance(paidAttendance.offenderNo,
                paidAttendance.eventId,"CANC", "")
    }

    @Test
    fun `should record unpaid absence for 'rest in cell'`() {
        val service = AttendanceService(attendanceRepository, nomisService)

        val paidAttendance = testAttendanceDto
                .toBuilder()
                .offenderNo("A1234546")
                .absentReason(AbsentReason.RestInCell)
                .attended(false)
                .paid(false)
                .build()

        service.updateOffenderAttendance(paidAttendance)

        verify(nomisService).updateAttendance(paidAttendance.offenderNo,
                paidAttendance.eventId,"REST", "")
    }

    @Test
    fun `should record unpaid absence for 'Sick'`() {
        val service = AttendanceService(attendanceRepository, nomisService)

        val paidAttendance = testAttendanceDto
                .toBuilder()
                .offenderNo("A1234546")
                .absentReason(AbsentReason.Sick)
                .attended(false)
                .paid(false)
                .build()

        service.updateOffenderAttendance(paidAttendance)

        verify(nomisService).updateAttendance(paidAttendance.offenderNo,
                paidAttendance.eventId,"REST", "")
    }

    @Test
    fun `should record unpaid absence for 'Rest day'`() {
        val service = AttendanceService(attendanceRepository, nomisService)

        val paidAttendance = testAttendanceDto
                .toBuilder()
                .offenderNo("A1234546")
                .absentReason(AbsentReason.RestDay)
                .attended(false)
                .paid(false)
                .build()

        service.updateOffenderAttendance(paidAttendance)

        verify(nomisService).updateAttendance(paidAttendance.offenderNo,
                paidAttendance.eventId,"REST", "")
    }

    @Test
    fun `should record unpaid absence as 'Refused'`() {
        val service = AttendanceService(attendanceRepository, nomisService)

        val paidAttendance = testAttendanceDto
                .toBuilder()
                .offenderNo("A1234546")
                .absentReason(AbsentReason.Refused)
                .attended(false)
                .paid(false)
                .build()

        service.updateOffenderAttendance(paidAttendance)

        verify(nomisService).updateAttendance(paidAttendance.offenderNo,
                paidAttendance.eventId,"UNACAB", "")
    }
    @Test
    fun `should record unpaid absence for 'Unacceptable absence'`() {
        val service = AttendanceService(attendanceRepository, nomisService)

        val paidAttendance = testAttendanceDto
                .toBuilder()
                .offenderNo("A1234546")
                .absentReason(AbsentReason.UnacceptableAbsence)
                .attended(false)
                .paid(false)
                .build()

        service.updateOffenderAttendance(paidAttendance)

        verify(nomisService).updateAttendance(paidAttendance.offenderNo,
                paidAttendance.eventId,"UNACAB", "")
    }

}
