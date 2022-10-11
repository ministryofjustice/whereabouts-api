package uk.gov.justice.digital.hmpps.whereabouts.services

import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import wiremock.org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

class EventListenerTest {
  private val attendanceService: AttendanceService = mock()
  private val videoLinkBookingService: VideoLinkBookingService = mock()

  @Test
  fun `should call delete with the correct offenderNo`() {
    val eventListener = EventListener(attendanceService, videoLinkBookingService, Gson())
    eventListener.handleEvents(getJson("/services/offender-deletion-request.json"))
    verify(attendanceService).deleteAttendancesForOffenderDeleteEvent("A1234AA", listOf(321L, 322L))
  }

  @Test
  fun `delete appointment when delete flat is true`() {
    val eventListener = EventListener(attendanceService, videoLinkBookingService, Gson())
    eventListener.handleEvents(getJson("/services/appointment-deleted-request.json"))
    verify(videoLinkBookingService).deleteAppointments(484209875)
  }

  @Test
  fun `skip sqs message when delete flat is false`() {
    val eventListener = EventListener(attendanceService, videoLinkBookingService, Gson())
    eventListener.handleEvents(getJson("/services/appointment-changed-request.json"))
    verify(videoLinkBookingService, times(0)).deleteAppointments(any())
  }

  private fun getJson(filename: String): String {
    return IOUtils.toString(javaClass.getResourceAsStream(filename), StandardCharsets.UTF_8.toString())
  }
}
