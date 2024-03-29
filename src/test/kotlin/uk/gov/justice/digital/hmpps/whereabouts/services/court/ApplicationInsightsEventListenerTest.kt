package uk.gov.justice.digital.hmpps.whereabouts.services.court

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers

class ApplicationInsightsEventListenerTest {
  private val authenticationFacade: AuthenticationFacade = mock()
  private val telemetryClient: TelemetryClient = mock()

  private val booking = EventListenerTestData.booking
  private val appointment = DataHelpers.makeVideoLinkAppointment()
  private val appointmentChangedEventMessage = DataHelpers.makeAppointmentChangedEventMessage()
  private val createSpecification = EventListenerTestData.createSpecification
  private val updateSpecification = EventListenerTestData.updateSpecification
  private val startTime = EventListenerTestData.startTime

  private val listener = ApplicationInsightsEventListener(authenticationFacade, telemetryClient)

  @BeforeEach
  fun programMocks() {
    whenever(authenticationFacade.currentUsername).thenReturn("A_USER")
  }

  @Test
  fun `create event`() {
    listener.bookingCreated(booking, createSpecification)

    verify(telemetryClient).trackEvent(
      "VideoLinkBookingCreated",
      mapOf(
        "id" to "11",
        "bookingId" to "-1",
        "court" to "York Crown Court",
        "courtId" to "TSTCRT",
        "agencyId" to "WWI",
        "user" to "A_USER",
        "madeByTheCourt" to "true",
        "preAppointmentId" to "12",
        "preId" to "120",
        "preStart" to startTime.toString(),
        "preEnd" to startTime.plusMinutes(30).toString(),
        "mainAppointmentId" to "13",
        "mainId" to "130",
        "mainStart" to startTime.plusMinutes(60).toString(),
        "mainEnd" to startTime.plusMinutes(90).toString(),
        "postAppointmentId" to "14",
        "postId" to "140",
        "postStart" to startTime.plusMinutes(120).toString(),
        "postEnd" to startTime.plusMinutes(150).toString(),
      ),
      null,
    )
  }

  @Test
  fun `update event`() {
    listener.bookingUpdated(booking, updateSpecification)

    verify(telemetryClient).trackEvent(
      "VideoLinkBookingUpdated",
      mapOf(
        "id" to "11",
        "user" to "A_USER",
        "bookingId" to "-1",
        "agencyId" to "WWI",
        "courtId" to "TSTCRT2",
        "preAppointmentId" to "12",
        "preId" to "120",
        "preStart" to startTime.toString(),
        "preEnd" to startTime.plusMinutes(30).toString(),
        "mainAppointmentId" to "13",
        "mainId" to "130",
        "mainStart" to startTime.plusMinutes(60).toString(),
        "mainEnd" to startTime.plusMinutes(90).toString(),
        "postAppointmentId" to "14",
        "postId" to "140",
        "postStart" to startTime.plusMinutes(120).toString(),
        "postEnd" to startTime.plusMinutes(150).toString(),
      ),
      null,
    )
  }

  @Test
  fun `delete event`() {
    listener.bookingDeleted(booking)

    verify(telemetryClient).trackEvent(
      "VideoLinkBookingDeleted",
      mapOf(
        "id" to "11",
        "user" to "A_USER",
        "bookingId" to "-1",
        "court" to "York Crown Court",
        "courtId" to "TSTCRT",
        "preAppointmentId" to "12",
        "preId" to "120",
        "mainAppointmentId" to "13",
        "mainId" to "130",
        "postAppointmentId" to "14",
        "postId" to "140",
      ),
      null,
    )
  }

  @Test
  fun `update event in nomis`() {
    listener.appointmentUpdatedInNomis(appointment, appointmentChangedEventMessage)

    verify(telemetryClient).trackEvent(
      "VideoLinkAppointmentUpdated",
      mapOf(
        "bookingId" to "1",
        "appointmentId" to "1",
        "agencyId" to "WWI",
        "court" to "The Court",
        "courtId" to "TSTCRT",
        "current_startDate" to "2022-01-01T10:00",
        "current_endDate" to "2022-01-01T11:00",
        "update_startDate" to "2022-01-01T11:00",
        "update_endDate" to "2022-01-01T12:00",
      ),
      null,
    )
  }
}
