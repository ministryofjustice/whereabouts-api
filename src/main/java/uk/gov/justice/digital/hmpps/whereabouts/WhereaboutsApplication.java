package uk.gov.justice.digital.hmpps.whereabouts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableResourceServer
public class WhereaboutsApplication {
    public static void main(final String[] args) {
        SpringApplication.run(WhereaboutsApplication.class, args);
    }
}
