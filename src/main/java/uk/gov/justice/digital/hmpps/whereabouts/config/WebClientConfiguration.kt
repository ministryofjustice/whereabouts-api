package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserContext

@Configuration
class WebClientConfiguration(
  @Value("\${elite2.api.uri.root}") private val prisonApiRootUri: String,
  @Value("\${elite2api.endpoint.url}") private val prisonApiHealthRootUri: String,
  @Value("\${prisonregister.endpoint.url}") private val prisonRegisterRootUri: String,
  @Value("\${locationapi.endpoint.url}") private val locationApiRootUri: String,
  @Value("\${casenotes.endpoint.url}") private val caseNotesRootUri: String,
  @Value("\${oauth.endpoint.url}") private val oauthRootUri: String,
) {

  @Bean
  fun prisonRegisterWebClient(builder: WebClient.Builder): WebClient {
    return builder.baseUrl(prisonRegisterRootUri)
      .filter(addAuthHeaderFilterFunction())
      .build()
  }

  @Bean
  fun prisonRegisterClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, prisonRegisterRootUri)
  }

  @Bean
  fun locationApiWebClient(builder: WebClient.Builder): WebClient {
    return builder.baseUrl(locationApiRootUri)
      .filter(addAuthHeaderFilterFunction())
      .build()
  }

  @Bean
  fun locationApiWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, locationApiRootUri)
  }

  @Bean
  fun prisonApiHealthWebClient(builder: WebClient.Builder): WebClient {
    return builder.baseUrl(prisonApiHealthRootUri)
      .filter(addAuthHeaderFilterFunction())
      .build()
  }

  @Bean
  fun prisonAPiWebClientAuditable(builder: WebClient.Builder): WebClient {
    return builder
      .baseUrl(prisonApiRootUri)
      .filter(addAuthHeaderFilterFunction())
      .build()
  }

  @Bean
  fun caseNoteHealthWebClient(builder: WebClient.Builder): WebClient {
    return builder
      .baseUrl(caseNotesRootUri)
      .filter(addAuthHeaderFilterFunction())
      .build()
  }

  @Bean
  fun oAuthHealthWebClient(builder: WebClient.Builder): WebClient {
    return builder
      .baseUrl(oauthRootUri)
      .filter(addAuthHeaderFilterFunction())
      .build()
  }

  private fun addAuthHeaderFilterFunction(): ExchangeFilterFunction {
    return ExchangeFilterFunction { request: ClientRequest?, next: ExchangeFunction ->
      val filtered = ClientRequest.from(request)
        .header(HttpHeaders.AUTHORIZATION, UserContext.getAuthToken())
        .build()
      next.exchange(filtered)
    }
  }

  @Bean
  @RequestScope
  fun elite2WebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientRepository: OAuth2AuthorizedClientRepository,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(
      authorizedClientManager(clientRegistrationRepository, authorizedClientRepository),
      builder,
      prisonApiRootUri,
    )
  }

  @Bean
  @RequestScope
  fun caseNoteWebClient(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientRepository: OAuth2AuthorizedClientRepository,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(
      authorizedClientManager(clientRegistrationRepository, authorizedClientRepository),
      builder,
      caseNotesRootUri,
    )
  }

  @Bean
  fun authorizedClientManagerAppScope(
    clientRegistrationRepository: ClientRegistrationRepository?,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService?,
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager =
      AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }

  @Bean
  fun elite2WebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, prisonApiRootUri)
  }

  @Bean
  fun caseNoteWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, caseNotesRootUri)
  }

  private fun getOAuthWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
    rootUri: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("elite2-api")
    return builder.baseUrl(rootUri)
      .apply(oauth2Client.oauth2Configuration())
      .build()
  }

  private fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientRepository: OAuth2AuthorizedClientRepository,
  ): OAuth2AuthorizedClientManager {
    val defaultClientCredentialsTokenResponseClient = DefaultClientCredentialsTokenResponseClient()
    val authentication = UserContext.getAuthentication()

    defaultClientCredentialsTokenResponseClient.setRequestEntityConverter { grantRequest: OAuth2ClientCredentialsGrantRequest? ->
      val converter = CustomOAuth2ClientCredentialsGrantRequestEntityConverter()
      val username = authentication.name
      converter.enhanceWithUsername(grantRequest, username)
    }

    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials { clientCredentialsGrantBuilder: OAuth2AuthorizedClientProviderBuilder.ClientCredentialsGrantBuilder ->
        clientCredentialsGrantBuilder.accessTokenResponseClient(
          defaultClientCredentialsTokenResponseClient,
        )
      }
      .build()
    val authorizedClientManager =
      DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }
}
