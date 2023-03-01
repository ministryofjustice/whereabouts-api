package uk.gov.justice.digital.hmpps.whereabouts.services

import com.google.gson.Gson
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService

@Service
class SqsEventListener(
  @Qualifier("attendanceServiceAppScope")
  private val attendanceService: AttendanceService,
  @Qualifier("videoLinkBookingServiceAppScope")
  private val videoLinkBookingService: VideoLinkBookingService,
  private val gson: Gson,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @SqsListener("whereabouts", factory = "hmppsQueueContainerFactoryProxy")
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
          bookingIds,
        )
      }
      "APPOINTMENT_CHANGED" -> {
        val appointmentChangedEventMessage = gson.fromJson(Message, AppointmentChangedEventMessage::class.java)
        videoLinkBookingService.processNomisUpdate(appointmentChangedEventMessage)
      }
    }
  }
}

data class Attribute(val Type: String, val Value: String)
data class MessageAttributes(val eventType: Attribute)
data class Booking(val offenderBookId: Long)
data class Offender(val offenderId: Long, val bookings: List<Booking>)
data class DeleteOffenderEventMessage(val offenderIdDisplay: String, val offenders: List<Offender>)
data class AppointmentChangedEventMessage(val bookingId: Long, val scheduleEventId: Long, val recordDeleted: Boolean, val agencyLocationId: String, val eventDatetime: String, val scheduledStartTime: String, val scheduledEndTime: String)

data class Message(
  val Message: String,
  val MessageAttributes: MessageAttributes,
  val message: DeleteOffenderEventMessage,
)
