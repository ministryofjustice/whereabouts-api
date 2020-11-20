package uk.gov.justice.digital.hmpps.whereabouts.model

import javax.persistence.CascadeType.PERSIST
import javax.persistence.CascadeType.REMOVE
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "VIDEO_LINK_BOOKING")
data class VideoLinkBooking(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @ManyToOne(optional = false, cascade = [PERSIST, REMOVE])
  @JoinColumn(name = "MAIN_APPOINTMENT", nullable = false, updatable = false)
  val main: VideoLinkAppointment,

  @ManyToOne(optional = true, cascade = [PERSIST, REMOVE])
  @JoinColumn(name = "PRE_APPOINTMENT", nullable = true, updatable = false)
  val pre: VideoLinkAppointment? = null,

  @ManyToOne(optional = true, cascade = [PERSIST, REMOVE])
  @JoinColumn(name = "POST_APPOINTMENT", nullable = true, updatable = false)
  val post: VideoLinkAppointment? = null
)
