package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.AcceptableAbsence
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.ApprovedCourse
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.NotRequired
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Refused
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.RefusedIncentiveLevelWarning
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.RestDay
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.RestInCellOrSick
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.SessionCancelled
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.UnacceptableAbsence
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.UnacceptableAbsenceIncentiveLevelWarning
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import java.time.LocalDate

@Suppress("unused")
data class PaidReasons(
  val approvedCourse: Int?,
  val notRequired: Int?,
  val acceptableAbsence: Int?,
) {
  val acceptableAbsenceDescription = AcceptableAbsence.labelWithAddedWarning
  val approvedCourseDescription = ApprovedCourse.labelWithAddedWarning
  val notRequiredDescription = NotRequired.labelWithAddedWarning
}

@Suppress("unused")
data class UnpaidReasons(
  val restDay: Int?,
  val restInCellOrSick: Int?,
  val refused: Int?,
  val refusedIncentiveLevelWarning: Int?,
  val sessionCancelled: Int?,
  val unacceptableAbsence: Int?,
  val unacceptableAbsenceIncentiveLevelWarning: Int?,
) {
  val refusedDescription = Refused.labelWithAddedWarning
  val refusedIncentiveLevelWarningDescription = RefusedIncentiveLevelWarning.labelWithAddedWarning
  val sessionCancelledDescription = SessionCancelled.labelWithAddedWarning
  val unacceptableAbsenceDescription = UnacceptableAbsence.labelWithAddedWarning
  val unacceptableAbsenceIncentiveLevelWarningDescription = UnacceptableAbsenceIncentiveLevelWarning.labelWithAddedWarning
  val restDayDescription = RestDay.labelWithAddedWarning
  val restInCellOrSickDescription = RestInCellOrSick.labelWithAddedWarning
}

data class Stats(
  val scheduleActivities: Int,
  val notRecorded: Int,
  val paidReasons: PaidReasons?,
  val unpaidReasons: UnpaidReasons?,
  val suspended: Int,
  val attended: Int,
)

@Service
class AttendanceStatistics(
  private val attendanceRepository: AttendanceRepository,
  private val prisonApiService: PrisonApiService,
) {
  fun getStats(prisonId: String, period: TimePeriod?, from: LocalDate, to: LocalDate): Stats {
    val periods = period?.let { setOf(it) } ?: setOf(TimePeriod.PM, TimePeriod.AM)
    val attendances = when (periods.size) {
      1 -> attendanceRepository.findByPrisonIdAndPeriodAndEventDateBetween(prisonId, period, from, to)
      else -> attendanceRepository.findByPrisonIdAndEventDateBetweenAndPeriodIn(prisonId, from, to, periods)
    }

    val attendancesBookingIdsCount = attendances.groupingBy { it.bookingId }.eachCount()
    val counts = prisonApiService.getScheduleActivityCounts(prisonId, from, to, periods, attendancesBookingIdsCount)

    val (paid, unpaid) = attendances.partition { AbsentReason.paidReasons.contains(it.absentReason) }
    val paidCounts = paid.groupBy { it.absentReason }.mapValues { it.value.count() }
    val unpaidCounts = unpaid.groupBy { it.absentReason }.mapValues { it.value.count() }

    return Stats(
      scheduleActivities = counts.total,
      notRecorded = counts.notRecorded,
      paidReasons = PaidReasons(
        acceptableAbsence = paidCounts[AcceptableAbsence],
        approvedCourse = paidCounts[ApprovedCourse],
        notRequired = paidCounts[NotRequired],
      ),
      unpaidReasons = UnpaidReasons(
        refused = unpaidCounts[Refused],
        refusedIncentiveLevelWarning = unpaidCounts[RefusedIncentiveLevelWarning],
        sessionCancelled = unpaidCounts[SessionCancelled],
        unacceptableAbsence = unpaidCounts[UnacceptableAbsence],
        unacceptableAbsenceIncentiveLevelWarning = unpaidCounts[UnacceptableAbsenceIncentiveLevelWarning],
        restDay = unpaidCounts[RestDay],
        restInCellOrSick = unpaidCounts[RestInCellOrSick],
      ),
      suspended = counts.suspended,
      attended = attendances.count { it.attended },
    )
  }
}

data class PrisonerActivitiesCount(val total: Int, val suspended: Int, val notRecorded: Int)
