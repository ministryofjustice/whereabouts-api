package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.findByDatesBetween
import uk.gov.justice.digital.hmpps.whereabouts.repository.findByStartTimeBetween
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
    val bookingEventsIncludingCourtNames = bookingEvents.map(::withCourtName)
    return csvConverter.toCsv(bookingEventsIncludingCourtNames)
  }

  @Transactional(readOnly = true)
  fun getBookingsByStartDateAsCSV(startDate: LocalDate, days: Long): String {
    val bookingEvents = repository.findByStartTimeBetween(startDate, startDate.plusDays(days))
    val bookingEventsIncludingCourtNames = bookingEvents.map(::withCourtName)
    return csvConverter.toCsv(bookingEventsIncludingCourtNames)
  }

  private fun withCourtName(event: VideoLinkBookingEvent): VideoLinkBookingEvent {
    val courtName = event.courtId?.let { courtsService.getCourtNameForCourtId(it) } ?: event.court
    return event.copy(court = courtName)
  }
}
