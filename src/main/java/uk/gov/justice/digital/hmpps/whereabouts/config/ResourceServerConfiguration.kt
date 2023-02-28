package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthAwareTokenConverter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class ResourceServerConfiguration {
  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
    http
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and().csrf().disable()
      .authorizeHttpRequests { auth ->
        auth.requestMatchers(
          "/webjars/**", "/favicon.ico", "/csrf",
          "/health/**", "/info", "/ping", "/h2-console/**",
          "/v3/api-docs/**", "/swagger-ui.html",
          "/swagger-ui/**", "/swagger-resources", "/swagger-resources/configuration/ui",
          "/swagger-resources/configuration/security", "/queue-admin/retry-all-dlqs"
        )
          .permitAll().anyRequest().authenticated()
      }
      .also { it.oauth2ResourceServer().jwt().jwtAuthenticationConverter(AuthAwareTokenConverter()) }
      .build()
}
