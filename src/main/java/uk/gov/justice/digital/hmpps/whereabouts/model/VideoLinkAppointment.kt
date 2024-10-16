package uk.gov.justice.digital.hmpps.whereabouts.model

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.Hibernate
import java.time.LocalDateTime

enum class HearingType {
  MAIN,
  PRE,
  POST,
}

@Entity
@Table(name = "VIDEO_LINK_APPOINTMENT")
class VideoLinkAppointment(
  id: Long? = null,

  @ManyToOne
  @JoinColumn(name = "video_link_booking_id")
  val videoLinkBooking: VideoLinkBooking,

  val appointmentId: Long,
  var locationId: Long?,
  var dpsLocationId: String?,
  var startDateTime: LocalDateTime,
  var endDateTime: LocalDateTime,

  @Enumerated(EnumType.STRING)
  val hearingType: HearingType,
) : BaseEntity(id) {
  override fun toString(): String = "VideoLinkAppointment(id = $id, appointmentId = $appointmentId, locationId = $locationId, dpslocationId = $dpsLocationId, startDateTime = $startDateTime, endDateTime = $endDateTime, hearingType = $hearingType)"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as VideoLinkAppointment
    return appointmentId == other.appointmentId
  }

  override fun hashCode(): Int = appointmentId.hashCode()
}
