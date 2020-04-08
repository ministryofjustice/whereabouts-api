package uk.gov.justice.digital.hmpps.whereabouts.model

import javax.persistence.*

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
    val hearingType: HearingType,
    val createdByUsername: String? = null,
    val madeByTheCourt: Boolean? = true
)
