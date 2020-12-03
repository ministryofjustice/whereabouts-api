UPDATE video_link_appointment set hearing_type = 'MAIN' where hearing_type = '0';
UPDATE video_link_appointment set hearing_type = 'PRE'  where hearing_type = '1';
UPDATE video_link_appointment set hearing_type = 'POST' where hearing_type = '2';
