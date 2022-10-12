package uk.gov.justice.digital.hmpps.whereabouts.services

import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService

@Service
@ConditionalOnProperty("sqs.provider")
class EventListener(
  @Qualifier("attendanceServiceAppScope")
  private val attendanceService: AttendanceService,
  private val videoLinkBookingService: VideoLinkBookingService,
  private val gson: Gson
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @JmsListener(destination = "\${sqs.queue.name}")
  fun handleEvents(requestJson: String?) {
    val (Message, MessageAttributes) = gson.fromJson<Message>(requestJson, Message::class.java)
    val eventType = MessageAttributes.eventType.Value
    log.info("Processing message of type {}", eventType)

    when (eventType) {
      "DATA_COMPLIANCE_DELETE-OFFENDER" -> {
        val (offenderIdDisplay, offenders) = gson.fromJson(Message, DeleteOffenderEventMessage::class.java)
        val bookingIds = offenders.flatMap { offender -> offender.bookings.map { it.offenderBookId } }

        attendanceService.deleteAttendancesForOffenderDeleteEvent(
          offenderIdDisplay,
          bookingIds
        )
      }
      "APPOINTMENT_CHANGED" -> {
        val appointmentChangedEventMessage = gson.fromJson(Message, AppointmentChangedEventMessage::class.java)
        if (appointmentChangedEventMessage.recordDeleted) {
          videoLinkBookingService.deleteAppointments(appointmentChangedEventMessage.scheduleEventId)
        }
      }
    }
  }
}

data class Attribute(val Type: String, val Value: String)
data class MessageAttributes(val eventType: Attribute)
data class Booking(val offenderBookId: Long)
data class Offender(val offenderId: Long, val bookings: List<Booking>)
data class DeleteOffenderEventMessage(val offenderIdDisplay: String, val offenders: List<Offender>)
data class AppointmentChangedEventMessage(val bookingId: Long, val scheduleEventId: Long, val recordDeleted: Boolean)
data class Message(
  val Message: String,
  val MessageAttributes: MessageAttributes,
  val message: DeleteOffenderEventMessage
)
