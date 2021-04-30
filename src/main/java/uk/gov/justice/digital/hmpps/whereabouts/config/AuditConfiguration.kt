package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade

@Configuration
@EnableJpaAuditing
class AuditConfiguration {
  @Bean
  fun auditorAware(authenticationFacade: AuthenticationFacade): AuditorAware<String> = AuditorAwareImpl(authenticationFacade)
}
