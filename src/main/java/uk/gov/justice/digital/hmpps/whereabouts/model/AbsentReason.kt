package uk.gov.justice.digital.hmpps.whereabouts.model

enum class AbsentReason(val label: String, val eventOutcome: String) {
  ApprovedCourse("Approved course", "ACCAB"),
  AcceptableAbsence("Acceptable absence", "ACCAB"),
  SessionCancelled("Session cancelled", "CANC"),
  RestInCellOrSick("Rest in cell or sick", "REST"),
  RestDay("Rest day", "REST"),
  UnacceptableAbsence("Unacceptable absence", "UNACAB"),
  UnacceptableAbsenceIncentiveLevelWarning("Unacceptable absence", "UNACAB"),
  NotRequired("Not required to attend", "NREQ"),
  Refused("Refused to attend", "UNACAB"),
  RefusedIncentiveLevelWarning("Refused to attend", "UNACAB");

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
      UnacceptableAbsenceIncentiveLevelWarning,
    )

    val iepTriggers = setOf(RefusedIncentiveLevelWarning, UnacceptableAbsenceIncentiveLevelWarning)
    val absentSubReasonTriggers = listOf(
      AcceptableAbsence,
      Refused,
      RefusedIncentiveLevelWarning,
      SessionCancelled,
      UnacceptableAbsence,
      UnacceptableAbsenceIncentiveLevelWarning,
    )
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
