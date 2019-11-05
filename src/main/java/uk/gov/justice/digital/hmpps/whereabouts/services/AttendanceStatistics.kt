package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate

data class PaidReasons(val attended: Int? = 0, val acceptableAbsence: Int? = 0, val approvedCourse: Int? = 0, val notRequired: Int? = 0)
data class UnpaidReasons(val refused: Int? = 0, val restDay: Int? = 0, val sessionCancelled: Int? = 0, val sick: Int? = 0, val unacceptableAbsence: Int? = 0, val restInCell: Int? = 0)
data class Stats(val scheduleActivities: Int? = 0, val notRecorded: Int? = 0, val paidReasons: PaidReasons?, val unpaidReasons: UnpaidReasons?)

@Service
open class AttendanceStatistics(private val attendanceRepository: AttendanceRepository, private val elite2ApiService: Elite2ApiService) {
  fun getStats(prisonId: String, period: TimePeriod?, from: LocalDate, to: LocalDate): Stats {

    val periods = period?.let { setOf(it) } ?: setOf(TimePeriod.PM, TimePeriod.AM)
    val offendersScheduledForActivity = getScheduleActivityForPeriods(prisonId, from, to, periods)

    val attendances = when (periods.size) {
      in 2..3 -> attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodIn(prisonId, from, to, periods)
      else -> attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(prisonId, period, from, to)
    }

    val attendanceBookingIds = attendances.map { it.bookingId }

    return Stats(
        scheduleActivities = offendersScheduledForActivity.count(),
        notRecorded = offendersScheduledForActivity.count { !attendanceBookingIds.contains(it) },
        paidReasons = PaidReasons(
            attended = attendances.count { it.attended },
            acceptableAbsence = attendances.count { it.absentReason == AbsentReason.AcceptableAbsence },
            approvedCourse = attendances.count { it.absentReason == AbsentReason.ApprovedCourse },
            notRequired = attendances.count { it.absentReason == AbsentReason.NotRequired }
        ),
        unpaidReasons = UnpaidReasons(
            refused = attendances.count { it.absentReason == AbsentReason.Refused },
            restDay = attendances.count { it.absentReason == AbsentReason.RestDay },
            sessionCancelled = attendances.count { it.absentReason == AbsentReason.SessionCancelled },
            sick = attendances.count { it.absentReason == AbsentReason.Sick },
            unacceptableAbsence = attendances.count { it.absentReason == AbsentReason.UnacceptableAbsence },
            restInCell = attendances.count { it.absentReason == AbsentReason.RestInCell }
        )
    )
  }

  private fun getScheduleActivityForPeriods(prisonId: String, from: LocalDate, to: LocalDate, periods: Set<TimePeriod>): List<Long> =
      periods.map { elite2ApiService.getBookingIdsForScheduleActivitiesByDateRange(prisonId, it, from, to) }.flatten()
}
