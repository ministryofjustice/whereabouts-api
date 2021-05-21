package uk.gov.justice.digital.hmpps.whereabouts.model

import javax.persistence.CascadeType.PERSIST
import javax.persistence.CascadeType.REMOVE
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "VIDEO_LINK_BOOKING")
data class VideoLinkBooking(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @OneToOne(optional = false, orphanRemoval = true, cascade = [PERSIST, REMOVE])
  @JoinColumn(name = "MAIN_APPOINTMENT")
  var main: VideoLinkAppointment,

  @OneToOne(optional = true, orphanRemoval = true, cascade = [PERSIST, REMOVE])
  @JoinColumn(name = "PRE_APPOINTMENT")
  var pre: VideoLinkAppointment? = null,

  @OneToOne(optional = true, orphanRemoval = true, cascade = [PERSIST, REMOVE])
  @JoinColumn(name = "POST_APPOINTMENT")
  var post: VideoLinkAppointment? = null

) {
  fun toAppointments(): List<VideoLinkAppointment> {
    return listOfNotNull(pre, main, post)
  }

  fun matchesCourt(court: String?, courtId: String?): Boolean {
    if (courtId != null) return main.courtId == courtId
    if (court != null) return main.court == court
    return true
  }
}
