package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationIdAndDescription
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingEventRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.LocationService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.stream.Stream

class VideoLinkBookingEventServiceTest {
  private val videoLinkBookingEventRepository: VideoLinkBookingEventRepository = mock()
  private val eventToCsvConverter = EventToCsvConverter()
  private val eventWithRoomsToCsvConverter = EventWithRoomsToCsvConverter()
  private val courtsService: CourtService = mock()
  private val locationService: LocationService = mock()
  private lateinit var service: VideoLinkBookingEventService

  @BeforeEach
  fun createService() {
    service = VideoLinkBookingEventService(
      videoLinkBookingEventRepository,
      eventToCsvConverter,
      eventWithRoomsToCsvConverter,
      courtsService,
      locationService,
    )
  }

  @Nested
  inner class `find by timestamp between` {
    @Test
    fun `Should include court name in csv object when courtId is present`() {
      val eventStream = Stream.of(
        VideoLinkBookingEvent(
          courtId = "EYI",
          eventType = VideoLinkBookingEventType.CREATE,
          timestamp = LocalDateTime.of(2021, Month.JUNE, 15, 3, 15),
          videoLinkBookingId = 2L,
        ),
      )

      whenever(videoLinkBookingEventRepository.findByTimestampBetweenOrderByEventId(any(), any())).thenReturn(
        eventStream,
      )
      whenever(courtsService.getCourtNameForCourtId("EYI")).thenReturn("Elmley")

      val events = service.getEventsAsCSV(LocalDate.of(2021, Month.JUNE, 1), 7L)

      verify(courtsService).getCourtNameForCourtId("EYI")
      assertThat(events).contains("2021-06-15T03:15:00,2,CREATE,,Elmley,EYI,,,,,,,")
    }

    @Test
    fun `Should return court when courtId is not present`() {
      val court = "Elmley"

      val eventStream = Stream.of(
        VideoLinkBookingEvent(
          court = court,
          eventType = VideoLinkBookingEventType.CREATE,
          timestamp = LocalDateTime.of(2021, Month.JUNE, 15, 3, 15),
          videoLinkBookingId = 2L,
        ),
      )

      whenever(videoLinkBookingEventRepository.findByTimestampBetweenOrderByEventId(any(), any())).thenReturn(
        eventStream,
      )

      val events = service.getEventsAsCSV(LocalDate.of(2021, Month.JUNE, 1), 7L)

      assertThat(events).contains("2021-06-15T03:15:00,2,CREATE,,Elmley,,,,,,,,")
    }

    @Test
    fun `Should not return court when court is not present`() {
      val eventStream = Stream.of(
        VideoLinkBookingEvent(
          courtId = "EYI",
          eventType = VideoLinkBookingEventType.CREATE,
          timestamp = LocalDateTime.of(2021, Month.JUNE, 15, 3, 15),
          videoLinkBookingId = 2L,
        ),
      )

      whenever(videoLinkBookingEventRepository.findByTimestampBetweenOrderByEventId(any(), any())).thenReturn(
        eventStream,
      )
      whenever(courtsService.getCourtNameForCourtId("EYI")).thenReturn(null)

      val events = service.getEventsAsCSV(LocalDate.of(2021, Month.JUNE, 1), 7L)

      assertThat(events).contains("2021-06-15T03:15:00,2,CREATE,,,EYI,,,,,,,")
    }

    @Test
    fun `Should not return court when both courtId and court are not present`() {
      val eventStream = Stream.of(
        VideoLinkBookingEvent(
          eventType = VideoLinkBookingEventType.CREATE,
          timestamp = LocalDateTime.of(2021, Month.JUNE, 15, 3, 15),
          videoLinkBookingId = 2L,
        ),
      )

      whenever(videoLinkBookingEventRepository.findByTimestampBetweenOrderByEventId(any(), any())).thenReturn(
        eventStream,
      )

      val events = service.getEventsAsCSV(LocalDate.of(2021, Month.JUNE, 1), 7L)

      assertThat(events).contains("2021-06-15T03:15:00,2,CREATE,,,,,,,,,,")
    }

    @Test
    fun `Should only call location service once per prison`() {
      val court = "Elmley"

      val eventStream = Stream.of(
        VideoLinkBookingEvent(
          agencyId = "MDI",
          court = court,
          eventType = VideoLinkBookingEventType.CREATE,
          mainLocationId = 101,
          preLocationId = 102,
          postLocationId = 103,
          timestamp = LocalDateTime.of(2021, Month.JUNE, 15, 3, 15),
          videoLinkBookingId = 2L,
        ),
        VideoLinkBookingEvent(
          agencyId = "BXI",
          court = court,
          eventType = VideoLinkBookingEventType.CREATE,
          mainLocationId = 404,
          timestamp = LocalDateTime.of(2021, Month.JUNE, 15, 3, 15),
          videoLinkBookingId = 2L,
        ),
      )
      val location1Stream = listOf(
        LocationIdAndDescription(
          locationId = 101,
          description = "Room 101",
        ),
        LocationIdAndDescription(
          locationId = 102,
          description = "Room 102",
        ),
        LocationIdAndDescription(
          locationId = 103,
          description = "Room 103",
        ),
      )
      val location2Stream = listOf(
        LocationIdAndDescription(
          locationId = 404,
          description = "Video Room A",
        ),
      )

      whenever(videoLinkBookingEventRepository.findByTimestampBetweenOrderByEventId(any(), any())).thenReturn(
        eventStream,
      )
      whenever(locationService.getAllLocationsForPrison("MDI")).thenReturn(location1Stream)
      whenever(locationService.getAllLocationsForPrison("BXI")).thenReturn(location2Stream)

      val events = service.getEventsAsCSV(LocalDate.of(2021, Month.JUNE, 1), 7L, true)

      assertThat(events).contains("2021-06-15T03:15:00,2,CREATE,MDI,Elmley,,,,,,,,,\"Room 101\",\"Room 102\",\"Room 103\"")
      assertThat(events).contains("2021-06-15T03:15:00,2,CREATE,BXI,Elmley,,,,,,,,,\"Video Room A\"")

      verify(locationService, times(2)).getAllLocationsForPrison(any())
    }
  }

