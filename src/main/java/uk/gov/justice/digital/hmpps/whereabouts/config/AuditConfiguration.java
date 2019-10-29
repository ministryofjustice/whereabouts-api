package uk.gov.justice.digital.hmpps.whereabouts.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditConfiguration {

    @Service(value = "auditorAware")
    public class AuditorAwareImpl implements AuditorAware<String> {
        private final AuthenticationFacade authenticationFacade;

        public AuditorAwareImpl(final AuthenticationFacade authenticationFacade) {
            this.authenticationFacade = authenticationFacade;
        }

        @Override
        public Optional<String> getCurrentAuditor() {
            return Optional.ofNullable(authenticationFacade.getCurrentUsername());
        }
    }
}
