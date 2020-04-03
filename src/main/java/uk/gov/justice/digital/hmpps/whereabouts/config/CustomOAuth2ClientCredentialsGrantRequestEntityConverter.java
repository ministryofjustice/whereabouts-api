package uk.gov.justice.digital.hmpps.whereabouts.config;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;

import java.util.Objects;

public class CustomOAuth2ClientCredentialsGrantRequestEntityConverter extends OAuth2ClientCredentialsGrantRequestEntityConverter {

    public RequestEntity<?> enhanceWithUsername(final OAuth2ClientCredentialsGrantRequest grantRequest, final String username) {
        final var request = super.convert(grantRequest);
        final var body = Objects.requireNonNull(request).getBody();

        if(body instanceof MultiValueMap) {
            final var headers = request.getHeaders();
            final var formParameters = (MultiValueMap<String, Object>) body;

            formParameters.add("username", username);

            return new RequestEntity<>(formParameters, headers, HttpMethod.POST, request.getUrl());
        }

        return request;
    }
}