  @Nested
  inner class `find by start date between` {
    @Test
    fun `Should include court name in csv object when courtId is present`() {
      val eventStream = Stream.of(
        VideoLinkBookingEvent(
          courtId = "EYI",
          eventType = VideoLinkBookingEventType.CREATE,
          timestamp = LocalDateTime.of(2021, Month.JUNE, 15, 3, 15),
          videoLinkBookingId = 2L,
        ),
      )

      whenever(videoLinkBookingEventRepository.findEventsByBookingTime(any(), any())).thenReturn(
        eventStream,
      )
      whenever(courtsService.getCourtNameForCourtId("EYI")).thenReturn("Elmley")

      val events = service.getBookingsByStartDateAsCSV(LocalDate.of(2021, Month.JUNE, 1), 7L)

      verify(courtsService).getCourtNameForCourtId("EYI")
      assertThat(events).contains("2021-06-15T03:15:00,2,CREATE,,Elmley,EYI,,,,,,,")
    }

    @Test
    fun `Should return court when courtId is not present`() {
      val court = "Elmley"

      val eventStream = Stream.of(
        VideoLinkBookingEvent(
          court = court,
          eventType = VideoLinkBookingEventType.CREATE,
          timestamp = LocalDateTime.of(2021, Month.JUNE, 15, 3, 15),
          videoLinkBookingId = 2L,
        ),
      )

      whenever(videoLinkBookingEventRepository.findEventsByBookingTime(any(), any())).thenReturn(
        eventStream,
      )

      val events = service.getBookingsByStartDateAsCSV(LocalDate.of(2021, Month.JUNE, 1), 7L)

      assertThat(events).contains("2021-06-15T03:15:00,2,CREATE,,Elmley,,,,,,,,")
    }

    @Test
    fun `Should not return court when court is not present`() {
      val eventStream = Stream.of(
        VideoLinkBookingEvent(
          courtId = "EYI",
          eventType = VideoLinkBookingEventType.CREATE,
          timestamp = LocalDateTime.of(2021, Month.JUNE, 15, 3, 15),
          videoLinkBookingId = 2L,
        ),
      )

      whenever(videoLinkBookingEventRepository.findEventsByBookingTime(any(), any())).thenReturn(
        eventStream,
      )
      whenever(courtsService.getCourtNameForCourtId("EYI")).thenReturn(null)

      val events = service.getBookingsByStartDateAsCSV(LocalDate.of(2021, Month.JUNE, 1), 7L)

      assertThat(events).contains("2021-06-15T03:15:00,2,CREATE,,,EYI,,,,,,,")
    }

    @Test
    fun `Should not return court when both courtId and court are not present`() {
      val eventStream = Stream.of(
        VideoLinkBookingEvent(
          eventType = VideoLinkBookingEventType.CREATE,
          timestamp = LocalDateTime.of(2021, Month.JUNE, 15, 3, 15),
          videoLinkBookingId = 2L,
        ),
      )

      whenever(videoLinkBookingEventRepository.findEventsByBookingTime(any(), any())).thenReturn(
        eventStream,
      )

      val events = service.getBookingsByStartDateAsCSV(LocalDate.of(2021, Month.JUNE, 1), 7L)

      assertThat(events).contains("2021-06-15T03:15:00,2,CREATE,,,,,,,,,,")
    }
  }
}
