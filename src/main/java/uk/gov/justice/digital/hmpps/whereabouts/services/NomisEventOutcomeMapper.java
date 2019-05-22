package uk.gov.justice.digital.hmpps.whereabouts.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.logstash.logback.encoder.org.apache.commons.lang.NullArgumentException;
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
    private static final HashMap<Long, EventOutcome> codeMap = new HashMap<>();

    NomisEventOutcomeMapper() {
        addPaidAttendanceMapping("ATT", "STANDARD");

        addPaidAbsenceMapping(AbsentReason.AcceptableAbsence,"ACCAB");
        addPaidAbsenceMapping(AbsentReason.NotRequired,"NREQ");

        addUnPaidAbsenceMapping(AbsentReason.SessionCancelled, "CANC");
        addUnPaidAbsenceMapping(AbsentReason.RestInCell,  "REST");
        addUnPaidAbsenceMapping(AbsentReason.Sick, "REST");
        addUnPaidAbsenceMapping(AbsentReason.RestDay, "REST");

        addUnPaidAbsenceMapping(AbsentReason.Refused,  "UNACAB");
        addUnPaidAbsenceMapping(AbsentReason.UnacceptableAbsence,  "UNACAB");
    }

    private void addPaidAttendanceMapping(final String eventOutcome, final String performance) {
        codeMap.put(getKey(null, true, true), new EventOutcome(eventOutcome, performance));
    }

    private void addUnPaidAbsenceMapping(final AbsentReason reason, final String eventOutCome) {
        codeMap.put(getKey(reason, false, false), new EventOutcome(eventOutCome, null));
    }

    private void addPaidAbsenceMapping(final AbsentReason reason, final String eventOutCome) {
        codeMap.put(getKey(reason, false, true), new EventOutcome(eventOutCome, null));
    }


    private long getKey(final AbsentReason reason, final Boolean attended, final Boolean paid) {
        return reason != null ?
                reason.hashCode() + attended.hashCode() + paid.hashCode() :
                "ATT_PAID_STANDARD".hashCode() + attended.hashCode() + paid.hashCode();
    }

    public EventOutcome getEventOutcome(final AbsentReason reason, final boolean attended, final boolean paid) {
        final var mapping = codeMap.get(getKey(reason, attended, paid));
        if (mapping == null)
            throw new NullArgumentException(String.format("No mapping for (%s) attended = %s paid = %s",
                    reason, attended ? "Y" : "N", paid ? "Y" : "N"));

        return mapping;
    }
}
