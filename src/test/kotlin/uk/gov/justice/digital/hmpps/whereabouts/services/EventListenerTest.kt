package uk.gov.justice.digital.hmpps.whereabouts.services

import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import wiremock.org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

class EventListenerTest {
  private val attendanceService: AttendanceService = mock()

  @Test
  fun `should call delete with the correct offenderNo`() {
    val eventListener = EventListener(attendanceService, Gson())
    eventListener.handleEvents(getJson("/services/offender-deletion-request.json"))
    verify(attendanceService).deleteAttendancesForOffenderDeleteEvent("A1234AA", listOf(321L, 322L))
  }

  private fun getJson(filename: String): String {
    return IOUtils.toString(javaClass.getResourceAsStream(filename), StandardCharsets.UTF_8.toString())
  }
}
