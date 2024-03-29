package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventWithRoomNames
import java.time.LocalDateTime

class EventToCsvConverterTest {
  val expectedHeader =
    "eventId,timestamp,videoLinkBookingId,eventType,agencyId,court,courtId,madeByTheCourt,mainStartTime,mainEndTime,preStartTime,preEndTime,postStartTime,postEndTime,mainLocationName,preLocationName,postLocationName\n"
  val exepectedRow =
    "1000,2021-03-01T00:00:00,1,CREATE,MDI,\"Court with ,.\"\"' characters\",EYI,false,2021-03-01T09:00:00,2021-03-01T10:00:00,2021-03-01T08:00:00,2021-03-01T09:00:00,2021-03-01T10:00:00,2021-03-01T11:00:00,\"Room A\",\"Room B\",\"Room C\"\n"

  val event = VideoLinkBookingEventWithRoomNames(
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
    mainLocationName = "Room A",
    mainStartTime = LocalDateTime.of(2021, 3, 1, 9, 0),
    mainEndTime = LocalDateTime.of(2021, 3, 1, 10, 0),
    mainNomisAppointmentId = 11L,
    preLocationId = 20L,
    preLocationName = "Room B",
    preStartTime = LocalDateTime.of(2021, 3, 1, 8, 0),
    preEndTime = LocalDateTime.of(2021, 3, 1, 9, 0),
    preNomisAppointmentId = 21L,
    postLocationId = 30L,
    postLocationName = "Room C",
    postStartTime = LocalDateTime.of(2021, 3, 1, 10, 0),
    postEndTime = LocalDateTime.of(2021, 3, 1, 11, 0),
    postNomisAppointmentId = 31L,
  )

  @Test
  fun convertEmptyList() {
    val converter = EventToCsvConverter()
    assertThat(converter.toCsv(listOf())).isEqualTo(expectedHeader)
  }

  @Test
  fun convertSingleEvent() {
    val converter = EventToCsvConverter()
    assertThat(converter.toCsv(listOf(event))).isEqualTo(expectedHeader + exepectedRow)
  }

  @Test
  fun convertSeveralEvents() {
    val converter = EventToCsvConverter()
    assertThat(
      converter.toCsv(
        listOf<VideoLinkBookingEventWithRoomNames>(
          event,
          event,
          event,
          VideoLinkBookingEventWithRoomNames(
            eventType = VideoLinkBookingEventType.DELETE,
            timestamp = LocalDateTime.of(2021, 3, 1, 1, 1, 1, 1),
            videoLinkBookingId = 99L,
            courtId = "EYI",
          ),
        ),
      ),
    ).isEqualTo(
      expectedHeader +
        exepectedRow +
        exepectedRow +
        exepectedRow +
        ",\"2021-03-01T01:01:01.000000001\",99,DELETE,,,EYI,,,,,,,,,,\n",
    )
  }
}
