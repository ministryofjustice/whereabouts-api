package uk.gov.justice.digital.hmpps.whereabouts.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import uk.gov.justice.digital.hmpps.whereabouts.security.ClientTokenService;

import java.io.IOException;

public class JwtClientAuthInterceptor implements ClientHttpRequestInterceptor {


    private final ClientTokenService clientTokenService;

    public JwtClientAuthInterceptor(final ClientTokenService clientTokenService) {
        this.clientTokenService = clientTokenService;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        final var headers = request.getHeaders();
        final var bearerToken = "Bearer " + clientTokenService.getAccessToken();

        headers.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return execution.execute(request, body);
    }
}
