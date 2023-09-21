package uk.gov.justice.digital.hmpps.whereabouts.model

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDateTime

data class VideoLinkBookingEventWithRoomNames(
  val eventId: Long? = null,

  @Enumerated(EnumType.STRING)
  val eventType: VideoLinkBookingEventType,
  val timestamp: LocalDateTime,
  val userId: String? = null,
  val videoLinkBookingId: Long,
  val agencyId: String? = null,
  val offenderBookingId: Long? = null,
  val court: String? = null,
  val courtId: String? = null,
  val madeByTheCourt: Boolean? = null,
  val comment: String? = null,
  val mainNomisAppointmentId: Long? = null,
  val mainLocationId: Long? = null,
  val mainLocationName: String? = null,
  val mainStartTime: LocalDateTime? = null,
  val mainEndTime: LocalDateTime? = null,
  val preNomisAppointmentId: Long? = null,
  val preLocationId: Long? = null,
  val preLocationName: String? = null,
  val preStartTime: LocalDateTime? = null,
  val preEndTime: LocalDateTime? = null,
  val postNomisAppointmentId: Long? = null,
  val postLocationId: Long? = null,
  val postLocationName: String? = null,
  val postStartTime: LocalDateTime? = null,
  val postEndTime: LocalDateTime? = null,
)
