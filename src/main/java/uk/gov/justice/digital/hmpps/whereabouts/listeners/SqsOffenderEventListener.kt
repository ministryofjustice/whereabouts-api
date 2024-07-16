package uk.gov.justice.digital.hmpps.whereabouts.listeners

import com.google.gson.Gson
import io.awspring.cloud.sqs.annotation.SqsListener
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService

@Service
class SqsOffenderEventListener(
  @Qualifier("attendanceServiceAppScope")
  private val attendanceService: AttendanceService,
  @Qualifier("videoLinkBookingServiceAppScope")
  private val videoLinkBookingService: VideoLinkBookingService,
  private val gson: Gson,
  @Value("\${feature.bvls.enabled}") private val bvlsEnabled: Boolean,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  init {
    log.info("BVLS enabled=$bvlsEnabled for offender events")
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

        "APPOINTMENT_CHANGED" -> {
          if (bvlsEnabled) {
            val appointmentChangedEventMessage = gson.fromJson(message, AppointmentChangedEventMessage::class.java)
            videoLinkBookingService.processNomisUpdate(appointmentChangedEventMessage)
          }
        }
      }
    } catch (e: Exception) {
      log.error("handleEvents() Unexpected error", e)
      throw e
    }
  }
}
