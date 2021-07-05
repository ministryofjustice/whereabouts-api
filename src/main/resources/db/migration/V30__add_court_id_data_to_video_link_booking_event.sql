update video_link_booking_event vlbe
   set court_id = (
       select id
         from enabled_court ec
        where vlbe.court = ec.name
       )
 where vlbe.court_id is null;

