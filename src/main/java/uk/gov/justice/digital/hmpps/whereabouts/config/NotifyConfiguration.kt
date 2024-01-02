package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.service.notify.NotificationClient

@Configuration
class NotifyConfiguration(@Value("\${notify.api.key}") private val apiKey: String) {
  @Bean
  fun notifyClient(): NotificationClient {
    return NotificationClient(apiKey)
  }
}
