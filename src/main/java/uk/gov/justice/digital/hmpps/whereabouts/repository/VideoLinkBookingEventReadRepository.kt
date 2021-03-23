/*
package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.jdbc.core.RowMapperResultSetExtractor
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

*/
/**
 *  JDBC template repository for bulk read of events.
 *  Implemented using a jdbc template instead of JPA because JPA features are a hindrance here (wrapping
 *  objects with proxies, object caching etc)
 *//*

@Repository
class VideoLinkBookingEventReadRepository(val template: NamedParameterJdbcTemplate) {
  fun findByDatesBetween(start: LocalDate, end: LocalDate) {
    findByTimesBetween(
      start.atStartOfDay(),
      end.plusDays(1).atStartOfDay().minusSeconds(1)
    )
  }

  private fun findByTimesBetween(startTime: LocalDateTime, endTime: LocalDateTime) {
    template.queryForStream(
      """
        select event_id,                  
               timestamp,                 
               event_type,                
               user_id,                  
               video_link_booking_id,     
               agency_id,                 
               offender_booking_id,       
               court,                     
               made_by_the_court,         
               comment,                   
               main_nomis_appointment_id, 
               main_location_id,          
               main_start_time,           
               main_end_time,             
               pre_nomis_appointment_id,  
               pre_location_id,           
               pre_start_time,            
               pre_end_time,              
               post_nomis_appointment_id, 
               post_location_id,          
               post_start_time,           
               post_end_time             
          from video_link_booking_event
         where timestamp between :startTime and :endTime
      order by event_id   
      """,
      mapOf("startTime" to startTime, "endTime" to endTime),
    )
  }
}*/
