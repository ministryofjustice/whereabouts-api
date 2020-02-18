package uk.gov.justice.digital.hmpps.whereabouts.model

import javax.persistence.*

enum class HearingType {
  MAIN,
  PRE,
  POST
}

@Entity
@Table(name = "COURT_APPOINTMENT")
data class CourtAppointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    val bookingId: Long,
    val appointmentId: Long,
    val court: String,
    val hearingType: HearingType
)
