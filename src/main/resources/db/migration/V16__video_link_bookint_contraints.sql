ALTER TABLE video_link_booking
    DROP CONSTRAINT fk_video_link_booking_main;

ALTER TABLE video_link_booking
    DROP CONSTRAINT fk_video_link_booking_post;

ALTER TABLE video_link_booking
    ADD CONSTRAINT fk_video_link_booking_main foreign key (main_appointment) references video_link_appointment (id);

ALTER TABLE video_link_booking
    ADD CONSTRAINT fk_video_link_booking_post foreign key (post_appointment) references video_link_appointment (id);

