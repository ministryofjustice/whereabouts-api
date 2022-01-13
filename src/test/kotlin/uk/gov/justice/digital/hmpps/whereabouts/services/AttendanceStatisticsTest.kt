package uk.gov.justice.digital.hmpps.whereabouts.services

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import uk.gov.justice.digital.hmpps.whereabouts.dto.OffenderDetails
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate

class AttendanceStatisticsTest {
  private val attendanceRepository: AttendanceRepository = mock()
  private val prisonApiService: PrisonApiService = mock()

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
    whenever(prisonApiService.getScheduleActivityOffenderData(anyString(), any(), any(), any())).thenReturn(
      listOf(
        OffenderDetails(
          bookingId = 1,
          offenderNo = "A12345",
          eventId = 2,
          cellLocation = "cell1",
          eventDate = from,
          timeSlot = "AM",
          comment = "Gym",
          firstName = "john",
          lastName = "doe",
          suspended = true
        ),
        OffenderDetails(
          bookingId = 2,
          offenderNo = "A12346",
          eventId = 3,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        ),
        OffenderDetails(
          bookingId = 3,
          offenderNo = "A12347",
          eventId = 4,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        ),
        OffenderDetails(
          bookingId = 100,
          offenderNo = "A12348",
          eventId = 5,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        ),
        OffenderDetails(
          bookingId = 102,
          offenderNo = "A12349",
          eventId = 6,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        )
      )
    )

    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
      .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("notRecorded").isEqualTo(2)
  }

  @Test
  fun `count offender schedules`() {
    whenever(prisonApiService.getScheduleActivityOffenderData(anyString(), any(), any(), any())).thenReturn(
      listOf(
        OffenderDetails(
          bookingId = 1,
          offenderNo = "A12345",
          eventId = 2,
          cellLocation = "cell1",
          eventDate = from,
          timeSlot = "AM",
          comment = "Gym",
          firstName = "john",
          lastName = "doe",
          suspended = true
        ),
        OffenderDetails(
          bookingId = 2,
          offenderNo = "A12346",
          eventId = 3,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        ),
        OffenderDetails(
          bookingId = 3,
          offenderNo = "A12347",
          eventId = 4,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        ),
        OffenderDetails(
          bookingId = 4,
          offenderNo = "A12348",
          eventId = 5,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        ),
        OffenderDetails(
          bookingId = 5,
          offenderNo = "A12349",
          eventId = 6,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        ),
        OffenderDetails(
          bookingId = 6,
          offenderNo = "A12340",
          eventId = 7,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        ),
        OffenderDetails(
          bookingId = 7,
          offenderNo = "A12341",
          eventId = 8,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        ),
        OffenderDetails(
          bookingId = 8,
          offenderNo = "A12342",
          eventId = 9,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        )
      )
    )

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
  fun `count rest day`() {
    whenever(attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(anyString(), any(), any(), any()))
      .thenReturn(attendances)

    val service = buildAttendanceStatistics()

    val stats = service.getStats(prisonId, period, from, to)

    assertThat(stats).extracting("unpaidReasons").extracting("restDay").isEqualTo(1)
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

    whenever(prisonApiService.getScheduleActivityOffenderData(prisonId, from, to, TimePeriod.AM)).thenReturn(
      listOf(
        OffenderDetails(
          bookingId = 1,
          offenderNo = "A12345",
          eventId = 2,
          cellLocation = "cell1",
          eventDate = from,
          timeSlot = "AM",
          comment = "Gym",
          firstName = "john",
          lastName = "doe",
          suspended = true
        ),
        OffenderDetails(
          bookingId = 2,
          offenderNo = "A12346",
          eventId = 3,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        )
      )
    )

    whenever(prisonApiService.getScheduleActivityOffenderData(prisonId, from, to, TimePeriod.PM)).thenReturn(
      listOf(
        OffenderDetails(
          bookingId = 1,
          offenderNo = "A12345",
          eventId = 4,
          eventDate = from,
          timeSlot = "PM",
          firstName = "dave",
          lastName = "doe1",
          suspended = true
        ),
        OffenderDetails(
          bookingId = 2,
          offenderNo = "A12346",
          eventId = 5,
          cellLocation = "cell4",
          eventDate = from,
          timeSlot = "PM",
          firstName = "dave",
          lastName = "doe1",
          suspended = false
        )
      )
    )

    val stats = service.getStats(prisonId, null, from, to)

    verify(prisonApiService)
      .getScheduleActivityOffenderData(prisonId, from, to, TimePeriod.AM)

    verify(prisonApiService)
      .getScheduleActivityOffenderData(prisonId, from, to, TimePeriod.PM)

    assertThat(stats).extracting("notRecorded").isEqualTo(4)
  }

  @Test
  fun `should return correct number of not recorded when AM and PM selected`() {
    val service = buildAttendanceStatistics()

    whenever(
      attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodIn(
        prisonId,
        from,
        to,
        setOf(TimePeriod.AM, TimePeriod.PM)
      )
    )
      .thenReturn(attendances)

    // Return the same booking ids for AM and PM. These booking ids have attendances in the AM
    // but not in the PM. We expect the not recorded count to take into account the missing PM data
    // as it is meant to be cumulative
    whenever(prisonApiService.getScheduleActivityOffenderData(prisonId, from, to, TimePeriod.AM)).thenReturn(
      listOf(
        OffenderDetails(
          bookingId = 1,
          offenderNo = "A12345",
          eventId = 2,
          cellLocation = "cell1",
          eventDate = from,
          timeSlot = "AM",
          comment = "Gym",
          firstName = "john",
          lastName = "doe",
          suspended = true
        ),
        OffenderDetails(
          bookingId = 2,
          offenderNo = "A12346",
          eventId = 3,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        )
      )
    )

    whenever(prisonApiService.getScheduleActivityOffenderData(prisonId, from, to, TimePeriod.PM)).thenReturn(
      listOf(
        OffenderDetails(
          bookingId = 1,
          offenderNo = "A12345",
          eventId = 4,
          eventDate = from,
          timeSlot = "PM",
          firstName = "dave",
          lastName = "doe1",
          suspended = true
        ),
        OffenderDetails(
          bookingId = 2,
          offenderNo = "A12346",
          eventId = 5,
          cellLocation = "cell4",
          eventDate = from,
          timeSlot = "PM",
          firstName = "dave",
          lastName = "doe1",
          suspended = false
        )
      )
    )

    val stats = service.getStats(prisonId, null, from, to)

    assertThat(stats).extracting("notRecorded").isEqualTo(2)
  }

  @Test
  fun `should return the correct number of suspended`() {
    val service = buildAttendanceStatistics()

    whenever(
      attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodIn(
        prisonId,
        from,
        to,
        setOf(TimePeriod.AM, TimePeriod.PM)
      )
    )
      .thenReturn(attendances)

    whenever(prisonApiService.getScheduleActivityOffenderData(prisonId, from, to, TimePeriod.AM)).thenReturn(
      listOf(
        OffenderDetails(
          bookingId = 1,
          offenderNo = "A12345",
          eventId = 2,
          cellLocation = "cell1",
          eventDate = from,
          timeSlot = "AM",
          comment = "Gym",
          firstName = "john",
          lastName = "doe",
          suspended = true
        ),
        OffenderDetails(
          bookingId = 2,
          offenderNo = "A12346",
          eventId = 3,
          cellLocation = "cell2",
          eventDate = from,
          timeSlot = "AM",
          comment = "Workshop 1",
          firstName = "john",
          lastName = "doe",
          suspended = false
        )
      )
    )

    whenever(prisonApiService.getScheduleActivityOffenderData(prisonId, from, to, TimePeriod.PM)).thenReturn(
      listOf(
        OffenderDetails(
          bookingId = 1,
          offenderNo = "A12345",
          eventId = 4,
          eventDate = from,
          timeSlot = "PM",
          firstName = "dave",
          lastName = "doe1",
          suspended = true
        ),
        OffenderDetails(
          bookingId = 2,
          offenderNo = "A12346",
          eventId = 5,
          cellLocation = "cell4",
          eventDate = from,
          timeSlot = "PM",
          firstName = "dave",
          lastName = "doe1",
          suspended = false
        )
      )
    )

    val stats = service.getStats(prisonId, null, from, to)

    assertThat(stats).extracting("suspended").isEqualTo(2)
  }

  private fun buildAttendanceStatistics() = AttendanceStatistics(attendanceRepository, prisonApiService)
}
