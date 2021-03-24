package uk.gov.justice.digital.hmpps.whereabouts.model

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

/**
 * A flat class / table containing all possible fields seems to be the best fit for the way in which it is used.
 */
@Entity
@Table(name = "VIDEO_LINK_BOOKING_EVENT")
data class VideoLinkBookingEvent(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val eventId: Long? = null,

  @Enumerated(EnumType.STRING)
  val eventType: VideoLinkBookingEventType,
  val timestamp: LocalDateTime,
  val userId: String? = null,
  val videoLinkBookingId: Long,
  val agencyId: String? = null,
  val offenderBookingId: Long? = null,
  val court: String? = null,
  val madeByTheCourt: Boolean? = null,
  val comment: String? = null,
  val mainNomisAppointmentId: Long? = null,
  val mainLocationId: Long? = null,
  val mainStartTime: LocalDateTime? = null,
  val mainEndTime: LocalDateTime? = null,
  val preNomisAppointmentId: Long? = null,
  val preLocationId: Long? = null,
  val preStartTime: LocalDateTime? = null,
  val preEndTime: LocalDateTime? = null,
  val postNomisAppointmentId: Long? = null,
  val postLocationId: Long? = null,
  val postStartTime: LocalDateTime? = null,
  val postEndTime: LocalDateTime? = null,
)
