package uk.gov.justice.digital.hmpps.whereabouts.services

import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SqsDomainEventListener() {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @SqsListener("domainevent", factory = "hmppsQueueContainerFactoryProxy")
  fun handleDomainEvents(requestJson: String?) {
    log.info("Raw domain event message: {}", requestJson)
  }
}
