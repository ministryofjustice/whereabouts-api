ALTER TABLE ENABLED_COURT
    ADD COLUMN EMAIL VARCHAR(250);
UPDATE ENABLED_COURT
SET email =
        CASE
            WHEN id = 'AMERCC' THEN 'test@test.gov.uk'
            END
