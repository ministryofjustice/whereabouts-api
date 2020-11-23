CREATE TABLE VIDEO_LINK_BOOKING
(
    ID                 SERIAL      PRIMARY KEY,
    PRE_APPOINTMENT    BIGINT,
    MAIN_APPOINTMENT   BIGINT      NOT NULL,
    POST_APPOINTMENT   BIGINT,
    CONSTRAINT fk_video_link_booking_pre  foreign key(pre_appointment) references video_link_appointment(id),
    CONSTRAINT fk_video_link_booking_main foreign key(pre_appointment) references video_link_appointment(id),
    CONSTRAINT fk_video_link_booking_post foreign key(pre_appointment) references video_link_appointment(id)
);

COMMENT ON TABLE VIDEO_LINK_BOOKING IS 'A Video Link Booking consisting of a main appointment and optional pre and post appointments';
