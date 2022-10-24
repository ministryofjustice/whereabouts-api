package uk.gov.justice.digital.hmpps.whereabouts.services

import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import wiremock.org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

class EventListenerTest {
  private val attendanceService: AttendanceService = mock()
  private val videoLinkBookingService: VideoLinkBookingService = mock()

  @Test
  fun `should call delete with the correct offenderNo`() {
    val eventListener = SqsEventListener(attendanceService, videoLinkBookingService, Gson())
    eventListener.handleEvents(getJson("/services/offender-deletion-request.json"))
    verify(attendanceService).deleteAttendancesForOffenderDeleteEvent("A1234AA", listOf(321L, 322L))
  }

  @Test
  fun `should call process nomis update when event type is APPOINTMENT_CHANGED`() {
    val eventListener = SqsEventListener(attendanceService, videoLinkBookingService, Gson())
    eventListener.handleEvents(getJson("/services/appointment-deleted-request.json"))
    verify(videoLinkBookingService).processNomisUpdate(484209875, true)
  }

  private fun getJson(filename: String): String {
    return IOUtils.toString(javaClass.getResourceAsStream(filename), StandardCharsets.UTF_8.toString())
  }
}
