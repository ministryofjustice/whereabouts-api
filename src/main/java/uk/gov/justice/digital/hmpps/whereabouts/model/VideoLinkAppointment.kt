package uk.gov.justice.digital.hmpps.whereabouts.model

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
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
data class VideoLinkAppointment(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  @ManyToOne
  @JoinColumn(name = "video_link_booking_id")
  var videoLinkBooking: VideoLinkBooking,

  var appointmentId: Long,

  @Enumerated(EnumType.STRING)
  var hearingType: HearingType
) {
  override fun toString(): String = "VideoLinkAppointment(id = $id, appointmentId = $appointmentId, hearingType = $hearingType)"

  override fun equals(other: Any?): Boolean {
    return other is VideoLinkAppointment && appointmentId == other.appointmentId
  }

  override fun hashCode(): Int = appointmentId.hashCode()
}
