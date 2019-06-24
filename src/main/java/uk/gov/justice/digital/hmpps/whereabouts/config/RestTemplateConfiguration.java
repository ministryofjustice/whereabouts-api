package uk.gov.justice.digital.hmpps.whereabouts.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.digital.hmpps.whereabouts.security.ClientTokenService;
import uk.gov.justice.digital.hmpps.whereabouts.utils.JwtAuthInterceptor;
import uk.gov.justice.digital.hmpps.whereabouts.utils.JwtClientAuthInterceptor;
import uk.gov.justice.digital.hmpps.whereabouts.utils.UserContextInterceptor;

import java.util.List;

@Configuration
public class RestTemplateConfiguration {

    @Value("${elite2.uri.root}")
    private String elite2UriRoot;

    @Value("${elite2.api.uri.root}")
    private String apiRootUri;

    @Value("${oauth.uri.root}")
    private String oauthUriRoot;

    @Autowired
    private ClientTokenService clientTokenService;

    @Bean(name = "oauthRestTemplate")
    public RestTemplate oauthRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(oauthUriRoot)
                .build();
    }

    @Bean(name = "elite2ApiRestTemplate")
    public RestTemplate elite2ApiRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(apiRootUri)
                .additionalInterceptors(List.of(
                        new UserContextInterceptor(),
                        new JwtClientAuthInterceptor(clientTokenService)
                )).build();
    }

    @Bean(name = "elite2ApiHealthRestTemplate")
    public RestTemplate elite2ApiHealthRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(elite2UriRoot)
                .additionalInterceptors(List.of(
                    new UserContextInterceptor(),
                    new JwtAuthInterceptor()
                )).build();
    }
}
