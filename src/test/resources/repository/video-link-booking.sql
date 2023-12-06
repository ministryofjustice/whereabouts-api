TRUNCATE TABLE video_link_booking RESTART IDENTITY;
INSERT INTO video_link_booking (id, offender_booking_id, court_name, court_id, made_by_the_court, created_by_username, prison_id, "comment",  court_hearing_type) VALUES
(1, 111111, 'Coventry','CVNTCC', 'true','USER1','HEI','no comments',NULL),
(2, 222222, 'Birmingham','BMCC', true,'USER1','BMI','no comments',NULL);