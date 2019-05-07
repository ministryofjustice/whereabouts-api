package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.runners.MockitoJUnitRunner
import uk.gov.justice.digital.hmpps.whereabouts.dto.AbsentReasonDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AbsentReasonsRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate


@RunWith(MockitoJUnitRunner::class)
class AttendanceServiceTest {
    @Mock
    var attendanceRepository: AttendanceRepository? = null

    @Mock
    var absentReasonsRepository: AbsentReasonsRepository? = null

    @Test
    fun `should find attendance given some criteria`() {
        val today = LocalDate.now()

        `when`(attendanceRepository!!.findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, today, TimePeriod.AM))
                .thenReturn(setOf(
                        Attendance.
                                builder()
                                .id(1)
                                .absentReasonId(1)
                                .attended(true)
                                .paid(false)
                                .eventId(2)
                                .eventLocationId(3)
                                .period(TimePeriod.AM)
                                .prisonId("LEI")
                                .offenderBookingId(100)
                                .eventDate(today)
                                .build()
                ))

        val service =  AttendanceService(attendanceRepository, absentReasonsRepository)

        val result = service.getAttendance("LEI" , 1, today, TimePeriod.AM)

        assertThat(result).containsAnyElementsOf(mutableListOf(
                AttendanceDto
                        .builder()
                        .id(1)
                        .absentReasonId(1)
                        .attended(true)
                        .paid(false)
                        .eventId(2)
                        .eventLocationId(3)
                        .period(TimePeriod.AM.toString())
                        .prisonId("LEI")
                        .bookingId(100)
                        .eventDate(today)
                        .build()
        ))
    }

    @Test
    fun `should create an attendance record`() {
        val today = LocalDate.now()
        val service = AttendanceService(attendanceRepository, absentReasonsRepository)

        service.updateOffenderAttendance(AttendanceDto
                .builder()
                .absentReasonId(1)
                .attended(true)
                .paid(false)
                .eventId(2)
                .eventLocationId(3)
                .period(TimePeriod.AM.toString())
                .prisonId("LEI")
                .bookingId(100)
                .eventDate(today)
                .build())


        verify(attendanceRepository)?.save(Attendance.
                builder()
                .absentReasonId(1)
                .attended(true)
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
    fun `should return absent reasons`() {
        `when`(absentReasonsRepository?.findAll()).thenReturn(setOf(
                AbsentReason
                        .builder()
                        .id(1)
                        .pnomisCode("CODE1")
                        .reason("Rest in cell")
                        .paidReason(true)
                        .build()
        ))

        val service = AttendanceService(attendanceRepository, absentReasonsRepository)
        val result = service.getAbsentReasons()

        assertThat(result).containsExactlyInAnyOrder(AbsentReasonDto.builder()
                .id(1)
                .reason("Rest in cell")
                .paidReason(true)
                .build()
        )
    }
}
