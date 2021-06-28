package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.findByDatesBetween
import java.time.LocalDate

@Service
class VideoLinkBookingEventService(
  val repository: VideoLinkBookingEventRepository,
  val csvConverter: EventToCsvConverter,
  val courtsService: CourtService
) {
  @Transactional(readOnly = true)
  fun getEventsAsCSV(startDate: LocalDate, days: Long): String {
    val bookingEvents = repository.findByDatesBetween(startDate, startDate.plusDays(days))
    val bookingEventsIncludingCourtNames = bookingEvents.map {
      val courtId = it.courtId
      val courtName = if (courtId != null) courtsService.getCourtNameForCourtId(courtId) else null
      it.copy(court = courtName)
    }
    return csvConverter.toCsv(bookingEventsIncludingCourtNames)
  }
}
