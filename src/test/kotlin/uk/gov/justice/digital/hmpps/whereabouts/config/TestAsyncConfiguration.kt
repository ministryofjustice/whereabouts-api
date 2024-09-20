package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.core.task.SyncTaskExecutor
import java.util.concurrent.Executor

@Profile("test")
@TestConfiguration
class TestAsyncConfiguration {

  @Bean
  fun asyncExecutor(): Executor? = SyncTaskExecutor()
}
