package uk.gov.justice.digital.hmpps.whereabouts.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserContext;


@Configuration
public class WebClientConfiguration {

    private String prisonApiRootUri;
    private String prisonApiHealthRootUri;
    private String caseNotesRootUri;
    private String oauthRootUri;

    public WebClientConfiguration(
            @Value("${elite2.api.uri.root}") final String prisonApiRootUri,
            @Value("${elite2api.endpoint.url}") final String prisonApiHealthRootUri,
            @Value("${casenotes.endpoint.url}") final String caseNotesRootUri,
            @Value("${oauth.endpoint.url}") final String oauthRootUri) {

        this.prisonApiRootUri = prisonApiRootUri;
        this.prisonApiHealthRootUri = prisonApiHealthRootUri;
        this.caseNotesRootUri = caseNotesRootUri;
        this.oauthRootUri = oauthRootUri;
    }

    @Bean
    public WebClient prisonApiHealthWebClient(final WebClient.Builder builder) {
        return builder.baseUrl(prisonApiHealthRootUri)
                .filter(addAuthHeaderFilterFunction())
                .build();
    }

    @Bean
    public WebClient caseNoteHealthWebClient(final WebClient.Builder builder) {
        return builder
                .baseUrl(caseNotesRootUri)
                .filter(addAuthHeaderFilterFunction())
                .build();
    }


    @Bean
    public WebClient oAuthHealthWebClient(final WebClient.Builder builder) {
        return builder
                .baseUrl(oauthRootUri)
                .filter(addAuthHeaderFilterFunction())
                .build();
    }

    @NotNull
    private ExchangeFilterFunction addAuthHeaderFilterFunction() {
        return (request, next) -> {
            ClientRequest filtered = ClientRequest.from(request)
                    .header(HttpHeaders.AUTHORIZATION, UserContext.getAuthToken())
                    .build();
            return next.exchange(filtered);
        };
    }

    @Bean
    @RequestScope
    public WebClient elite2WebClient(final ClientRegistrationRepository clientRegistrationRepository,
                                     final OAuth2AuthorizedClientRepository authorizedClientRepository,
                                     final WebClient.Builder builder) {
        return getOAuthWebClient(authorizedClientManager(clientRegistrationRepository, authorizedClientRepository), builder, prisonApiRootUri);
    }

    @Bean
    @RequestScope
    public WebClient caseNoteWebClient(final ClientRegistrationRepository clientRegistrationRepository,
                                       final OAuth2AuthorizedClientRepository authorizedClientRepository,
                                       final WebClient.Builder builder) {

        return getOAuthWebClient(authorizedClientManager(clientRegistrationRepository, authorizedClientRepository), builder, caseNotesRootUri);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManagerAppScope(final ClientRegistrationRepository clientRegistrationRepository,
                                                                         final OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {

        final var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();
        final var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    @Bean
    public WebClient elite2WebClientAppScope(@Qualifier(value = "authorizedClientManagerAppScope") final OAuth2AuthorizedClientManager authorizedClientManager, final WebClient.Builder builder) {
         return getOAuthWebClient(authorizedClientManager, builder, prisonApiRootUri);
    }

    @Bean
    public WebClient caseNoteWebClientAppScope(@Qualifier(value = "authorizedClientManagerAppScope") final OAuth2AuthorizedClientManager authorizedClientManager, final WebClient.Builder builder) {
        return getOAuthWebClient(authorizedClientManager, builder, caseNotesRootUri);
    }

    private WebClient getOAuthWebClient(final OAuth2AuthorizedClientManager authorizedClientManager, final WebClient.Builder builder, final String rootUri) {
        final var oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId("elite2-api");

        return builder.baseUrl(rootUri)
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }

    private OAuth2AuthorizedClientManager authorizedClientManager(final ClientRegistrationRepository clientRegistrationRepository,
                                                                  final OAuth2AuthorizedClientRepository authorizedClientRepository) {

        final var defaultClientCredentialsTokenResponseClient = new DefaultClientCredentialsTokenResponseClient();
        final var authentication = UserContext.getAuthentication();

        defaultClientCredentialsTokenResponseClient.setRequestEntityConverter(grantRequest -> {
            final var converter = new CustomOAuth2ClientCredentialsGrantRequestEntityConverter();
            final var username = authentication.getName();
            return converter.enhanceWithUsername(grantRequest, username);
        });

        final var authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials(clientCredentialsGrantBuilder -> clientCredentialsGrantBuilder.accessTokenResponseClient(defaultClientCredentialsTokenResponseClient))
                        .build();

        final var authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }
}
