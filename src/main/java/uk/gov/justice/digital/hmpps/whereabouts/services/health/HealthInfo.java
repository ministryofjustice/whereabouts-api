package uk.gov.justice.digital.hmpps.whereabouts.services.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

/**
 * Adds version data to the /health endpoint. This is called by the UI to display API details
 */
@Component
@Slf4j
public class HealthInfo implements HealthIndicator {

    private final BuildProperties buildProperties;

    public HealthInfo(@Autowired(required = false) final BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public Health health() {
        final var status = Health.up().withDetail("version", getVersion()).build();
        log.info(status.toString());
        return status;
    }

    private String getVersion() {
        return buildProperties == null ? "version not available" : buildProperties.getVersion();
    }

}
