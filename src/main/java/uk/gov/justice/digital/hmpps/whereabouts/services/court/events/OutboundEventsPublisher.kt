package uk.gov.justice.digital.hmpps.whereabouts.services.court.events

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@Service
class OutboundEventsPublisher(
  private val hmppsQueueService: HmppsQueueService,
  private val mapper: ObjectMapper,
) {
  companion object {
    const val TOPIC_ID = "domainevents"
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  private val domainEventsTopic by lazy {
    hmppsQueueService.findByTopicId(TOPIC_ID) ?: throw RuntimeException("Topic with name $TOPIC_ID doesn't exist")
  }

  fun send(event: OutboundHMPPSDomainEvent) {
    domainEventsTopic.snsClient.publish(
      PublishRequest.builder()
        .topicArn(domainEventsTopic.arn)
        .message(mapper.writeValueAsString(event))
        .messageAttributes(metaData(event))
        .build(),
    ).also { log.debug("Published {}", event) }

    return
  }

  private fun metaData(payload: OutboundHMPPSDomainEvent) =
    mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue(payload.eventType).build())
}
