package uk.gov.justice.digital.hmpps.whereabouts.config;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

public class CustomOAuth2ClientCredentialsGrantRequestEntityConverter extends OAuth2ClientCredentialsGrantRequestEntityConverter {

    public RequestEntity<?> enhanceWithUsername(final OAuth2ClientCredentialsGrantRequest grantRequest, final String username) {
        final var request = super.convert(grantRequest);

        final var formParameters = Objects.requireNonNull(request).getBody();
        final var headers = request.getHeaders();
        final var uri = UriComponentsBuilder.fromUri(request.getUrl())
                .queryParam("username", username)
                .build()
                .toUri();

        return new RequestEntity<>(formParameters, headers, HttpMethod.POST, uri);
    }
}
