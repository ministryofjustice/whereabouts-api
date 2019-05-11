package uk.gov.justice.digital.hmpps.whereabouts.model;

public enum AbsentReason {
    AcceptableAbsence {
        @Override
        public String toString() {
            return "Acceptable absence";
        }
    },
    SessionCancelled {
        @Override
        public String toString() {
            return "Session cancelled";
        }
    },
    RestInCell {
        @Override
        public String toString() {
            return "Rest in cell";
        }
    },
    RestDay {
        @Override
        public String toString() {
            return "Rest day";
        }
    },
    UnacceptableAbsence {
        @Override
        public String toString() {
            return "Unacceptable absence";
        }
    },
    NotRequired {
        @Override
        public String toString() {
            return "Not required";
        }
    },
    Refused,
    Sick
}
