package uk.gov.justice.digital.hmpps.whereabouts.model

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

enum class HearingType {
  MAIN,
  PRE,
  POST
}

@Entity
@Table(name = "VIDEO_LINK_APPOINTMENT")
@EntityListeners(AuditingEntityListener::class)
data class VideoLinkAppointment(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  var bookingId: Long,
  var appointmentId: Long,
  var court: String? = null,
  var courtId: String? = null,

  @Enumerated(EnumType.STRING)
  var hearingType: HearingType,

  @CreatedBy
  var createdByUsername: String? = null,
  var madeByTheCourt: Boolean? = true
)
