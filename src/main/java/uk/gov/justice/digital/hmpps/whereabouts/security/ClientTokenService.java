package uk.gov.justice.digital.hmpps.whereabouts.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;


@Service
public class ClientTokenService {

    private final String clientId;
    private final String clientSecret;
    private final RestTemplate restTemplate;
    private final AuthenticationFacade authenticationFacade;

    public ClientTokenService(@Value("${oauth-client-id}") final String clientId,
                              @Value("${oauth-client-secret}") final String clientSecret,
                              @Qualifier("oauthRestTemplate") final RestTemplate restTemplate,
                              final AuthenticationFacade authenticationFacade) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restTemplate = restTemplate;
        this.authenticationFacade = authenticationFacade;
    }

    public String getAccessToken() {
        final var httpHeaders = new HttpHeaders();
        final var token = Base64.getEncoder().encodeToString(String.format("%s:%s", clientId, clientSecret).getBytes());

        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");
        httpHeaders.add("accept", "application/json");
        httpHeaders.add("authorization", "Basic " + token);

        final var httpEntity =  new HttpEntity(null, httpHeaders);

        final var url = "/oauth/token?grant_type=client_credentials&username=" + authenticationFacade.getCurrentUsername();

        final var response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                new ParameterizedTypeReference<Map<String,Object>>() {});

        return Objects.requireNonNull(response.getBody()).get("access_token").toString();
    }
}
