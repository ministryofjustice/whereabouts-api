package uk.gov.justice.digital.hmpps.whereabouts.listeners

import com.google.gson.Gson
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService

@Service
class SqsDomainEventListener(
  @Qualifier("videoLinkBookingServiceAppScope")
  private val videoLinkBookingService: VideoLinkBookingService,
  private val gson: Gson,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @SqsListener("domainevent", factory = "hmppsQueueContainerFactoryProxy")
  fun handleDomainEvents(requestJson: String?) {
    log.info("Raw domain event message: {}", requestJson)
    val (message, messageAttributes) = gson.fromJson(requestJson, Message::class.java)
    val eventType = messageAttributes.eventType.value
    log.info("Processing message of type {}", eventType)

    when (eventType) {
      "prison-offender-events.prisoner.released" -> {
        val transferEventMessage = gson.fromJson(message, ReleasedOffenderEventMessage::class.java)
        videoLinkBookingService.deleteAppointments(transferEventMessage)
      }
    }
  }
}
