package uk.gov.justice.digital.hmpps.whereabouts.listeners

import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers
import wiremock.org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

class SqsOffenderEventListenerTest {
  private val attendanceService: AttendanceService = mock()
  private val videoLinkBookingService: VideoLinkBookingService = mock()
  private val appointmentChangedEventMessage = DataHelpers.makeAppointmentChangedEventMessage(
    bookingId = 1056979,
    scheduleEventId = 484209875,
    recordDeleted = false,
    agencyLocationId = "WWI",
    eventDatetime = "2022-10-06T09:34:40",
    scheduledStartTime = "2022-10-06T11:00:00",
    scheduledEndTime = "2022-10-06T15:00:00",
    scheduleEventStatus = ScheduleEventStatus.SCH,
  )

  private val appointmentChangedEventMessageAndRecordDeleted = DataHelpers.makeAppointmentChangedEventMessage(
    bookingId = 1056979,
    scheduleEventId = 484209875,
    recordDeleted = true,
    agencyLocationId = "WWI",
    eventDatetime = "2022-10-06T09:34:40",
    scheduledStartTime = "2022-10-06T11:00:00",
    scheduledEndTime = "2022-10-06T15:00:00",
    scheduleEventStatus = ScheduleEventStatus.SCH,
  )

  private val appointmentCancelledMessage = DataHelpers.makeAppointmentChangedEventMessage(
    bookingId = 1056979,
    scheduleEventId = 484209875,
    recordDeleted = false,
    agencyLocationId = "WWI",
    eventDatetime = "2022-10-06T09:34:40",
    scheduledStartTime = "2022-10-06T11:00:00",
    scheduledEndTime = "2022-10-06T15:00:00",
    scheduleEventStatus = ScheduleEventStatus.CANC,
  )

  @Test
  fun `should call delete with the correct offenderNo`() {
    val eventListener = SqsOffenderEventListener(attendanceService, videoLinkBookingService, Gson())
    eventListener.handleEvents(getJson("/listeners/offender-deletion-request.json"))
    verify(attendanceService).deleteAttendancesForOffenderDeleteEvent("A1234AA", listOf(321L, 322L))
  }

  @Test
  fun `should call process nomis update when event type is APPOINTMENT_CHANGED and recordDeleted is true`() {
    val eventListener = SqsOffenderEventListener(attendanceService, videoLinkBookingService, Gson())
    eventListener.handleEvents(getJson("/listeners/appointment-deleted-request.json"))
    verify(videoLinkBookingService).processNomisUpdate(appointmentChangedEventMessageAndRecordDeleted)
  }

  @Test
  fun `should call process nomis update when event type is APPOINTMENT_CHANGED and scheduleEventStatus is CANC`() {
    val eventListener = SqsOffenderEventListener(attendanceService, videoLinkBookingService, Gson())
    eventListener.handleEvents(getJson("/listeners/appointment-cancelled-request.json"))
    verify(videoLinkBookingService).processNomisUpdate(appointmentCancelledMessage)
  }

  @Test
  fun `should call process nomis update when event type is APPOINTMENT_CHANGED and scheduleEventStatus is SCH`() {
    val eventListener = SqsOffenderEventListener(attendanceService, videoLinkBookingService, Gson())
    eventListener.handleEvents(getJson("/listeners/appointment-changed-request.json"))
    verify(videoLinkBookingService).processNomisUpdate(appointmentChangedEventMessage)
  }

  private fun getJson(filename: String): String {
    return IOUtils.toString(javaClass.getResourceAsStream(filename), StandardCharsets.UTF_8.toString())
  }
}
