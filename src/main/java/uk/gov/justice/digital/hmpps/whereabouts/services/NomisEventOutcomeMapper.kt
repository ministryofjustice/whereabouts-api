package uk.gov.justice.digital.hmpps.whereabouts.services

import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import java.util.HashMap

internal class NomisEventOutcomeMapper {
    init {
        //paid absences
        eventOutcomes[AbsentReason.AcceptableAbsence] = EventOutcome("ACCAB")
        eventOutcomes[AbsentReason.NotRequired] = EventOutcome("NREQ")
        eventOutcomes[AbsentReason.ApprovedCourse] = EventOutcome("ACCAB")

        // unpaid absences
        eventOutcomes[AbsentReason.SessionCancelled] = EventOutcome("CANC")
        eventOutcomes[AbsentReason.RestInCell] = EventOutcome("REST")
        eventOutcomes[AbsentReason.Sick] = EventOutcome("REST")
        eventOutcomes[AbsentReason.RestDay] = EventOutcome("REST")

        //unpaid absences that trigger automatic iep warnings
        eventOutcomes[AbsentReason.Refused] = EventOutcome("UNACAB")
        eventOutcomes[AbsentReason.UnacceptableAbsence] = EventOutcome("UNACAB")
    }

    fun getEventOutcome(reason: AbsentReason?, attended: Boolean, paid: Boolean, comment: String?): EventOutcome? {
        val isAttendedWithAbsentReason = attended && reason != null
        val isAbsentWithoutReason = !attended && reason == null

        throwIfTrue(isAttendedWithAbsentReason) { "An absent reason was supplied for a valid attendance" }
        throwIfTrue(isAbsentWithoutReason) { "An absent reason was not supplied" }

        if (attended && paid)
            return EventOutcome("ATT", "STANDARD", comment)

        val isNotValidPaidReason = paid && !AbsentReason.getPaidReasons().contains(reason)
        val isNotValidUnPaidReason = !paid && !AbsentReason.getUnpaidReasons().contains(reason)

        throwIfTrue(isNotValidPaidReason) { String.format("%s is not a valid paid reason", reason) }
        throwIfTrue(isNotValidUnPaidReason) { String.format("%s is not a valid unpaid reason", reason) }

        val outcome = eventOutcomes[reason]

        return if (comment != null) outcome?.copy(outcomeComment = comment) else outcome
    }

    companion object {
        private val eventOutcomes = HashMap<AbsentReason, EventOutcome>()
    }

    private fun throwIfTrue (value: Boolean, lazyMessage: () -> Any) {
        check(!value, lazyMessage)
    }
}
