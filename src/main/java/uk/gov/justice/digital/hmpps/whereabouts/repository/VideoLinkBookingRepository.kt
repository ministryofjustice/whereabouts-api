package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking

interface VideoLinkBookingRepository : JpaRepository<VideoLinkBooking, Long> {
  @Query(
    """
      select distinct b
        from VideoLinkBooking b 
             left outer join fetch b.appointments
       where b.id in (
              select a.videoLinkBooking.id 
               from VideoLinkAppointment a
              where a.appointmentId in ?1 and a.hearingType = ?2 
             )
          """
  )
  fun findByAppointmentIdsAndHearingType(
    ids: List<Long>,
    hearingType: HearingType = HearingType.MAIN
  ): List<VideoLinkBooking>
}
