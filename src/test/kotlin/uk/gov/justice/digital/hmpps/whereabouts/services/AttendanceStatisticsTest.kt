@file:Suppress("ClassName")

package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate

class AttendanceStatisticsTest {
  companion object {
    @JvmStatic
    private fun getPaidReasons() = AbsentReason.paidReasons

    @JvmStatic
    private fun getUnpaidReasons() = AbsentReason.unpaidReasons
  }

  private val attendanceRepository: AttendanceRepository = mock()
  private val prisonApiService: PrisonApiService = mock()
  private val service = AttendanceStatistics(attendanceRepository, prisonApiService)
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
      .absentReason(AbsentReason.UnacceptableAbsenceIncentiveLevelWarning)
      .paid(false)
      .period(TimePeriod.AM)
      .build(),
    Attendance.builder()
      .id(10)
      .bookingId(10)
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
      .absentReason(AbsentReason.RestDay)
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
      .build(),
  )

  @Nested
  inner class getStats {
    @BeforeEach
    fun setup() {
      whenever(prisonApiService.getScheduleActivityCounts(anyString(), any(), any(), any(), any())).thenReturn(
        PrisonerActivitiesCount(0, 0, 0),
      )
    }

    @Test
    fun `count attendances`() {
      whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

      val stats = service.getStats(prisonId, period, from, to)

      assertThat(stats).extracting("attended").isEqualTo(1)
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceStatisticsTest#getPaidReasons")
    fun `count paid reasons`(reason: AbsentReason) {
      whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

      val stats = service.getStats(prisonId, period, from, to)

      assertThat(stats).extracting("paidReasons").extracting(reason.name.replaceFirstChar { it.uppercaseChar() })
        .isEqualTo(1)
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceStatisticsTest#getUnpaidReasons")
    fun `count unpaid reasons`(reason: AbsentReason) {
      whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

      val stats = service.getStats(prisonId, period, from, to)

      assertThat(stats).extracting("unpaidReasons").extracting(reason.name.replaceFirstChar { it.uppercaseChar() })
        .isEqualTo(1)
    }

    @Test
    fun `count not recorded`() {
      whenever(prisonApiService.getScheduleActivityCounts(anyString(), any(), any(), any(), any())).thenReturn(
        PrisonerActivitiesCount(13, 1, 2),
      )

      whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

      val stats = service.getStats(prisonId, period, from, to)

      assertThat(stats).extracting("notRecorded").isEqualTo(2)
    }

    @Test
    fun `count offender schedules`() {
      whenever(prisonApiService.getScheduleActivityCounts(anyString(), any(), any(), any(), any())).thenReturn(
        PrisonerActivitiesCount(8, 1, 0),
      )
      val stats = service.getStats(prisonId, TimePeriod.AM, from, to)

      assertThat(stats).extracting("scheduleActivities").isEqualTo(8)
    }

    @Test
    fun `count rest in cell or sick`() {
      whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

      val stats = service.getStats(prisonId, period, from, to)

      assertThat(stats).extracting("unpaidReasons").extracting("restInCellOrSick").isEqualTo(1)
    }

    @Test
    fun `count rest day`() {
      whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

      val stats = service.getStats(prisonId, period, from, to)

      assertThat(stats).extracting("unpaidReasons").extracting("restDay").isEqualTo(1)
    }

    @Test
    fun `count refusedIncentiveLevelWarning`() {
      whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
        .thenReturn(attendances)

      val stats = service.getStats(prisonId, period, from, to)

      assertThat(stats).extracting("unpaidReasons").extracting("refusedIncentiveLevelWarning").isEqualTo(1)
    }

    @Test
    fun `should call the correct repository method when period all is supplied`() {
      service.getStats(prisonId, null, from, to)

      verify(attendanceRepository)
        .findByPrisonIdAndEventDateBetweenAndPeriodIn(prisonId, from, to, setOf(TimePeriod.AM, TimePeriod.PM))
    }

    @Test
    fun `should return the correct number of suspended`() {
      whenever(
        attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodIn(
          prisonId,
          from,
          to,
          setOf(TimePeriod.AM, TimePeriod.PM),
        ),
      )
        .thenReturn(attendances)

      whenever(prisonApiService.getScheduleActivityCounts(anyString(), any(), any(), any(), any())).thenReturn(
        PrisonerActivitiesCount(5, 2, 3),
      )

      val stats = service.getStats(prisonId, null, from, to)

      assertThat(stats).extracting("suspended").isEqualTo(2)
    }

    @Test
    fun `should call Prison API passing through the attendances`() {
      whenever(
        attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodIn(
          prisonId,
          from,
          to,
          setOf(TimePeriod.AM, TimePeriod.PM),
        ),
      )
        .thenReturn(attendances.filter { setOf(1L, 2L, 3L).contains(it.bookingId) }.toSet())

      val stats = service.getStats(prisonId, null, from, to)

      verify(prisonApiService).getScheduleActivityCounts(
        prisonId,
        from,
        to,
        setOf(TimePeriod.AM, TimePeriod.PM),
        mapOf(
          1L to 1,
          2L to 1,
          3L to 1,
        ),
      )
    }
  }
}
