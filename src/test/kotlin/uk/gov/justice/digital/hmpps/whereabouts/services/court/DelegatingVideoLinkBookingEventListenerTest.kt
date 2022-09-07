package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import java.time.LocalDateTime

class DelegatingVideoLinkBookingEventListenerTest {

  private val eventStoreListener: EventStoreListener = mock()
  private val applicationInsightsEventListener: ApplicationInsightsEventListener = mock()
  private val courtService: CourtService = mock()
  private lateinit var listener: DelegatingVideoLinkBookingEventListener

  @BeforeEach
  fun createListener() {
    listener = DelegatingVideoLinkBookingEventListener(
      eventStoreListener,
      applicationInsightsEventListener,
      courtService,
    )
  }

  val main = VideoLinkAppointmentSpecification(
    locationId = 2L,
    startTime = LocalDateTime.of(2020, 1, 1, 13, 0),
    endTime = LocalDateTime.of(2020, 1, 1, 13, 30)
  )

  val specification =
    VideoLinkBookingUpdateSpecification(comment = "none ", courtId = "EYI", pre = null, post = null, main = main)

  val appointment = VideoLinkAppointment(
    id = 1,
    appointmentId = 2,
    hearingType = HearingType.MAIN,
    locationId = 20L,
    videoLinkBooking = VideoLinkBooking(offenderBookingId = 1, prisonId = "WWI"),
    startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0),
    endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)
  )

  val booking = VideoLinkBooking(
    id = 123,
    offenderBookingId = 12345,
    courtId = "EYI",
    madeByTheCourt = true,
    prisonId = "WWI"
  ).also {
    it.createdByUsername = "Smith"
    it.appointments.put(HearingType.MAIN, appointment)
  }

  @Test
  fun `Should call eventStore with correct args for Updates`() {
    whenever(courtService.chooseCourtName(booking)).thenReturn("Elmley")
    listener.bookingUpdated(booking, specification)

    argumentCaptor<VideoLinkBooking>().apply {
      verify(eventStoreListener).bookingUpdated(capture(), eq(specification))
      assertThat(firstValue.courtName).isEqualTo("Elmley")
      assertThat(firstValue.createdByUsername).isEqualTo(booking.createdByUsername)
      assertThat(firstValue.appointments).isEqualTo(booking.appointments)
    }
  }

  @Test
  fun `Should call eventStore with correct args for Deletes`() {
    whenever(courtService.chooseCourtName(booking)).thenReturn("Elmley")
    listener.bookingDeleted(booking)

    argumentCaptor<VideoLinkBooking>().apply {
      verify(eventStoreListener).bookingDeleted(capture())
      assertThat(firstValue.courtName).isEqualTo("Elmley")
      assertThat(firstValue.createdByUsername).isEqualTo(booking.createdByUsername)
      assertThat(firstValue.appointments).isEqualTo(booking.appointments)
    }
  }
}
