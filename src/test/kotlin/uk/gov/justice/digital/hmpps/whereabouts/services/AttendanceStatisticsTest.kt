package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.junit.MockitoJUnitRunner
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate

@RunWith(MockitoJUnitRunner::class)
class AttendanceStatisticsTest {
  @Mock
  private lateinit var attendanceRepository: AttendanceRepository

  @Mock
  private lateinit var elite2ApiService: Elite2ApiService

  private val prisonId = "LEI"
  private val period = TimePeriod.AM
  private val from = LocalDate.now()
  private val to = LocalDate.now()

  private val attendances = setOf(
      Attendance.builder()
          .id(1)
          .bookingId(1)
          .attended(true)
          .paid(true)
          .build(),
      Attendance.builder()
          .id(2)
          .bookingId(2)
          .attended(false)
          .absentReason(AbsentReason.ApprovedCourse)
          .paid(true)
          .build(),
      Attendance.builder()
          .id(3)
          .bookingId(3)
          .attended(false)
          .absentReason(AbsentReason.AcceptableAbsence)
          .paid(true)
          .build(),
      Attendance.builder()
          .id(4)
          .bookingId(4)
          .attended(false)
          .absentReason(AbsentReason.NotRequired)
          .paid(true)
          .build(),
      Attendance.builder()
          .id(5)
          .bookingId(5)
          .attended(false)
          .absentReason(AbsentReason.Refused)
          .paid(false)
          .build(),
      Attendance.builder()
          .id(6)
          .bookingId(6)
          .attended(false)
          .absentReason(AbsentReason.RestDay)
          .paid(false)
          .build(),
      Attendance.builder()
          .id(7)
          .bookingId(7)
          .attended(false)
          .absentReason(AbsentReason.SessionCancelled)
          .paid(false)
          .build(),
      Attendance.builder()
          .id(8)
          .bookingId(8)
          .attended(false)
          .absentReason(AbsentReason.Sick)
          .paid(false)
          .build(),
      Attendance.builder()
          .id(9)
          .bookingId(9)
          .attended(false)
          .absentReason(AbsentReason.UnacceptableAbsence)
          .paid(false)
          .build()
  )

  @Test
  fun `count attendances`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("paidReasons").extracting("attended").contains(1)
  }

  @Test
  fun `count acceptable absences`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("paidReasons").extracting("acceptableAbsences").contains(1)
  }

  @Test
  fun `count approved course`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("paidReasons").extracting("approvedCourse").contains(1)
  }

  @Test
  fun `count not required`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("paidReasons").extracting("notRequired").contains(1)
  }

  @Test
  fun `count refused`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("unpaidReasons").extracting("refused").contains(1)
  }

  @Test
  fun `count rest day`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("unpaidReasons").extracting("restDay").contains(1)
  }

  @Test
  fun `count session cancelled`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("unpaidReasons").extracting("sessionCancelled").contains(1)
  }

  @Test
  fun `count sick`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("unpaidReasons").extracting("sick").contains(1)
  }


  @Test
  fun `count unacceptable absence`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("unpaidReasons").extracting("unacceptableAbsence").contains(1)
  }

  @Test
  fun `count not recorded`() {
    whenever(elite2ApiService.getBookingIdsForScheduleActivitiesByDateRange(anyString(), any(), any(), any()))
        .thenReturn(setOf(1, 2, 3, 100, 102))

    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("notRecorded").contains(2)
  }

  @Test
  fun `count offender schedules`() {
    whenever(elite2ApiService.getBookingIdsForScheduleActivitiesByDateRange(anyString(), any(), any(), any()))
        .thenReturn(setOf(1, 2, 3, 100, 102, 100, 100, 100))

    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(setOf())

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("offenderSchedules").contains(5)
  }

  private fun buildAttendanceStatistics() = AttendanceStatistics(attendanceRepository, elite2ApiService)
}
