package uk.gov.justice.digital.hmpps.whereabouts.services.court

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.stream.Stream

class VideoLinkBookingEventServiceTest {
  private val videoLinkBookingEventRepository: VideoLinkBookingEventRepository = mock()
  private val eventToCsvConverter = EventToCsvConverter()
  private val courtsService: CourtService = mock()

  @Test
  fun `Should include court name in csv object`() {
    val service = VideoLinkBookingEventService(
      videoLinkBookingEventRepository,
      eventToCsvConverter,
      courtsService,
    )

    val eventStream = Stream.of(
      VideoLinkBookingEvent(
        courtId = "EYI",
        eventType = VideoLinkBookingEventType.CREATE,
        timestamp = LocalDateTime.of(2021, Month.JUNE, 15, 3, 15),
        videoLinkBookingId = 2L
      )
    )

    whenever(videoLinkBookingEventRepository.findByTimestampBetween(any(), any())).thenReturn(eventStream)
    whenever(courtsService.getCourtNameForCourtId("EYI")).thenReturn("Elmley")

    val events = service.getEventsAsCSV(LocalDate.of(2021, Month.JUNE, 1), 7L)

    verify(courtsService).getCourtNameForCourtId("EYI")
    assertThat(events).contains("2021-06-15T03:15:00,2,CREATE,,Elmley,EYI,,,,,,,")
  }
}