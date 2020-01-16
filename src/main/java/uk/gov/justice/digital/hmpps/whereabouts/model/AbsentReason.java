package uk.gov.justice.digital.hmpps.whereabouts.model;

import java.util.Set;

public enum AbsentReason {
    ApprovedCourse,
    AcceptableAbsence,
    SessionCancelled,
    RestInCellOrSick,
    RestDay,
    UnacceptableAbsence,
    NotRequired,
    Refused,
    RefusedWithIncentiveLevelWarning;

    public static Set<AbsentReason> getPaidReasons() {
        return Set.of(
                ApprovedCourse,
                AcceptableAbsence,
                NotRequired
        );
    }

    public static Set<AbsentReason> getUnpaidReasons() {
        return Set.of(
                SessionCancelled,
                RestInCellOrSick,
                RestDay,
                UnacceptableAbsence,
                Refused,
                RefusedWithIncentiveLevelWarning
        );
    }

    public static Set<AbsentReason> getIepTriggers() {
        return Set.of(RefusedWithIncentiveLevelWarning, UnacceptableAbsence);
    }
}
