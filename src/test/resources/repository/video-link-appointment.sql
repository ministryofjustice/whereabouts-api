TRUNCATE TABLE video_link_appointment RESTART IDENTITY;
INSERT INTO video_link_appointment (id, appointment_id, hearing_type, video_link_booking_id, location_id, start_date_time, end_date_time) VALUES
(1, 00000001, 'MAIN', 1, 1292, to_timestamp('2023-01-01 01:00:00', 'YYYY-MM-DD HH:MI:SS'), to_timestamp('2023-01-01 02:00:00', 'YYYY-MM-DD HH:MI:SS')),
(2, 00000002, 'MAIN', 2, 1293, to_timestamp('2023-01-01 01:00:00', 'YYYY-MM-DD HH:MI:SS'), to_timestamp('2023-01-01 02:00:00', 'YYYY-MM-DD HH:MI:SS'));