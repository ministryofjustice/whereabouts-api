package uk.gov.justice.digital.hmpps.whereabouts
/*
This runs, but
* I can't work out how to get the gradle build to work with two main classes.
* The configuration pollutes the main app configuration and breaks the tests. (needs moving elsewhere)

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkAppointmentLinker

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableWebSecurity
@ComponentScan(basePackageClasses = [VideoLinkBookingRepository::class])
class AppointmentLinkerApplication(@Value("\${elite2.api.uri.root}") private val prisonApiRootUri: String) {

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ReactiveClientRegistrationRepository?,
    oAuth2AuthorizedClientService: ReactiveOAuth2AuthorizedClientService?
  ): ReactiveOAuth2AuthorizedClientManager {
    val authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager = AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }

  @Bean
  fun elite2WebClient(clientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    val filter = ServerOAuth2AuthorizedClientExchangeFilterFunction(clientManager)
    filter.setDefaultClientRegistrationId("elite2-api")
    return WebClient
      .builder()
      .baseUrl(prisonApiRootUri)
      .filter(filter)
      .build()
  }

  @Bean
  fun videoLinkAppointmentLinker(
    videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
    videoLinkBookingRepository: VideoLinkBookingRepository,
    prisonApiService: PrisonApiService
  ) = VideoLinkAppointmentLinker(videoLinkAppointmentRepository, videoLinkBookingRepository, prisonApiService)

  @Bean
  fun prisonApiService(webClient: WebClient) = PrisonApiService(webClient)

  @Bean
  fun runLinker(linker: VideoLinkAppointmentLinker) = CommandLineRunner {
    println("Command line")
    linker.linkAppointments()
  }
}

fun main(args: Array<String>) {
  runApplication<AppointmentLinkerApplication>(*args) {
   webApplicationType = WebApplicationType.NONE
  }.close()
}
*/
