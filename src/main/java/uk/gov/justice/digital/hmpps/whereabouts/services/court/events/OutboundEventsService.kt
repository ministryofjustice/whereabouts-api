package uk.gov.justice.digital.hmpps.whereabouts.services.court.events

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.config.FeatureSwitches

@Service
class OutboundEventsService(
  private val publisher: OutboundEventsPublisher,
  private val featureSwitches: FeatureSwitches,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun send(outboundEvent: OutboundEvent, identifier: Long) {
    if (featureSwitches.isEnabled(outboundEvent)) {
      log.info("Sending outbound event $outboundEvent for identifier $identifier")
      when (outboundEvent) {
        OutboundEvent.VIDEO_LINK_BOOKING_MIGRATE -> {
          publisher.send(outboundEvent.event(VideoLinkBookingMigrate(identifier)))
        }
      }
    } else {
      log.warn("Outbound event type $outboundEvent feature is configured off.")
    }
  }
}
