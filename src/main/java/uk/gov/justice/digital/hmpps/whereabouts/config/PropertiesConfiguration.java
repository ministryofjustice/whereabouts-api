package uk.gov.justice.digital.hmpps.whereabouts.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class PropertiesConfiguration {

    final private Resource[] resources;

    public PropertiesConfiguration(@Value("classpath:whereabouts/patterns/*.properties") Resource[] resources) {
        this.resources = resources;
    }

    @Bean
    @Qualifier("whereaboutsGroups")
    public PropertiesFactoryBean pfb() {
        final var pfb = new PropertiesFactoryBean();
        pfb.setLocations(resources);
        return pfb;
    }
}
