package uk.gov.justice.digital.hmpps.whereabouts.model;

import java.util.Set;

public enum AbsentReason {
    AcceptableAbsence,
    SessionCancelled,
    RestInCell,
    RestDay,
    UnacceptableAbsence,
    NotRequired,
    Refused,
    Sick;

    public static Set<AbsentReason> getPaidReasons() {
        return Set.of(
                AbsentReason.AcceptableAbsence,
                AbsentReason.NotRequired
        );
    }

    public static Set<AbsentReason> getUnpaidReasons() {
        return Set.of(
                AbsentReason.SessionCancelled,
                AbsentReason.RestInCell,
                AbsentReason.RestDay,
                AbsentReason.UnacceptableAbsence,
                AbsentReason.Refused,
                AbsentReason.Sick
        );
    }

    public static Set<AbsentReason> getIepTriggers() {
        return Set.of(AbsentReason.Refused, AbsentReason.UnacceptableAbsence);
    }
}
