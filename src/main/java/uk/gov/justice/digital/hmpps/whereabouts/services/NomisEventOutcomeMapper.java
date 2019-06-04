package uk.gov.justice.digital.hmpps.whereabouts.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;

import java.util.HashMap;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class EventOutcome {
    private final String eventOutcome;
    private final String performance;
}

class NomisEventOutcomeMapper {
    private static final HashMap<AbsentReason, EventOutcome> eventOutcomes = new HashMap<>();

    NomisEventOutcomeMapper() {
        eventOutcomes.put(AbsentReason.AcceptableAbsence, new EventOutcome("ACCAB", null));
        eventOutcomes.put(AbsentReason.NotRequired, new EventOutcome("NREQ", null));

        eventOutcomes.put(AbsentReason.SessionCancelled, new EventOutcome("CANC", null));
        eventOutcomes.put(AbsentReason.RestInCell, new EventOutcome("REST", null));

        eventOutcomes.put(AbsentReason.Sick, new EventOutcome("REST", null));
        eventOutcomes.put(AbsentReason.RestDay, new EventOutcome("REST", null));

        eventOutcomes.put(AbsentReason.Refused, new EventOutcome("UNACAB", null));
        eventOutcomes.put(AbsentReason.UnacceptableAbsence, new EventOutcome("UNACAB", null));

    }

    EventOutcome getEventOutcome(final AbsentReason reason, final boolean attended, final boolean paid) {

        if (attended && reason != null) {
            throw new IllegalArgumentException("An absent reason was supplied for a valid attendance");
        }

        if (!attended && reason == null) {
            throw new IllegalArgumentException("An absent reason was not supplied");
        }

        if (attended && paid)
            return new EventOutcome("ATT", "STANDARD");

        if (paid && !AbsentReason.getPaidReasons().contains(reason)) {
            throw new IllegalArgumentException(String.format("%s is not a valid paid reason", reason));
        }

        if (!paid && !AbsentReason.getUnpaidReasons().contains(reason)) {
            throw new IllegalArgumentException(String.format("%s is not a valid unpaid reason", reason));
        }

        return eventOutcomes.get(reason);

    }
}
