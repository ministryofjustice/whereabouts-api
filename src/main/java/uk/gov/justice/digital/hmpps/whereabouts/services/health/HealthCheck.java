package uk.gov.justice.digital.hmpps.whereabouts.services.health;


import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


public abstract class HealthCheck implements HealthIndicator {
    private final RestTemplate restTemplate;

    protected HealthCheck(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        try {
            final var responseEntity = this.restTemplate.getForEntity("/ping", String.class);
            return health(responseEntity.getStatusCode());
        } catch (final RestClientException e) {
            return health(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private Health health(final HttpStatus code) {
        return health (
                Health.up(),
                code);
    }

    private Health health(final Health.Builder builder, final HttpStatus code) {
        return builder
                .withDetail("HttpStatus", code.value())
                .build();
    }
}
