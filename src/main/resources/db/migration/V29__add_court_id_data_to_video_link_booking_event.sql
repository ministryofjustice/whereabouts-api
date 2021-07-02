UPDATE video_link_booking_event vlbe
   SET court_id=(
       SELECT vlb.court_id
         FROM video_link_booking vlb
        WHERE vlb.id = vlbe.video_link_booking_id
       )
 WHERE vlbe.court_id IS NULL
       AND vlbe.event_type = 'CREATE' ;

