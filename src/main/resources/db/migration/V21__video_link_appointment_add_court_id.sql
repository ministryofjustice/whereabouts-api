alter table video_link_appointment
    add column court_id varchar(20);

alter table public.video_link_appointment
    alter column court drop not null;