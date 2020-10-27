package uk.gov.justice.digital.hmpps.whereabouts.model

enum class AbsentReason {
  ApprovedCourse, AcceptableAbsence, SessionCancelled, RestInCellOrSick, RestDay, UnacceptableAbsence, NotRequired, Refused, RefusedIncentiveLevelWarning;

  companion object {
    val paidReasons: Set<AbsentReason>
      get() = java.util.Set.of(ApprovedCourse, AcceptableAbsence, NotRequired)

    val unpaidReasons: Set<AbsentReason>
      get() = java.util.Set.of(
        SessionCancelled,
        RestInCellOrSick,
        RestDay,
        UnacceptableAbsence,
        Refused,
        RefusedIncentiveLevelWarning
      )

    val iepTriggers: Set<AbsentReason>
      get() = java.util.Set.of(RefusedIncentiveLevelWarning, UnacceptableAbsence)
  }
}
