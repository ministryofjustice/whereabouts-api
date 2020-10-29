package uk.gov.justice.digital.hmpps.whereabouts.controllers

import org.springframework.context.annotation.Bean
import uk.gov.justice.digital.hmpps.whereabouts.security.UserSecurityUtils

class StubUserSecurityUtilsConfig {
  @Bean
  fun getUserSecurityUtils(): UserSecurityUtils = UserSecurityUtils()
}
