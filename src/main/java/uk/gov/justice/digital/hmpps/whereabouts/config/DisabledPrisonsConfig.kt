package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class DisabledPrisonsConfig(
  @Value("\${service.whereabouts-disabled}")
  val disabledPrisons: String,
) {
  fun getPrisons(): List<String> = disabledPrisons.split(",")
}
