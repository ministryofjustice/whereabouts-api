create table video_link_appointment_pre_dcs_703 as table video_link_appointment;
create table video_link_booking_pre_dcs_703 as table video_link_booking;

alter table video_link_booking
    add column offender_booking_id bigint;

alter table video_link_booking
    add column court_name varchar(50);

alter table video_link_booking
    add column court_id varchar(50);

alter table video_link_booking
    add column made_by_the_court boolean;

alter table video_link_booking
    add column created_by_username varchar(50);

alter table video_link_appointment
    add column video_link_booking_id integer;

update video_link_booking b
set (offender_booking_id, court_name, court_id, made_by_the_court, created_by_username) = (
    select booking_id, court, court_id, made_by_the_court, created_by_username
    from video_link_appointment a
    where b.main_appointment = a.id
);

alter table video_link_appointment
    drop column booking_id;

alter table video_link_appointment
    drop column court;

alter table video_link_appointment
    drop column court_id;

alter table video_link_appointment
    drop column made_by_the_court;

alter table video_link_appointment
    drop column created_by_username;

alter table video_link_booking
    alter column offender_booking_id set not null;

alter table video_link_booking
    alter column made_by_the_court set default true;

alter table video_link_booking
    alter column made_by_the_court set not null;

update video_link_appointment a
set video_link_booking_id = (select id from video_link_booking b where b.main_appointment = a.id)
where hearing_type = 'MAIN';

update video_link_appointment a
set video_link_booking_id = (select id from video_link_booking b where b.pre_appointment = a.id)
where hearing_type = 'PRE';

update video_link_appointment a
set video_link_booking_id = (select id from video_link_booking b where b.post_appointment = a.id)
where hearing_type = 'POST';

delete from video_link_appointment where video_link_booking_id is null;

alter table video_link_appointment
    alter column video_link_booking_id set not null;

alter table video_link_appointment
    add constraint video_link_appointment_fk foreign key (video_link_booking_id) references video_link_booking(id);

alter table video_link_booking
    drop column main_appointment;

alter table video_link_booking
    drop column pre_appointment;

alter table video_link_booking
    drop column post_appointment;

alter table video_link_appointment
    add constraint video_link_appointment_unique unique (video_link_booking_id, hearing_type);
