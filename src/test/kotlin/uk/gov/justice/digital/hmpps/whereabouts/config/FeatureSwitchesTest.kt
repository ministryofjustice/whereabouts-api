package uk.gov.justice.digital.hmpps.whereabouts.config

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.whereabouts.listeners.SqsDomainEventListener
import uk.gov.justice.digital.hmpps.whereabouts.listeners.SqsOffenderEventListener
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.OutboundEvent
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.OutboundEventsPublisher
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.OutboundEventsService
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class FeatureSwitchesTest {

  // Beans are mocked out so as not to interfere with the running of the tests. We do not want/need them on the context.
  @MockBean
  private lateinit var hmppsQueueService: HmppsQueueService

  @MockBean
  private lateinit var sqsDomainEventListener: SqsDomainEventListener

  @MockBean
  private lateinit var sqsOffenderEventListener: SqsOffenderEventListener

  @MockBean
  private lateinit var outboundEventsService: OutboundEventsService

  @MockBean
  private lateinit var outboundEventsPublisher: OutboundEventsPublisher

  @TestPropertySource(properties = ["FEATURE.EVENT.WHEREABOUTS-API.VIDEOLINK.MIGRATE=true"])
  @Nested
  @DisplayName("Features are enabled when set")
  inner class EnabledFeatures(@Autowired val featureSwitches: FeatureSwitches) {
    @Test
    fun `features are enabled`() {
      assertThat(featureSwitches.isEnabled(OutboundEvent.VIDEO_LINK_BOOKING_MIGRATE)).isTrue
    }
  }

  @TestPropertySource(properties = ["FEATURE.EVENT.WHEREABOUTS-API.VIDEOLINK.MIGRATE=false"])
  @Nested
  @DisplayName("Features are enabled when set")
  inner class DisabledFeatures(@Autowired val featureSwitches: FeatureSwitches) {
    @Test
    fun `features are disabled`() {
      assertThat(featureSwitches.isEnabled(OutboundEvent.VIDEO_LINK_BOOKING_MIGRATE)).isFalse
    }
  }
}
