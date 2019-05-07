DROP TABLE IF EXISTS ABSENT_REASONS;

CREATE TABLE ABSENT_REASONS
(
    ID                              SERIAL PRIMARY KEY,
    REASON                          VARCHAR(30) NOT NULL,
    PNOMIS_CODE                     VARCHAR(10) NOT NULL,
    PAID_REASON                     BOOLEAN DEFAULT false
);

COMMENT ON TABLE ABSENT_REASONS IS 'Reason for non attendance';
