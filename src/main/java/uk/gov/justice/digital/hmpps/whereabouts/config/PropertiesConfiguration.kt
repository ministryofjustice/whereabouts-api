package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PropertiesLoaderUtils
import java.io.IOException

@Configuration
open class PropertiesConfiguration(
  @Value("classpath:whereabouts/patterns/*.properties") private val resources: Array<Resource>,
  @Value("classpath:whereabouts/enabled.properties") private val enabled: Resource
) {

  @Bean
  @Qualifier("whereaboutsGroups")
  open fun pfb(): PropertiesFactoryBean {
    val pfb = PropertiesFactoryBean()
    pfb.setLocations(*resources)
    return pfb
  }

  @Bean
  @Qualifier("whereaboutsEnabled")
  @Throws(IOException::class)
  open fun enabled(): Set<String> {
    return PropertiesLoaderUtils.loadProperties(enabled).stringPropertyNames()
  }
}
