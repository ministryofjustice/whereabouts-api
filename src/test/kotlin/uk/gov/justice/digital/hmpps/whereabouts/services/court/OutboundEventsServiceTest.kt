package uk.gov.justice.digital.hmpps.whereabouts.services.court

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.justice.digital.hmpps.whereabouts.config.FeatureSwitches
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.AdditionalInformation
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.OutboundEvent
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.OutboundEventsPublisher
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.OutboundEventsService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.OutboundHMPPSDomainEvent
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.VideoLinkBookingMigrate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * These tests are only required for Video link booking migration from whereabouts API
 * into the new hmpps-book-a-video-link service.  These can be removed later.
 */
class OutboundEventsServiceTest {

  private val eventsPublisher: OutboundEventsPublisher = mock()
  private val featureSwitches: FeatureSwitches = mock()
  private val outboundEventsService = OutboundEventsService(eventsPublisher, featureSwitches)
  private val eventCaptor = argumentCaptor<OutboundHMPPSDomainEvent>()

  @Test
  fun `video link booking migration event is published with correct content`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.VIDEO_LINK_BOOKING_MIGRATE) } doReturn true }

    outboundEventsService.send(OutboundEvent.VIDEO_LINK_BOOKING_MIGRATE, 1L)

    verify(
      expectedEventType = "whereabouts-api.videolink.migrate",
      expectedAdditionalInformation = VideoLinkBookingMigrate(1),
      expectedDescription = "A video link booking has been identified for migration",
    )
  }

  @Test
  fun `video link booking migrate event is feature-switched off - no event sent`() {
    featureSwitches.stub { on { isEnabled(OutboundEvent.VIDEO_LINK_BOOKING_MIGRATE) } doReturn false }

    outboundEventsService.send(OutboundEvent.VIDEO_LINK_BOOKING_MIGRATE, 1L)

    verifyNoInteractions(eventsPublisher)
  }

  private fun verify(
    expectedEventType: String,
    expectedAdditionalInformation: AdditionalInformation,
    expectedOccurredAt: LocalDateTime = LocalDateTime.now(),
    expectedDescription: String,
  ) {
    verify(eventsPublisher).send(eventCaptor.capture())

    with(eventCaptor.firstValue) {
      assertThat(eventType).isEqualTo(expectedEventType)
      assertThat(additionalInformation).isEqualTo(expectedAdditionalInformation)
      assertThat(occurredAt).isCloseTo(expectedOccurredAt, within(60, ChronoUnit.SECONDS))
      assertThat(description).isEqualTo(expectedDescription)
    }

    verifyNoMoreInteractions(eventsPublisher)
  }
}
