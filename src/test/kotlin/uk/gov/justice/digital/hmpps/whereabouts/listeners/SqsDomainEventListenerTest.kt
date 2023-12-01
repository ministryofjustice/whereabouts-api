package uk.gov.justice.digital.hmpps.whereabouts.listeners

import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import wiremock.org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

class SqsDomainEventListenerTest {
  private val videoLinkBookingService: VideoLinkBookingService = mock()

  @Test
  fun `should call delete appointments when event type is prison-offender-events prisoner released`() {
    val domainListener = SqsDomainEventListener(videoLinkBookingService, Gson())
    domainListener.handleDomainEvents(getJson("/listeners/prison-offender-events.prisoner.released.json"))
    verify(videoLinkBookingService).deleteAppointments(
      ReleasedOffenderEventMessage(
        occurredAt = "2023-11-20T17:07:58Z",
        additionalInformation = AdditionalInformation(
          prisonId = "SWI",
          nomsNumber = "A7215DZ",
          reason = Reason.RELEASED,
        ),
      ),
    )
  }

  @Test
  fun `should not call delete appointments when event type is not released`() {
    val domainListener = SqsDomainEventListener(videoLinkBookingService, Gson())
    domainListener.handleDomainEvents(getJson("/listeners/prison-offender-events.prisoner.unknown.json"))
    verify(videoLinkBookingService, never()).deleteAppointments(any())
  }

  private fun getJson(filename: String): String {
    return IOUtils.toString(javaClass.getResourceAsStream(filename), StandardCharsets.UTF_8.toString())
  }
}
