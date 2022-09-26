package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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
              where a.appointmentId in :ids and a.hearingType = :hearingType 
             )
             and (:courtName is null or b.courtName = :courtName)
             and (:courtId is null   or b.courtId = :courtId)
          """
  )
  fun findByAppointmentIdsAndHearingType(
    @Param("ids") ids: List<Long>,
    @Param("hearingType") hearingType: HearingType = HearingType.MAIN,
    @Param("courtName") courtName: String? = null,
    @Param("courtId") courtId: String? = null
  ): List<VideoLinkBooking>

  fun countByPrisonIdisNull(): Long

  fun findByPrisonIdisNull(pageable: Pageable): List<VideoLinkBooking>
}
