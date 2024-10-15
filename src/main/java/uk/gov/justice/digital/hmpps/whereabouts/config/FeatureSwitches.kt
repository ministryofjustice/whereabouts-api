package uk.gov.justice.digital.hmpps.whereabouts.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.whereabouts.services.court.events.OutboundEvent

/**
 * A centralised reusable component for determining whether an application feature is enabled or not.
 */
@Component
class FeatureSwitches(private val environment: Environment) {

  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun isEnabled(outboundEvent: OutboundEvent, defaultValue: Boolean = false): Boolean =
    get("feature.event.${outboundEvent.eventType}", Boolean::class.java, defaultValue)

  private inline fun <reified T> get(property: String, type: Class<T>, defaultValue: T) =
    environment.getProperty(property, type).let {
      if (it == null) {
        log.info("property '$property' not configured, defaulting to $defaultValue")
        defaultValue
      } else {
        it
      }
    }
}
