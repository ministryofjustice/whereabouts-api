delete from enabled_court where id = 'DRBYMC';

update enabled_court set id = 'DRBYMC' where id = 'DRBYJC';

update video_link_appointment vla
    set court_id = (
            select ec.id
            from enabled_court ec
            where ec.name = vla.court
        )
where vla.court_id is null;
