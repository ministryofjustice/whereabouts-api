package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.data.domain.AuditorAware
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.util.Optional

class AuditorAwareImpl(private val authenticationFacade: AuthenticationFacade) : AuditorAware<String> {
  override fun getCurrentAuditor(): Optional<String> = Optional.ofNullable(authenticationFacade.currentUsername)
}
