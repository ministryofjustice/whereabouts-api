package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking

interface VideoLinkBookingRepository : JpaRepository<VideoLinkBooking, Long> {
  @Query(
    """
      select b
        from VideoLinkBooking b
       where b.main.appointmentId in ?1
    """
  )
  fun findByMainAppointmentIds(ids: Set<Long>): List<VideoLinkBooking>

  @Query(
    """
      select b
        from VideoLinkBooking b
       where b.pre is not null and b.pre.appointmentId in ?1
    """
  )
  fun findByPreAppointmentIds(ids: Set<Long>): List<VideoLinkBooking>

  @Query(
    """
      select b
        from VideoLinkBooking b
       where b.post is not null and b.post.appointmentId in ?1
    """
  )
  fun findByPostAppointmentIds(ids: Set<Long>): List<VideoLinkBooking>
}
