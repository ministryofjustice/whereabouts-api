package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.hibernate.jpa.QueryHints.HINT_CACHEABLE
import org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE
import org.hibernate.jpa.QueryHints.HINT_READONLY
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.QueryHints
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream
import javax.persistence.QueryHint

interface VideoLinkBookingEventRepository : JpaRepository<VideoLinkBookingEvent, Long> {
  @QueryHints(
    value = [
      QueryHint(name = HINT_FETCH_SIZE, value = "" + Integer.MAX_VALUE),
      QueryHint(name = HINT_CACHEABLE, value = "false"),
      QueryHint(name = HINT_READONLY, value = "true")
    ]
  )
  fun findByTimestampBetweenOrderByEventId(start: LocalDateTime, end: LocalDateTime): Stream<VideoLinkBookingEvent>
}

fun VideoLinkBookingEventRepository.findByDatesBetween(
  start: LocalDate,
  end: LocalDate
): Stream<VideoLinkBookingEvent> = findByTimestampBetweenOrderByEventId(
  start.atStartOfDay(),
  end.plusDays(1).atStartOfDay().minusSeconds(1)
)
