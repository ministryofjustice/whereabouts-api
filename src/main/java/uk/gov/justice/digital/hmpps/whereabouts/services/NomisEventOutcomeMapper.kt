package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.AcceptableAbsence
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.ApprovedCourse
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Companion.paidReasons
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Companion.unpaidReasons
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.NotRequired
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Refused
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.RefusedIncentiveLevelWarning
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.RestDay
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.RestInCellOrSick
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.SessionCancelled
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.UnacceptableAbsence
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason

@Service
class NomisEventOutcomeMapper {
  companion object {
    private val eventOutcomes = mapOf(
      AcceptableAbsence to EventOutcome("ACCAB"),
      NotRequired to EventOutcome("NREQ"),
      ApprovedCourse to EventOutcome("ACCAB"),
      SessionCancelled to EventOutcome("CANC"),
      RestInCellOrSick to EventOutcome("REST"),
      RestDay to EventOutcome("REST"),
      Refused to EventOutcome("UNACAB"),
      UnacceptableAbsence to EventOutcome("UNACAB"),
      RefusedIncentiveLevelWarning to EventOutcome("UNACAB")
    )
  }

  fun getEventOutcome(reason: AbsentReason?, subReason: AbsentSubReason?, attended: Boolean, paid: Boolean, comment: String?): EventOutcome? {
    requireTrue(attended && reason != null) { "An absent reason was supplied for a valid attendance" }
    requireTrue(!attended && reason == null) { "An absent reason was not supplied" }

    if (attended && paid) return EventOutcome("ATT", "STANDARD", comment)

    requireTrue(paid && !paidReasons.contains(reason)) { "$reason is not a valid paid reason" }
    requireTrue(!paid && !unpaidReasons.contains(reason)) { "$reason is not a valid unpaid reason" }

    // TODO: once DPS front end developed add in mandatory sub reason based on AbsentReason.absentSubReasonTriggers
    subReason?.run {
      requireTrue(paid && subReason == AbsentSubReason.Behaviour) { "$subReason is not a valid paid sub reason" }
    }

    val outcome = eventOutcomes[reason]

    return comment?.let {
      // comment is only 240 chars in nomis, so need to truncate if we've prefixed the sub reason
      val outcomeComment = subReason?.let { "${subReason.label}. $comment" }?.take(240) ?: comment
      outcome?.copy(outcomeComment = outcomeComment)
    } ?: outcome
  }

  private fun requireTrue(value: Boolean, lazyMessage: () -> Any) {
    require(!value, lazyMessage)
  }
}
