package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import wiremock.org.apache.commons.io.IOUtils
import java.io.IOException
import java.nio.charset.StandardCharsets

@RunWith(MockitoJUnitRunner::class)
class EventListenerTest {
  private val attendanceService: AttendanceService = mock()

  @Test
  @Throws(IOException::class)
  fun `should call delete with the correct offenderNo`() {
    val eventListener = EventListener(attendanceService)
    eventListener.handleEvents(getJson("/services/offender-deletion-request.json"))
    verify(attendanceService).deleteAttendances("A1234AA")
  }

  @Throws(IOException::class)
  private fun getJson(filename: String): String {
    return IOUtils.toString(javaClass.getResourceAsStream(filename), StandardCharsets.UTF_8.toString())
  }
}
