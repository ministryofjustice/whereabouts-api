package uk.gov.justice.digital.hmpps.whereabouts.config

@Configuration
class VersionOutputter(buildProperties: BuildProperties) {
  private val version = buildProperties.version

  @EventListener(ApplicationReadyEvent::class)
  fun logVersionOnStartup() {
    log.info("Version {} started", version)
  }

  @Bean
  fun versionContextInitializer() = ContextInitializer { it.component.setVersion(version) }

  companion object {
    private val log = LoggerFactory.getLogger(VersionOutputter::class.java)
  }
}
