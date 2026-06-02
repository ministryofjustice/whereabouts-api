package uk.gov.justice.digital.hmpps.whereabouts.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version!!

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://whereabouts-api-dev.service.justice.gov.uk").description("Development"),
        Server().url("https://whereabouts-api-preprod.service.justice.gov.uk").description("Pre-Production"),
        Server().url("https://whereabouts-api.service.justice.gov.uk").description("Production"),
        Server().url("http://localhost:8082").description("Local"),
      ),
    )
    .tags(
      listOf(),
    )
    .info(
      Info()
        .title("HMPPS Whereabouts API")
        .version(version)
        .description("API for prisoner whereabouts")
        .contact(
          Contact()
            .name("HMPPS Digital Studio")
            .email("feedback@digital.justice.gov.uk"),
        ),
    )
    .addSecurityItem(SecurityRequirement().addList("bearer-jwt", listOf("read", "write")))
}
