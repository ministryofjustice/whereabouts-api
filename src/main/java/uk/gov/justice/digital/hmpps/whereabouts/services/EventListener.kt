package uk.gov.justice.digital.hmpps.whereabouts.services

import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("sqs.provider")
class EventListener(@Qualifier("attendanceServiceAppScope") private val attendanceService: AttendanceService, private val gson: Gson) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "\${sqs.queue.name}")
  fun handleEvents(requestJson: String?) {
    val (Message, MessageAttributes) = gson.fromJson<Message>(requestJson, Message::class.java)
    val (offenderIdDisplay) = gson.fromJson(Message, EventMessage::class.java)

    val eventType = MessageAttributes.eventType.Value
    log.info("Processing message of type {}", eventType)

    when (eventType) {
      "DATA_COMPLIANCE_DELETE-OFFENDER" -> attendanceService.deleteAttendances(offenderIdDisplay)
    }
  }
}

data class Attribute(val Type: String, val Value: String)
data class MessageAttributes(val eventType: Attribute)
data class EventMessage(val offenderIdDisplay: String)
data class Message(val Message: String, val MessageAttributes: MessageAttributes, val message: EventMessage)
