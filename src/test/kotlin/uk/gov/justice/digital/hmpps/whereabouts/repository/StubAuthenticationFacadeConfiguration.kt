package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade

@TestConfiguration
open class StubAuthenticationFacadeConfiguration {
  @Bean
  open fun getAuthenticationFacade(): AuthenticationFacade {
    class StubAuthenticationFacade : AuthenticationFacade {
      override fun getCurrentUsername(): String {
        return "test"
      }
    }
    return StubAuthenticationFacade()
  }
}
