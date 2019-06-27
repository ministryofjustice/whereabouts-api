package uk.gov.justice.digital.hmpps.whereabouts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade;
import uk.gov.justice.digital.hmpps.whereabouts.utils.JwtAuthInterceptor;
import uk.gov.justice.digital.hmpps.whereabouts.utils.W3cTracingInterceptor;

import java.util.List;

@Configuration
public class RestTemplateConfiguration {

    @Value("${elite2.uri.root}")
    private String elite2UriRoot;

    @Value("${elite2.api.uri.root}")
    private String apiRootUri;

    private final OAuth2ClientContext oauth2ClientContext;
    private final ClientCredentialsResourceDetails elite2apiDetails;

    public RestTemplateConfiguration(final OAuth2ClientContext oauth2ClientContext,
                                     final ClientCredentialsResourceDetails elite2apiDetails) {
        this.oauth2ClientContext = oauth2ClientContext;
        this.elite2apiDetails = elite2apiDetails;
    }

    @Bean(name = "elite2ApiRestTemplate")
    public RestTemplate elite2ApiRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return getRestTemplate(restTemplateBuilder, apiRootUri);
    }

    @Bean(name = "elite2ApiHealthRestTemplate")
    public RestTemplate elite2ApiHealthRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return getRestTemplate(restTemplateBuilder, elite2UriRoot);
    }

    private RestTemplate getRestTemplate(final RestTemplateBuilder restTemplateBuilder, final String uri) {
        return restTemplateBuilder
                .rootUri(uri)
                .additionalInterceptors(getRequestInterceptors())
                .build();
    }

    private List<ClientHttpRequestInterceptor> getRequestInterceptors() {
        return List.of(
                new W3cTracingInterceptor(),
                new JwtAuthInterceptor());
    }

    @Bean
    public OAuth2RestTemplate elite2SystemRestTemplate(GatewayAwareAccessTokenProvider accessTokenProvider) {

        OAuth2RestTemplate elite2SystemRestTemplate = new OAuth2RestTemplate(elite2apiDetails, oauth2ClientContext);
        List<ClientHttpRequestInterceptor> systemInterceptors = elite2SystemRestTemplate.getInterceptors();
        systemInterceptors.add(new W3cTracingInterceptor());

        elite2SystemRestTemplate.setAccessTokenProvider(accessTokenProvider);

        RootUriTemplateHandler.addTo(elite2SystemRestTemplate, this.apiRootUri);
        return elite2SystemRestTemplate;
    }

    /**
     * This subclass is necessary to make OAuth2AccessTokenSupport.getRestTemplate() public
     */
    @Component("accessTokenProvider")
    public class GatewayAwareAccessTokenProvider extends ClientCredentialsAccessTokenProvider {

        public GatewayAwareAccessTokenProvider(final AuthenticationFacade authenticationFacade) {
            this.setTokenRequestEnhancer(new TokenRequestEnhancer(authenticationFacade));
        }

        @Override
        public RestOperations getRestTemplate() {
            return super.getRestTemplate();
        }
    }

    public class TokenRequestEnhancer implements RequestEnhancer {

        private final AuthenticationFacade authenticationFacade;

        TokenRequestEnhancer(AuthenticationFacade authenticationFacade) {
            this.authenticationFacade = authenticationFacade;
        }

        @Override
        public void enhance(AccessTokenRequest request, OAuth2ProtectedResourceDetails resource, MultiValueMap<String, String> form, HttpHeaders headers) {
            form.set("username", authenticationFacade.getCurrentUsername());
        }
    }
}
