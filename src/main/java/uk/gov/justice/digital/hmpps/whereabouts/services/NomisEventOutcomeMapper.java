package uk.gov.justice.digital.hmpps.whereabouts.services;

import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;

import java.util.HashMap;

class NomisEventOutcomeMapper {
    private static final HashMap<AbsentReason, EventOutcome> eventOutcomes = new HashMap<>();

    NomisEventOutcomeMapper() {
        //paid absences
        eventOutcomes.put(AbsentReason.AcceptableAbsence, new EventOutcome("ACCAB", null, null));
        eventOutcomes.put(AbsentReason.NotRequired, new EventOutcome("NREQ", null, null));
        eventOutcomes.put(AbsentReason.ApprovedCourse, new EventOutcome("ACCAB", null, null));

        // unpaid absences
        eventOutcomes.put(AbsentReason.SessionCancelled, new EventOutcome("CANC", null, null));
        eventOutcomes.put(AbsentReason.RestInCell, new EventOutcome("REST", null, null));

        eventOutcomes.put(AbsentReason.Sick, new EventOutcome("REST", null, null));
        eventOutcomes.put(AbsentReason.RestDay, new EventOutcome("REST", null, null));

        //unpaid absences that trigger automatic iep warnings
        eventOutcomes.put(AbsentReason.Refused, new EventOutcome("UNACAB", null, null));
        eventOutcomes.put(AbsentReason.UnacceptableAbsence, new EventOutcome("UNACAB", null, null));

    }

    EventOutcome getEventOutcome(final AbsentReason reason, final boolean attended, final boolean paid, final String comment) {

        if (attended && reason != null) {
            throw new IllegalArgumentException("An absent reason was supplied for a valid attendance");
        }

        if (!attended && reason == null) {
            throw new IllegalArgumentException("An absent reason was not supplied");
        }

        if (attended && paid)
            return new EventOutcome("ATT", "STANDARD", comment);

        if (paid && !AbsentReason.getPaidReasons().contains(reason)) {
            throw new IllegalArgumentException(String.format("%s is not a valid paid reason", reason));
        }

        if (!paid && !AbsentReason.getUnpaidReasons().contains(reason)) {
            throw new IllegalArgumentException(String.format("%s is not a valid unpaid reason", reason));
        }

        var outcome = eventOutcomes.get(reason);

        if (comment != null) {
            outcome.setOutcomeComment(comment);
        }

        return outcome;

    }
}
