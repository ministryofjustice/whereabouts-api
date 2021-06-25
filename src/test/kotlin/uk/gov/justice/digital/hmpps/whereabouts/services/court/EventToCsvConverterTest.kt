package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEvent
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import java.time.LocalDateTime
import java.util.stream.Stream

class EventToCsvConverterTest {
  val expectedHeader =
    "eventId,timestamp,videoLinkBookingId,eventType,agencyId,court,madeByTheCourt,mainStartTime,mainEndTime,preStartTime,preEndTime,postStartTime,postEndTime\n"
  val exepectedRow =
    "1000,2021-03-01T00:00:00,1,CREATE,MDI,\"Court with ,.\"\"' characters\",false,2021-03-01T09:00:00,2021-03-01T10:00:00,2021-03-01T08:00:00,2021-03-01T09:00:00,2021-03-01T10:00:00,2021-03-01T11:00:00\n"

  val event = VideoLinkBookingEvent(
    eventId = 1000L,
    timestamp = LocalDateTime.of(2021, 3, 1, 0, 0),
    eventType = VideoLinkBookingEventType.CREATE,
    userId = "X",
    videoLinkBookingId = 1L,
    agencyId = "MDI",
    comment = "Comment",
    court = "Court with ,.\"' characters",
    courtId = "EYI",
    madeByTheCourt = false,
    offenderBookingId = 2L,
    mainLocationId = 10L,
    mainStartTime = LocalDateTime.of(2021, 3, 1, 9, 0),
    mainEndTime = LocalDateTime.of(2021, 3, 1, 10, 0),
    mainNomisAppointmentId = 11L,
    preLocationId = 20L,
    preStartTime = LocalDateTime.of(2021, 3, 1, 8, 0),
    preEndTime = LocalDateTime.of(2021, 3, 1, 9, 0),
    preNomisAppointmentId = 21L,
    postLocationId = 30L,
    postStartTime = LocalDateTime.of(2021, 3, 1, 10, 0),
    postEndTime = LocalDateTime.of(2021, 3, 1, 11, 0),
    postNomisAppointmentId = 31L,
  )

  @Test
  fun convertEmptyList() {
    val converter = EventToCsvConverter()
    assertThat(converter.toCsv(Stream.of())).isEqualTo(expectedHeader)
  }

  @Test
  fun convertSingleEvent() {
    val converter = EventToCsvConverter()
    assertThat(converter.toCsv(Stream.of(event))).isEqualTo(expectedHeader + exepectedRow)
  }

  @Test
  fun convertSeveralEvents() {
    val converter = EventToCsvConverter()
    assertThat(
      converter.toCsv(
        Stream.of(
          event,
          event,
          event,
          VideoLinkBookingEvent(
            eventType = VideoLinkBookingEventType.DELETE,
            timestamp = LocalDateTime.of(2021, 3, 1, 1, 1, 1, 1),
            videoLinkBookingId = 99L
          )
        )
      )
    ).isEqualTo(
      expectedHeader +
        exepectedRow +
        exepectedRow +
        exepectedRow +
        ",\"2021-03-01T01:01:01.000000001\",99,DELETE,,,,,,,,,\n"
    )
  }
}
