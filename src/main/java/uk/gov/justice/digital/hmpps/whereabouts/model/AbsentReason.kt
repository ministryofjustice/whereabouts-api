package uk.gov.justice.digital.hmpps.whereabouts.model

enum class AbsentReason(val label: String) {
  ApprovedCourse("Approved course"),
  AcceptableAbsence("Acceptable absence"),
  SessionCancelled("Session cancelled"),
  RestInCellOrSick("Rest in cell or sick"),
  RestDay("Rest day"),
  UnacceptableAbsence("Unacceptable absence"),
  NotRequired("Not required to attend"),
  Refused("Refused to attend"),
  RefusedIncentiveLevelWarning("Refused to attend");

  val labelWithWarning: String
    get() = if (iepTriggers.contains(this)) "$label - incentive level warning" else label

  val labelWithShortWarning: String
    get() = if (iepTriggers.contains(this)) "$label with warning" else label

  companion object {
    val paidReasons = setOf(ApprovedCourse, AcceptableAbsence, NotRequired)

    val unpaidReasons = setOf(
      Refused,
      RefusedIncentiveLevelWarning,
      RestDay,
      RestInCellOrSick,
      SessionCancelled,
      UnacceptableAbsence,
    )

    val iepTriggers = setOf(RefusedIncentiveLevelWarning, UnacceptableAbsence)
    val absentSubReasonTriggers = listOf(AcceptableAbsence, Refused, RefusedIncentiveLevelWarning, SessionCancelled, UnacceptableAbsence)
  }
}

enum class AbsentSubReason(val label: String) {
  Activities("Activities and education"),
  Behaviour("Behaviour"),
  Courses("Courses, programmes and interventions"),
  ExternalMoves("External moves"),
  Healthcare("Healthcare"),
  Operational("Operational"),
  OverAllocated("Over allocated or schedule clash"),
  Visits("Visits"),
  NotListed("Not listed");

  companion object {
    // Behaviour is not a paid reason
    val paidSubReasons = listOf(Activities, Courses, ExternalMoves, Healthcare, Operational, OverAllocated, Visits, NotListed)
    val unpaidSubReasons = listOf(Activities, Behaviour, Courses, ExternalMoves, Healthcare, Operational, OverAllocated, Visits, NotListed)
  }
}
