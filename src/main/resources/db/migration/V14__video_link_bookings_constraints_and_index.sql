ALTER TABLE video_link_booking ADD UNIQUE (pre_appointment);
ALTER TABLE video_link_booking ADD UNIQUE (main_appointment);
ALTER TABLE video_link_booking ADD UNIQUE (post_appointment);

ALTER TABLE video_link_appointment ADD UNIQUE (appointment_id);

CREATE INDEX ON video_link_appointment(booking_id);
