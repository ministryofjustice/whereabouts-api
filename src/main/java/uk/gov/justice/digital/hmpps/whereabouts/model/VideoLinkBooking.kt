package uk.gov.justice.digital.hmpps.whereabouts.model

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MapKey
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "VIDEO_LINK_BOOKING")
@EntityListeners(AuditingEntityListener::class)
data class VideoLinkBooking(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,

  var offenderBookingId: Long,
  var courtName: String? = null,
  var courtId: String? = null,
  var madeByTheCourt: Boolean? = true,

  @CreatedBy
  var createdByUsername: String? = null,
) {
  @OneToMany(
    mappedBy = "videoLinkBooking",
    fetch = FetchType.EAGER,
    cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
    orphanRemoval = true
  )
  @MapKey(name = "hearingType")
  var appointments: MutableMap<HearingType, VideoLinkAppointment> = mutableMapOf()

  fun addPreAppointment(appointmentId: Long, id: Long? = null) = appointments.put(
    PRE,
    VideoLinkAppointment(
      id = id,
      videoLinkBooking = this,
      appointmentId = appointmentId,
      hearingType = PRE
    )
  )

  fun addMainAppointment(appointmentId: Long, id: Long? = null) = appointments.put(
    MAIN,
    VideoLinkAppointment(
      id = id,
      videoLinkBooking = this,
      appointmentId = appointmentId,
      hearingType = MAIN
    )
  )

  fun addPostAppointment(appointmentId: Long, id: Long? = null) = appointments.put(
    POST,
    VideoLinkAppointment(
      id = id,
      videoLinkBooking = this,
      appointmentId = appointmentId,
      hearingType = POST
    )
  )
}
