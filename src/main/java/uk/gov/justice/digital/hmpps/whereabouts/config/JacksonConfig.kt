package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper

@Configuration
class JacksonConfig {
  @Bean
  fun customizeJackson(): JsonMapperBuilderCustomizer = JsonMapperBuilderCustomizer { builder: JsonMapper.Builder? ->
    builder!!.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
  }
}
