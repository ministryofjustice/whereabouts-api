ALTER TABLE VIDEO_LINK_APPOINTMENT ADD location_id BIGINT;
ALTER TABLE VIDEO_LINK_APPOINTMENT ADD start_date_time TIMESTAMP,
ALTER TABLE VIDEO_LINK_APPOINTMENT ADD end_date_time TIMESTAMP;
CREATE INDEX LOCATION_ID_IDX ON VIDEO_LINK_APPOINTMENT (location_id);
CREATE INDEX START_DATE_TIME_IDX ON VIDEO_LINK_APPOINTMENT (start_date_time);
CREATE INDEX END_DATE_TIME_IDX ON VIDEO_LINK_APPOINTMENT (end_date_time);