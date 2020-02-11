package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.verify
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
          .period(TimePeriod.AM)
          .build(),
      Attendance.builder()
          .id(2)
          .bookingId(2)
          .attended(false)
          .absentReason(AbsentReason.ApprovedCourse)
          .period(TimePeriod.AM)
          .paid(true)
          .build(),
      Attendance.builder()
          .id(3)
          .bookingId(3)
          .attended(false)
          .absentReason(AbsentReason.AcceptableAbsence)
          .paid(true)
          .period(TimePeriod.AM)
          .build(),
      Attendance.builder()
          .id(4)
          .bookingId(4)
          .attended(false)
          .absentReason(AbsentReason.NotRequired)
          .paid(true)
          .period(TimePeriod.AM)
          .build(),
      Attendance.builder()
          .id(5)
          .bookingId(5)
          .attended(false)
          .absentReason(AbsentReason.Refused)
          .paid(false)
          .period(TimePeriod.AM)
          .build(),
      Attendance.builder()
          .id(7)
          .bookingId(7)
          .attended(false)
          .absentReason(AbsentReason.SessionCancelled)
          .paid(false)
          .period(TimePeriod.AM)
          .build(),
      Attendance.builder()
          .id(9)
          .bookingId(9)
          .attended(false)
          .absentReason(AbsentReason.UnacceptableAbsence)
          .paid(false)
          .period(TimePeriod.AM)
          .build(),
      Attendance.builder()
          .id(11)
          .bookingId(9)
          .attended(false)
          .absentReason(AbsentReason.RestInCellOrSick)
          .paid(false)
          .period(TimePeriod.AM)
          .build(),
      Attendance.builder()
          .id(11)
          .bookingId(9)
          .attended(false)
          .absentReason(AbsentReason.RefusedIncentiveLevelWarning)
          .paid(false)
          .period(TimePeriod.AM)
          .build()
  )

  @Test
  fun `count attendances`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("paidReasons").extracting("attended").isEqualTo(1)
  }

  @Test
  fun `count acceptable absences`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("paidReasons").extracting("acceptableAbsence").isEqualTo(1)
  }

  @Test
  fun `count approved course`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("paidReasons").extracting("approvedCourse").isEqualTo(1)
  }

  @Test
  fun `count not required`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("paidReasons").extracting("notRequired").isEqualTo(1)
  }

  @Test
  fun `count refused`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("unpaidReasons").extracting("refused").isEqualTo(1)
  }

  @Test
  fun `count session cancelled`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("unpaidReasons").extracting("sessionCancelled").isEqualTo(1)
  }

  @Test
  fun `count unacceptable absence`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("unpaidReasons").extracting("unacceptableAbsence").isEqualTo(1)
  }

  @Test
  fun `count not recorded`() {
    whenever(elite2ApiService.getBookingIdsForScheduleActivitiesByDateRange(anyString(), any(), any(), any()))
        .thenReturn(listOf(1, 2, 3, 100, 102))

    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("notRecorded").isEqualTo(2)
  }

  @Test
  fun `count offender schedules`() {
    whenever(elite2ApiService.getBookingIdsForScheduleActivitiesByDateRange(anyString(), any(), any(), any()))
        .thenReturn(listOf(1, 2, 3, 100, 102, 100, 100, 100))

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, TimePeriod.AM, from, to)

    assertThat(stats).extracting("scheduleActivities").isEqualTo(8)
  }

  @Test
  fun `count rest in cell or sick`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("unpaidReasons").extracting("restInCellOrSick").isEqualTo(1)
  }


  @Test
  fun `count refusedIncentiveLevelWarning`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("unpaidReasons").extracting("refusedIncentiveLevelWarning").isEqualTo(1)
  }


  @Test
  fun `should call the correct repository method when period all is supplied`() {
    val service = buildAttendanceStatistics()

    service.getStats(prisonId, null, from, to)

    verify(attendanceRepository)
        .findByPrisonIdAndEventDateBetweenAndPeriodIn(prisonId, from, to, setOf(TimePeriod.AM, TimePeriod.PM))
  }

  @Test
  fun `should call the elite2 schedule api twice, once for AM and then for PM`() {
    val service = buildAttendanceStatistics()

    whenever(elite2ApiService
        .getBookingIdsForScheduleActivitiesByDateRange(prisonId, TimePeriod.AM, from, to))
        .thenReturn(listOf(1, 2))

    whenever(elite2ApiService
        .getBookingIdsForScheduleActivitiesByDateRange(prisonId, TimePeriod.PM, from, to))
        .thenReturn(listOf(1, 2))

    val stats = service.getStats(prisonId, null, from, to)

    verify(elite2ApiService)
        .getBookingIdsForScheduleActivitiesByDateRange(prisonId, TimePeriod.AM, from, to)

    verify(elite2ApiService)
        .getBookingIdsForScheduleActivitiesByDateRange(prisonId, TimePeriod.PM, from, to)

    assertThat(stats).extracting("notRecorded").isEqualTo(4)
  }

  @Test
  fun `should return correct number of not recorded when AM and PM selected`() {
    val service = buildAttendanceStatistics()

    whenever(attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodIn(prisonId, from, to, setOf(TimePeriod.AM, TimePeriod.PM)))
            .thenReturn(attendances)

    // Return the same booking ids for AM and PM. These booking ids have attendances in the AM
    // but not in the PM. We expect the not recorded count to take into account the missing PM data
    // as it is meant to be cumulative
    whenever(elite2ApiService
            .getBookingIdsForScheduleActivitiesByDateRange(prisonId, TimePeriod.AM, from, to))
            .thenReturn(listOf(1, 2))

    whenever(elite2ApiService
            .getBookingIdsForScheduleActivitiesByDateRange(prisonId, TimePeriod.PM, from, to))
            .thenReturn(listOf(1, 2))

    val stats = service.getStats(prisonId, null, from, to)

    assertThat(stats).extracting("notRecorded").isEqualTo(2)
  }


  private fun buildAttendanceStatistics() = AttendanceStatistics(attendanceRepository, elite2ApiService)
}
