package uk.gov.justice.digital.hmpps.whereabouts.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

public class PropertiesConfiguration {

    @Value("classpath:whereabouts/patterns/*.properties")
    private Resource[] resources;

    @Bean
    @Qualifier("whereaboutsGroups")
    public PropertiesFactoryBean pfb() {
        final var pfb = new PropertiesFactoryBean();
        pfb.setLocations(resources);
        return pfb;
    }
}
