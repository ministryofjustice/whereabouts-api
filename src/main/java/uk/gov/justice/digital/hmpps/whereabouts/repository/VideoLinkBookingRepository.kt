package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking

@Repository
interface VideoLinkBookingRepository : CrudRepository<VideoLinkBooking, Long>
