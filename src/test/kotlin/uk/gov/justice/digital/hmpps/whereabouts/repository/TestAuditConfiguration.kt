package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.util.Optional

@TestConfiguration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
open class TestAuditConfiguration {
  @Service(value = "auditorAware")
  open class AuditorAwareImpl(val authenticationFacade: AuthenticationFacade) : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
      return Optional.ofNullable(authenticationFacade.currentUsername)
    }
  }
}
