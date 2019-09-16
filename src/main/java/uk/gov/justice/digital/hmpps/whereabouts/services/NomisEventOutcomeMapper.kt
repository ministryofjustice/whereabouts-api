package uk.gov.justice.digital.hmpps.whereabouts.services

import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.*

class NomisEventOutcomeMapper {
    companion object {
        private val eventOutcomes = mapOf(
                AcceptableAbsence to EventOutcome("ACCAB"),
                NotRequired to EventOutcome("NREQ"),
                ApprovedCourse to EventOutcome("ACCAB"),
                SessionCancelled to EventOutcome("CANC"),
                RestInCell to EventOutcome("REST"),
                Sick to EventOutcome("REST"),
                RestDay to EventOutcome("REST"),
                Refused to EventOutcome("UNACAB"),
                UnacceptableAbsence to EventOutcome("UNACAB")
        )
    }

    fun getEventOutcome(reason: AbsentReason?, attended: Boolean, paid: Boolean, comment: String?): EventOutcome? {
        val isAttendedWithAbsentReason = attended && reason != null
        val isAbsentWithoutReason = !attended && reason == null

        requireTrue(isAttendedWithAbsentReason) { "An absent reason was supplied for a valid attendance" }
        requireTrue(isAbsentWithoutReason) { "An absent reason was not supplied" }

        if (attended && paid)
            return EventOutcome("ATT", "STANDARD", comment)

        val isNotValidPaidReason = paid && !AbsentReason.getPaidReasons().contains(reason)
        val isNotValidUnPaidReason = !paid && !AbsentReason.getUnpaidReasons().contains(reason)

        requireTrue(isNotValidPaidReason) { String.format("%s is not a valid paid reason", reason) }
        requireTrue(isNotValidUnPaidReason) { String.format("%s is not a valid unpaid reason", reason) }

        val outcome = eventOutcomes[reason]

        return if (comment != null) outcome?.copy(outcomeComment = comment) else outcome
    }

    private fun requireTrue (value: Boolean, lazyMessage: () -> Any) {
        require(!value, lazyMessage)
    }
}
