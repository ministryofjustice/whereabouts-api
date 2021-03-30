package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.findByDatesBetween
import java.time.LocalDate

@Service
class VideoLinkBookingEventService(
  val repository: VideoLinkBookingEventRepository,
  val csvConverter: EventToCsvConverter
) {
  @Transactional(readOnly = true)
  fun getEventsAsCSV(startDate: LocalDate, days: Long): String =
    csvConverter.toCsv(repository.findByDatesBetween(startDate, startDate.plusDays(days)))
}
