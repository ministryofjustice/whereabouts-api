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

  @OneToOne(optional = false, cascade = [PERSIST, REMOVE])
  @JoinColumn(name = "MAIN_APPOINTMENT", nullable = false, updatable = false, unique = true)
  val main: VideoLinkAppointment,

  @OneToOne(optional = true, cascade = [PERSIST, REMOVE])
  @JoinColumn(name = "PRE_APPOINTMENT", nullable = true, updatable = true, unique = true)
  val pre: VideoLinkAppointment? = null,

  @OneToOne(optional = true, cascade = [PERSIST, REMOVE])
  @JoinColumn(name = "POST_APPOINTMENT", nullable = true, updatable = true, unique = true)
  val post: VideoLinkAppointment? = null

){
  fun toAppointments(): List<VideoLinkAppointment> {
    return listOfNotNull(pre, main, post)
  }
}
