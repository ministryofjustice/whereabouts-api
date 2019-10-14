package uk.gov.justice.digital.hmpps.whereabouts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade;
import uk.gov.justice.digital.hmpps.whereabouts.utils.JwtAuthInterceptor;

@Configuration
public class RestTemplateConfiguration {

    @Value("${elite2api.endpoint.url}")
    private String elite2UriRoot;

    @Value("${oauth.endpoint.url}")
    private String oauthRootUri;

    @Value("${casenotes.endpoint.url}")
    private String caseNotesRootUri;

    private final OAuth2ClientContext oauth2ClientContext;
    private final ClientCredentialsResourceDetails elite2apiDetails;

    public RestTemplateConfiguration(final OAuth2ClientContext oauth2ClientContext,
                                     final ClientCredentialsResourceDetails elite2apiDetails) {
        this.oauth2ClientContext = oauth2ClientContext;
        this.elite2apiDetails = elite2apiDetails;
    }

    @Bean(name = "elite2ApiHealthRestTemplate")
    public RestTemplate elite2ApiHealthRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return getRestTemplate(restTemplateBuilder, elite2UriRoot);
    }

    @Bean(name = "oauthApiRestTemplate")
    public RestTemplate oauthRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return getRestTemplate(restTemplateBuilder, oauthRootUri);
    }

    @Bean(name = "caseNotesApiHealthRestTemplate")
    public RestTemplate caseNotesHealthRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return getRestTemplate(restTemplateBuilder, caseNotesRootUri);
    }

    private RestTemplate getRestTemplate(final RestTemplateBuilder restTemplateBuilder, final String uri) {
        return restTemplateBuilder
                .rootUri(uri)
                .additionalInterceptors(new JwtAuthInterceptor())
                .build();
    }

    @Bean(name = "elite2ApiRestTemplate")
    public OAuth2RestTemplate elite2ApiRestTemplate(final AuthenticationFacade authenticationFacade) {

        final var elite2SystemRestTemplate = new OAuth2RestTemplate(elite2apiDetails, oauth2ClientContext);

        elite2SystemRestTemplate.setAccessTokenProvider(new GatewayAwareAccessTokenProvider(authenticationFacade));

        RootUriTemplateHandler.addTo(elite2SystemRestTemplate, elite2UriRoot + "/api");

        return elite2SystemRestTemplate;
    }

    @Bean(name = "caseNotesApiRestTemplate")
    public OAuth2RestTemplate caseNotesApiRestTemplate(final AuthenticationFacade authenticationFacade) {

        final var caseNotesApiRestTemplate = new OAuth2RestTemplate(elite2apiDetails, oauth2ClientContext);

        caseNotesApiRestTemplate.setAccessTokenProvider(new GatewayAwareAccessTokenProvider(authenticationFacade));

        RootUriTemplateHandler.addTo(caseNotesApiRestTemplate, caseNotesRootUri);

        return caseNotesApiRestTemplate;
    }

    public static class GatewayAwareAccessTokenProvider extends ClientCredentialsAccessTokenProvider {

        GatewayAwareAccessTokenProvider(final AuthenticationFacade authenticationFacade) {
            this.setTokenRequestEnhancer(new TokenRequestEnhancer(authenticationFacade));
        }

        @Override
        public RestOperations getRestTemplate() {
            return super.getRestTemplate();
        }
    }

    public static class TokenRequestEnhancer implements RequestEnhancer {

        private final AuthenticationFacade authenticationFacade;

        TokenRequestEnhancer(final AuthenticationFacade authenticationFacade) {
            this.authenticationFacade = authenticationFacade;
        }

        @Override
        public void enhance(final AccessTokenRequest request, final OAuth2ProtectedResourceDetails resource, final MultiValueMap<String, String> form, final HttpHeaders headers) {
            form.set("username", authenticationFacade.getCurrentUsername());
        }
    }
}
