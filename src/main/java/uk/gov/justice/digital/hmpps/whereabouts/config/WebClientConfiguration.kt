package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import uk.gov.justice.hmpps.kotlin.auth.usernameAwareTokenRequestOAuth2AuthorizedClientManager

@Configuration
class WebClientConfiguration(
  @Value("\${elite2.api.uri.root}") private val prisonApiRootUri: String,
  @Value("\${elite2api.endpoint.url}") private val prisonApiHealthRootUri: String,
  @Value("\${casenotes.endpoint.url}") private val caseNotesRootUri: String,
  @Value("\${oauth.endpoint.url}") private val oauthRootUri: String,
) {

  @Bean
  fun prisonApiHealthWebClient(builder: WebClient.Builder) = builder.healthWebClient(prisonApiHealthRootUri)

  @Bean
  fun prisonAPiWebClientAuditable(authorizedClientManager: OAuth2AuthorizedClientManager, builder: WebClient.Builder) = builder
    .authorisedWebClient(authorizedClientManager, "elite2-api", prisonApiRootUri)

  @Bean
  fun caseNoteHealthWebClient(builder: WebClient.Builder) = builder.healthWebClient(caseNotesRootUri)

  @Bean
  fun oAuthHealthWebClient(builder: WebClient.Builder) = builder.healthWebClient(oauthRootUri)

  @Bean
  @RequestScope
  fun elite2WebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
    builder: WebClient.Builder,
  ) = builder
    .exchangeStrategies(
      ExchangeStrategies.builder()
        .codecs { configurer ->
          configurer.defaultCodecs()
            .maxInMemorySize(-1)
        }
        .build(),
    )
    .authorisedWebClient(
      usernameAwareTokenRequestOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService),
      "elite2-api",
      prisonApiRootUri,
    )

  @Bean
  @RequestScope
  fun caseNoteWebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
    builder: WebClient.Builder,
  ) = builder.authorisedWebClient(
    usernameAwareTokenRequestOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService),
    "case-note-api",
    caseNotesRootUri,
  )
}
