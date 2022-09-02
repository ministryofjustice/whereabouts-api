package uk.gov.justice.digital.hmpps.whereabouts.model

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.MapKey
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "VIDEO_LINK_BOOKING")
@EntityListeners(AuditingEntityListener::class)
class VideoLinkBooking(
  id: Long? = null,

  val offenderBookingId: Long,
  var courtName: String? = null,
  var courtId: String? = null,
  val madeByTheCourt: Boolean? = true,
  val agencyId: String,
  val comment: String? = null,

) : BaseEntity(id) {
  @OneToMany(
    mappedBy = "videoLinkBooking",
    fetch = FetchType.EAGER,
    cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
    orphanRemoval = true
  )
  @MapKey(name = "hearingType")
  val appointments: MutableMap<HearingType, VideoLinkAppointment> = mutableMapOf()

  @CreatedBy
  var createdByUsername: String? = null

  fun addPreAppointment(appointmentId: Long, startDateTime: LocalDateTime, endDateTime: LocalDateTime, id: Long? = null) = appointments.put(
    PRE,
    VideoLinkAppointment(
      id = id,
      videoLinkBooking = this,
      appointmentId = appointmentId,
      hearingType = PRE,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
    )
  )

  fun addMainAppointment(appointmentId: Long, startDateTime: LocalDateTime, endDateTime: LocalDateTime, id: Long? = null) = appointments.put(
    MAIN,
    VideoLinkAppointment(
      id = id,
      videoLinkBooking = this,
      appointmentId = appointmentId,
      hearingType = MAIN,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
    )
  )

  fun addPostAppointment(appointmentId: Long, startDateTime: LocalDateTime, endDateTime: LocalDateTime, id: Long? = null) = appointments.put(
    POST,
    VideoLinkAppointment(
      id = id,
      videoLinkBooking = this,
      appointmentId = appointmentId,
      hearingType = POST,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
    )
  )

  override fun toString(): String =
    "VideoLinkBooking(id = $id, offenderBookingId = $offenderBookingId, courtName = $courtName, courtId = $courtId, madeByTheCourt = $madeByTheCourt, agencyId = $agencyId, comment = $comment)"

  fun copy(): VideoLinkBooking = VideoLinkBooking(
    id,
    offenderBookingId,
    courtName,
    courtId,
    madeByTheCourt,
    agencyId,
    comment
  ).also {
    it.appointments.putAll(appointments)
    it.createdByUsername = createdByUsername
  }
}
