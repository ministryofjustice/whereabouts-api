package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfiguration {

  @Bean
  fun asyncExecutor(): Executor? = ThreadPoolTaskExecutor().apply {
    corePoolSize = 2
    maxPoolSize = 2
    queueCapacity = 10
    threadNamePrefix = "AsyncThread-"
    initialize()
  }
}
