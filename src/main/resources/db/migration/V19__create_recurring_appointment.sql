DROP TABLE IF EXISTS RECURRING_APPOINTMENT;

CREATE TABLE RECURRING_APPOINTMENT
(
    ID                            SERIAL PRIMARY KEY,
    MAIN_RECURRING_APPOINTMENT_ID BIGINT REFERENCES MAIN_RECURRING_APPOINTMENT (ID)
);

COMMENT ON TABLE RECURRING_APPOINTMENT IS 'Records appointment created based of the recurring rules';

CREATE INDEX RECURRING_APPOINTMENT_BY_ID_IDX ON RECURRING_APPOINTMENT (ID);
CREATE INDEX RECURRING_APPOINTMENT_BY_BOOKING_ID_IDX ON RECURRING_APPOINTMENT (MAIN_RECURRING_APPOINTMENT_ID);

