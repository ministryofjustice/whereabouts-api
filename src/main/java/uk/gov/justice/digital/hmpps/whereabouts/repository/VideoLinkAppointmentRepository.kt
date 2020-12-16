package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment

interface VideoLinkAppointmentRepository : CrudRepository<VideoLinkAppointment, Long> {
  override fun findAll(): Set<VideoLinkAppointment>
  fun findVideoLinkAppointmentByAppointmentIdIn(appointmentIds: Set<Long>): Set<VideoLinkAppointment>

  @Query(
    """
    select distinct a.bookingId as bookingId 
      from VideoLinkAppointment a 
           left join VideoLinkBooking b on b.main = a 
     where b is null 
           and a.hearingType = ?1
     """
  )
  fun bookingIdsOfUnlinkedAppointments(hearingType: HearingType = HearingType.MAIN): List<Long>

  @Query(
    """
    select a 
      from VideoLinkAppointment a
           left join VideoLinkBooking b on b.main = a 
     where b is null
           and a.bookingId = ?1
           and a.hearingType = ?2
     """
  )
  fun unlinkedMainAppointmentsForBookingId(
    bookingId: Long,
    hearingType: HearingType = HearingType.MAIN
  ): List<VideoLinkAppointment>

  @Query(
    """
    select a 
      from VideoLinkAppointment a
           left join VideoLinkBooking b on b.pre = a 
     where b is null
           and a.bookingId = ?1
           and a.hearingType = ?2
     """
  )
  fun unlinkedPreAppointmentsForBookingId(
    bookingId: Long,
    hearingType: HearingType = HearingType.PRE
  ): List<VideoLinkAppointment>

  @Query(
    """
    select a 
      from VideoLinkAppointment a
           left join VideoLinkBooking b on b.post = a 
     where b is null
           and a.bookingId = ?1
           and a.hearingType = ?2
     """
  )
  fun unlinkedPostAppointmentsForBookingId(
    bookingId: Long,
    hearingType: HearingType = HearingType.POST
  ): List<VideoLinkAppointment>
}
