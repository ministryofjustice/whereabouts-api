package uk.gov.justice.digital.hmpps.whereabouts.model

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import javax.persistence.CascadeType.PERSIST
import javax.persistence.CascadeType.REMOVE
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "VIDEO_LINK_BOOKING")
@EntityListeners(AuditingEntityListener::class)
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
  var post: VideoLinkAppointment? = null,

  val bookingId: Long,
  val court: String,

  @CreatedBy
  var createdByUsername: String? = null,
  val madeByTheCourt: Boolean? = true,
) {
  fun toAppointments(): List<VideoLinkAppointment> {
    return listOfNotNull(pre, main, post)
  }
}
