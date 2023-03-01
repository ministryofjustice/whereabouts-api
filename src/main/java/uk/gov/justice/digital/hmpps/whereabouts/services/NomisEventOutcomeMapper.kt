package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Companion.paidReasons
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Companion.unpaidReasons
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason

@Service
class NomisEventOutcomeMapper {
  fun getEventOutcome(reason: AbsentReason?, subReason: AbsentSubReason?, attended: Boolean, paid: Boolean, comment: String?): EventOutcome? {
    requireTrue(attended && reason != null) { "An absent reason was supplied for a valid attendance" }
    requireTrue(!attended && reason == null) { "An absent reason was not supplied" }

    if (attended && paid) return EventOutcome(eventOutcome = "ATT", performance = "STANDARD", outcomeComment = comment)

    requireTrue(paid && !paidReasons.contains(reason)) { "$reason is not a valid paid reason" }
    requireTrue(!paid && !unpaidReasons.contains(reason)) { "$reason is not a valid unpaid reason" }

    // TODO: once DPS front end developed add in mandatory sub reason based on AbsentReason.absentSubReasonTriggers
    subReason?.run {
      requireTrue(paid && subReason == AbsentSubReason.Behaviour) { "$subReason is not a valid paid sub reason" }
    }

    return EventOutcome(
      eventOutcome = reason!!.eventOutcome,
      outcomeComment = comment?.let {
        // comment is only 240 chars in nomis, so need to truncate if we've prefixed the sub reason
        subReason?.let { "${subReason.label}. $comment" }?.take(240) ?: comment
      },
    )
  }

  private fun requireTrue(value: Boolean, lazyMessage: () -> Any) {
    require(!value, lazyMessage)
  }
}
