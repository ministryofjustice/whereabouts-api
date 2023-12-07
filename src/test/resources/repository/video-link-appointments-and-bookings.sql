INSERT INTO video_link_booking (id, offender_booking_id, court_name, court_id, made_by_the_court, created_by_username,
                                prison_id, comment, court_hearing_type)
VALUES (1, 111111, 'Coventry', 'CVNTCC', true, 'USER1', 'HEI', 'not visible', NULL),
       (2, 111111, 'Birmingham', 'BMCC', true, 'USER1', 'BMI', 'visible', NULL),
       (3, 222222, 'Birmingham', 'BMCC', true, 'USER1', 'BMI', 'not visible', NULL);

INSERT INTO video_link_appointment (id, appointment_id, hearing_type, video_link_booking_id, location_id,
                                    start_date_time, end_date_time)
VALUES (1, 1, 'MAIN', 1, 1292, {ts '2023-01-01 01:00:00'}, {ts '2023-01-01 02:00:00'}),
       (2, 2, 'MAIN', 2, 1293, {ts '2023-01-02 01:00:00'}, {ts '2023-01-02 02:00:00'}),
       (3, 3, 'MAIN', 3, 1293, {ts '2023-01-03 01:00:00'}, {ts '2023-01-03 02:00:00'}),
       (4, 4, 'POST', 3, 1293, {ts '2023-01-03 03:00:00'}, {ts '2023-01-03 04:00:00'});
