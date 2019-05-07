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
    var attendanceRepository: AttendanceRepository? = null

    @Test
    fun `should find attendance given some criteria`() {
        val today = LocalDate.now()

        `when`(attendanceRepository!!.findByPrisonIdAndEventLocationIdAndEventDateAndPeriod("LEI", 1, today, TimePeriod.AM))
                .thenReturn(setOf(
                        Attendance.
                                builder()
                                .id(1)
                                .absentReason(AbsentReason.Refused)
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

        val service =  AttendanceService(attendanceRepository)

        val result = service.getAttendance("LEI" , 1, today, TimePeriod.AM)

        assertThat(result).containsAnyElementsOf(mutableListOf(
                AttendanceDto
                        .builder()
                        .id(1)
                        .absentReason(AbsentReason.Refused)
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
        val service = AttendanceService(attendanceRepository)

        service.updateOffenderAttendance(AttendanceDto
                .builder()
                .absentReason(AbsentReason.Refused)
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
                .absentReason(AbsentReason.Refused)
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
}
