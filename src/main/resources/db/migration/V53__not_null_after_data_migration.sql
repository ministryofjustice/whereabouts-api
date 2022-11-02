alter table video_link_booking alter column prison_id set not null;
alter table video_link_appointment alter column location_id set not null;
alter table video_link_appointment alter column start_date_time set not null;
alter table video_link_appointment alter column end_date_time set not null;