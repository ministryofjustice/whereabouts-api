create table if not exists video_link_appointment_pre_DCS_703 as table video_link_appointment;
create table if not exists video_link_booking_pre_DCS_703 as table video_link_booking;

alter table video_link_booking
    add column booking_id bigint;

alter table video_link_booking
    add column court varchar(50);

alter table video_link_booking
    add column made_by_the_court boolean default true;

alter table video_link_booking
    add column created_by_username varchar(50);


update video_link_booking vlb
set (booking_id,
     court,
     made_by_the_court,
     created_by_username) = (
         select booking_id,
                court,
                made_by_the_court,
                created_by_username,
                appointment_id
         from video_link_appointment vla
         where vlb.main_appointment = vla.id
    );


alter table video_link_booking
    alter column booking_id set not null;

alter table video_link_booking
    alter column court set not null;

alter table video_link_appointment drop column booking_id;
alter table video_link_appointment drop column court;
alter table video_link_appointment drop column hearing_type;
alter table video_link_appointment drop column created_by_username;
alter table video_link_appointment drop column made_by_the_court;
