package uk.gov.justice.digital.hmpps.whereabouts.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.MapKey
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.MAIN
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.POST
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType.PRE
import java.time.LocalDateTime

@Entity
@Table(name = "VIDEO_LINK_BOOKING")
@EntityListeners(AuditingEntityListener::class)
class VideoLinkBooking(
  id: Long? = null,

  val offenderBookingId: Long,
  var courtName: String? = null,
  var courtId: String? = null,
  var courtHearingType: CourtHearingType? = null,
  val madeByTheCourt: Boolean? = true,
  var prisonId: String,
  var comment: String? = null,

) : BaseEntity(id) {
  @OneToMany(
    mappedBy = "videoLinkBooking",
    fetch = FetchType.EAGER,
    cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
    orphanRemoval = true,
  )
  @MapKey(name = "hearingType")
  val appointments: MutableMap<HearingType, VideoLinkAppointment> = mutableMapOf()

  @CreatedBy
  var createdByUsername: String? = null

  fun addPreAppointment(appointmentId: Long, locationId: Long, startDateTime: LocalDateTime, endDateTime: LocalDateTime, id: Long? = null) = appointments.put(
    PRE,
    VideoLinkAppointment(
      id = id,
      videoLinkBooking = this,
      appointmentId = appointmentId,
      locationId = locationId,
      hearingType = PRE,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
    ),
  )

  fun addMainAppointment(appointmentId: Long, locationId: Long, startDateTime: LocalDateTime, endDateTime: LocalDateTime, id: Long? = null) = appointments.put(
    MAIN,
    VideoLinkAppointment(
      id = id,
      videoLinkBooking = this,
      appointmentId = appointmentId,
      locationId = locationId,
      hearingType = MAIN,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
    ),
  )

  fun addPostAppointment(appointmentId: Long, locationId: Long, startDateTime: LocalDateTime, endDateTime: LocalDateTime, id: Long? = null) = appointments.put(
    POST,
    VideoLinkAppointment(
      id = id,
      videoLinkBooking = this,
      appointmentId = appointmentId,
      locationId = locationId,
      hearingType = POST,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
    ),
  )

  override fun toString(): String =
    "VideoLinkBooking(id = $id, offenderBookingId = $offenderBookingId, courtName = $courtName, courtId = $courtId, courtHearingType = $courtHearingType, madeByTheCourt = $madeByTheCourt, prisonId = $prisonId, comment = $comment)"

  fun copy(): VideoLinkBooking = VideoLinkBooking(
    id,
    offenderBookingId,
    courtName,
    courtId,
    courtHearingType,
    madeByTheCourt,
    prisonId,
    comment,
  ).also {
    it.appointments.putAll(appointments)
    it.createdByUsername = createdByUsername
  }
}
