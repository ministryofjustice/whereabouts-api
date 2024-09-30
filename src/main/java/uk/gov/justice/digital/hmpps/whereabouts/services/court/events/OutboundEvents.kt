package uk.gov.justice.digital.hmpps.whereabouts.services.court.events
import java.time.LocalDateTime

enum class OutboundEvent(val eventType: String) {
  VIDEO_LINK_BOOKING_MIGRATE("whereabouts-api.videolink.migrate") {
    override fun event(additionalInformation: AdditionalInformation) =
      OutboundHMPPSDomainEvent(
        eventType = eventType,
        additionalInformation = additionalInformation,
        description = "A video link booking has been identified for migration",
      )
  },
  ;

  abstract fun event(additionalInformation: AdditionalInformation): OutboundHMPPSDomainEvent
}

interface AdditionalInformation

data class OutboundHMPPSDomainEvent(
  val eventType: String,
  val additionalInformation: AdditionalInformation,
  val version: String = "1",
  val description: String,
  val occurredAt: LocalDateTime = LocalDateTime.now(),
)

data class VideoLinkBookingMigrate(val videoLinkBookingId: Long) : AdditionalInformation
