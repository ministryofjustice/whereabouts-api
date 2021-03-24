CREATE TABLE VIDEO_LINK_BOOKING_EVENT
(
    event_id                  SERIAL        NOT NULL  PRIMARY KEY,
    timestamp                 TIMESTAMP     NOT NULL,
    event_type                CHAR(6)       NOT NULL, -- 'CREATE', 'UPDATE' or 'DELETE'
    user_id                   VARCHAR(126),
    video_link_booking_id     BIGINT,
    agency_id                 VARCHAR(4),
    offender_booking_id       BIGINT,
    court                     TEXT,
    made_by_the_court         BOOLEAN,
    comment                   TEXT,
    main_nomis_appointment_id BIGINT,
    main_location_id          BIGINT,
    main_start_time           TIMESTAMP,
    main_end_time             TIMESTAMP,
    pre_nomis_appointment_id  BIGINT,
    pre_location_id           BIGINT,
    pre_start_time            TIMESTAMP,
    pre_end_time              TIMESTAMP,
    post_nomis_appointment_id BIGINT,
    post_location_id          BIGINT,
    post_start_time           TIMESTAMP,
    post_end_time             TIMESTAMP
);

COMMENT ON TABLE VIDEO_LINK_BOOKING_EVENT IS 'All data for a Video Link Booking create, update or delete event';
CREATE INDEX ON VIDEO_LINK_BOOKING_EVENT(TIMESTAMP)
