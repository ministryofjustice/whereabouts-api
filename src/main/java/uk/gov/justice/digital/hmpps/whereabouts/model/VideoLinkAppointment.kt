package uk.gov.justice.digital.hmpps.whereabouts.model

import org.hibernate.Hibernate
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

enum class HearingType {
  MAIN,
  PRE,
  POST
}

@Entity
@Table(name = "VIDEO_LINK_APPOINTMENT")
class VideoLinkAppointment(
  id: Long? = null,

  @ManyToOne
  @JoinColumn(name = "video_link_booking_id")
  val videoLinkBooking: VideoLinkBooking,

  val appointmentId: Long,
  val locationId: Long? = null,
  val startDateTime: LocalDateTime? = null,
  val endDateTime: LocalDateTime? = null,

  @Enumerated(EnumType.STRING)
  val hearingType: HearingType
) : BaseEntity(id) {
  override fun toString(): String = "VideoLinkAppointment(id = $id, appointmentId = $appointmentId, locationId = $locationId, startDateTime = $startDateTime, endDateTime = $endDateTime, hearingType = $hearingType)"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as VideoLinkAppointment
    return appointmentId == other.appointmentId
  }

  override fun hashCode(): Int = appointmentId.hashCode()
}
