package uk.gov.justice.digital.hmpps.whereabouts.services;

import lombok.Data;
import net.logstash.logback.encoder.org.apache.commons.lang.NullArgumentException;
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason;

import java.util.HashMap;
import java.util.Map;

class NomisEventOutcomeMap {
    private static final Map<Long, NomisCodes> codeMap = new HashMap();

    NomisEventOutcomeMap() {
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

    private void addPaidAttendanceMapping(String eventOutcome, String performance) {
        codeMap.put(getKey(null, true, true), new NomisCodes(eventOutcome, performance));
    }

    private void addUnPaidAbsenceMapping(AbsentReason reason, String eventOutCome) {
        codeMap.put(getKey(reason, false, false), new NomisCodes(eventOutCome, null));
    }

    private void addPaidAbsenceMapping(AbsentReason reason, String eventOutCome) {
        codeMap.put(getKey(reason, false, true), new NomisCodes(eventOutCome, null));
    }


    private long getKey(AbsentReason reason, Boolean attended, Boolean paid) {
        return reason != null ?
                reason.hashCode() + attended.hashCode() + paid.hashCode() :
                "ATT_PAID_STANDARD".hashCode() + attended.hashCode() + paid.hashCode();
    }

    public NomisCodes getEventOutCome(AbsentReason reason, Boolean attended, Boolean paid) {
        final var mapping = codeMap.get(getKey(reason, attended, paid));
        if (mapping == null)
            throw new NullArgumentException(String.format("No mapping for (%s) attended = %s paid = %s",
                    reason, attended ? "Y" : "N", paid ? "Y" : "N"));

        return mapping;
    }

    @Data
    public class NomisCodes {
        private final String outcome;
        private final String performance;
    }
}
