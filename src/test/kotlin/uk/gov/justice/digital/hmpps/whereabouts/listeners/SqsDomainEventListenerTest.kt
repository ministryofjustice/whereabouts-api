package uk.gov.justice.digital.hmpps.whereabouts.listeners

import com.google.gson.Gson
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import wiremock.org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

class SqsDomainEventListenerTest {
  private val videoLinkBookingService: VideoLinkBookingService = mock()
  private val bvlsEnabledListener = SqsDomainEventListener(videoLinkBookingService, Gson(), true)

  @Test
  fun `should call delete appointments when event type is prison-offender-events prisoner released and bvls enabled`() {
    bvlsEnabledListener.handleDomainEvents(getJson("/listeners/prison-offender-events.prisoner.released.json"))
    verify(videoLinkBookingService).deleteAppointmentWhenTransferredOrReleased(
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
  fun `should not call delete appointments when event type is not released and bvls enabled`() {
    bvlsEnabledListener.handleDomainEvents(getJson("/listeners/prison-offender-events.prisoner.unknown.json"))
    verify(videoLinkBookingService, never()).deleteAppointmentWhenTransferredOrReleased(any())
  }

  @Test
  fun `should throw exception if upstream service fails`() {
    whenever(videoLinkBookingService.deleteAppointmentWhenTransferredOrReleased(any())).thenThrow(
      EntityNotFoundException(),
    )

    assertThrows(EntityNotFoundException::class.java) {
      bvlsEnabledListener.handleDomainEvents(getJson("/listeners/prison-offender-events.prisoner.released.json"))
    }
  }

  @Test
  fun `should ignore events when bvls not enabled`() {
    SqsDomainEventListener(videoLinkBookingService, Gson(), false)
      .handleDomainEvents(getJson("/listeners/prison-offender-events.prisoner.released.json"))

    verifyNoInteractions(videoLinkBookingService)
  }

  private fun getJson(filename: String): String {
    return IOUtils.toString(javaClass.getResourceAsStream(filename), StandardCharsets.UTF_8.toString())
  }
}
