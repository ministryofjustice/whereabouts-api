package uk.gov.justice.digital.hmpps.whereabouts.model

import javax.persistence.Entity
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
data class VideoLinkAppointment(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
  val bookingId: Long,
  val appointmentId: Long,
  val court: String,

  @Enumerated(EnumType.STRING)
  val hearingType: HearingType,
  val createdByUsername: String? = null,
  val madeByTheCourt: Boolean? = true
)
