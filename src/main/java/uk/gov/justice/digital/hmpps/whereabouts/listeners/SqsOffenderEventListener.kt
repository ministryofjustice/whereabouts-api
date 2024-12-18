package uk.gov.justice.digital.hmpps.whereabouts.listeners

import com.google.gson.Gson
import io.awspring.cloud.sqs.annotation.SqsListener
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService

@Service
class SqsOffenderEventListener(
  @Qualifier("attendanceServiceAppScope")
  private val attendanceService: AttendanceService,
  private val gson: Gson,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @SqsListener("whereabouts", factory = "hmppsQueueContainerFactoryProxy")
  @WithSpan(value = "hmpps_prisoner_event_queue", kind = SpanKind.SERVER)
  fun handleEvents(requestJson: String?) {
    try {
      log.info("Raw message {}", requestJson)
      val (message, messageAttributes) = gson.fromJson(requestJson, Message::class.java)
      val eventType = messageAttributes.eventType.value
      log.info("Processing message of type {}", eventType)

      when (eventType) {
        "DATA_COMPLIANCE_DELETE-OFFENDER" -> {
          val (offenderIdDisplay, offenders) = gson.fromJson(message, DeleteOffenderEventMessage::class.java)
          val bookingIds = offenders.flatMap { offender -> offender.bookings.map { it.offenderBookId } }

          attendanceService.deleteAttendancesForOffenderDeleteEvent(
            offenderIdDisplay,
            bookingIds,
          )
        }
      }
    } catch (e: Exception) {
      log.error("handleEvents() Unexpected error", e)
      throw e
    }
  }
}
