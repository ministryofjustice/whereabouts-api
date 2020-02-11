package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate

data class PaidReasons(val attended: Int? = 0, val acceptableAbsence: Int? = 0, val approvedCourse: Int? = 0, val notRequired: Int? = 0)
data class UnpaidReasons(val refused: Int? = 0, val refusedIncentiveLevelWarning: Int?, val sessionCancelled: Int? = 0, val unacceptableAbsence: Int? = 0, val restInCell: Int? = 0, val restInCellOrSick: Int? = 0)
data class Stats(val scheduleActivities: Int? = 0, val notRecorded: Int? = 0, val paidReasons: PaidReasons?, val unpaidReasons: UnpaidReasons?)

@Service
open class AttendanceStatistics(private val attendanceRepository: AttendanceRepository, private val elite2ApiService: Elite2ApiService) {
  fun getStats(prisonId: String, period: TimePeriod?, from: LocalDate, to: LocalDate): Stats {

    val periods = period?.let { setOf(it) } ?: setOf(TimePeriod.PM, TimePeriod.AM)
    val offendersScheduledForActivity = getScheduleActivityForPeriods(prisonId, from, to, periods)

    // This creates a Grouping that looks like {1=2, 2=2}
    // Where the key is the booking id and the value is the
    // number of times that booking id appears
    val scheduledBookingIdsCount = offendersScheduledForActivity.groupingBy { it }.eachCount()

    val attendances = when (periods.size) {
      in 2..3 -> attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodIn(prisonId, from, to, periods)
      else -> attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(prisonId, period, from, to)
    }

    val attendancesBookingIdsCount = attendances.groupingBy { it.bookingId }.eachCount()

    // Iterate over the scheduled booking ids grouping
    // and check whether the number of times that booking
    // id appears in the schedules matches the number of times
    // it appears in the attendances. If not, the difference
    // is added
    var notRecordedCount = 0
    for (key in scheduledBookingIdsCount.keys) {
      notRecordedCount += if (!attendancesBookingIdsCount.containsKey(key)) scheduledBookingIdsCount.getValue(key) else scheduledBookingIdsCount.getValue(key) - attendancesBookingIdsCount.getValue(key)
    }

    return Stats(
        scheduleActivities = offendersScheduledForActivity.count(),
        notRecorded = notRecordedCount,
        paidReasons = PaidReasons(
            attended = attendances.count { it.attended },
            acceptableAbsence = attendances.count { it.absentReason == AbsentReason.AcceptableAbsence },
            approvedCourse = attendances.count { it.absentReason == AbsentReason.ApprovedCourse },
            notRequired = attendances.count { it.absentReason == AbsentReason.NotRequired }
        ),
        unpaidReasons = UnpaidReasons(
            refused = attendances.count { it.absentReason == AbsentReason.Refused },
            refusedIncentiveLevelWarning = attendances.count { it.absentReason == AbsentReason.RefusedIncentiveLevelWarning },
            sessionCancelled = attendances.count { it.absentReason == AbsentReason.SessionCancelled },
            unacceptableAbsence = attendances.count { it.absentReason == AbsentReason.UnacceptableAbsence },
            restInCellOrSick = attendances.count { it.absentReason == AbsentReason.RestInCellOrSick }
        )
    )
  }

  private fun getScheduleActivityForPeriods(prisonId: String, from: LocalDate, to: LocalDate, periods: Set<TimePeriod>): List<Long> =
      periods.map { elite2ApiService.getBookingIdsForScheduleActivitiesByDateRange(prisonId, it, from, to) }.flatten()
}
