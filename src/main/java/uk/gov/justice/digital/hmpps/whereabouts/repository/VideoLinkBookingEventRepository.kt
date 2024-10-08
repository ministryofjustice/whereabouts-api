package uk.gov.justice.digital.hmpps.whereabouts.repository

import jakarta.persistence.QueryHint
import org.hibernate.jpa.AvailableHints.HINT_CACHEABLE
import org.hibernate.jpa.AvailableHints.HINT_FETCH_SIZE
import org.hibernate.jpa.AvailableHints.HINT_READ_ONLY
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream

interface VideoLinkBookingEventRepository : JpaRepository<VideoLinkBookingEvent, Long>, PagingAndSortingRepository<VideoLinkBookingEvent, Long> {
  @QueryHints(
    value = [
      QueryHint(name = HINT_FETCH_SIZE, value = "" + Integer.MAX_VALUE),
      QueryHint(name = HINT_CACHEABLE, value = "false"),
      QueryHint(name = HINT_READ_ONLY, value = "true"),
    ],
  )
  fun findByTimestampBetweenOrderByEventId(start: LocalDateTime, end: LocalDateTime): Stream<VideoLinkBookingEvent>

  @Query(
    nativeQuery = true,
    value = """
    select
       vlbe.*
    from video_link_booking_event vlbe
    left join video_link_booking_event later on
      vlbe.video_link_booking_id = later.video_link_booking_id
      and vlbe.timestamp < later.timestamp
    where
      later.video_link_booking_id is null
      and vlbe.main_start_time between :start and :end
      and vlbe.event_type != 'DELETE'
    order by vlbe.main_start_time asc
    """,
  )
  fun findEventsByBookingTime(
    @Param("start") start: LocalDateTime,
    @Param("end") end: LocalDateTime,
  ): Stream<VideoLinkBookingEvent>

  fun findEventsByVideoLinkBookingId(videoLinkBookingId: Long): List<VideoLinkBookingEvent>

  fun findAllByMainStartTimeGreaterThanAndEventTypeEquals(
    fromDate: LocalDateTime,
    eventType: VideoLinkBookingEventType = VideoLinkBookingEventType.CREATE,
    pageable: Pageable,
  ): Page<VideoLinkBookingEvent>
}

fun VideoLinkBookingEventRepository.findByDatesBetween(
  start: LocalDate,
  end: LocalDate,
): Stream<VideoLinkBookingEvent> = findByTimestampBetweenOrderByEventId(
  start.atStartOfDay(),
  end.atEndOfDay(),
)

fun VideoLinkBookingEventRepository.findByStartTimeBetween(
  start: LocalDate,
  end: LocalDate,
): Stream<VideoLinkBookingEvent> = findEventsByBookingTime(
  start.atStartOfDay(),
  end.atEndOfDay(),
)

private fun LocalDate.atEndOfDay() = this.plusDays(1).atStartOfDay().minusSeconds(1)
