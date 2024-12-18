package uk.gov.justice.digital.hmpps.whereabouts.listeners

import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import wiremock.org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

class SqsOffenderEventListenerTest {
  private val attendanceService: AttendanceService = mock()
  private val listener = SqsOffenderEventListener(attendanceService, Gson())

  @Test
  fun `should call delete with the correct offenderNo`() {
    listener.handleEvents(getJson("/listeners/offender-deletion-request.json"))
    verify(attendanceService).deleteAttendancesForOffenderDeleteEvent("A1234AA", listOf(321L, 322L))
  }

  private fun getJson(filename: String): String {
    return IOUtils.toString(javaClass.getResourceAsStream(filename), StandardCharsets.UTF_8.toString())
  }
}
