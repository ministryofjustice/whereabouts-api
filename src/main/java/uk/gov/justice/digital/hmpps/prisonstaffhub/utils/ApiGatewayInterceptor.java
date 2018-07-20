package uk.gov.justice.digital.hmpps.prisonstaffhub.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Copies an existing Auth token from user context to elite-authorization and adds the gateway Jwt
 */
public class ApiGatewayInterceptor implements ClientHttpRequestInterceptor {

    private final ApiGatewayTokenGenerator apiGatewayTokenGenerator;

    public ApiGatewayInterceptor(ApiGatewayTokenGenerator apiGatewayTokenGenerator) {
        this.apiGatewayTokenGenerator = apiGatewayTokenGenerator;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        HttpHeaders headers = request.getHeaders();
        headers.add("elite-authorization", UserContext.getAuthToken());
        try {
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer "+ apiGatewayTokenGenerator.createGatewayToken());
        } catch (Exception e) {
            throw new IOException(e);
        }

        return execution.execute(request, body);
    }
}