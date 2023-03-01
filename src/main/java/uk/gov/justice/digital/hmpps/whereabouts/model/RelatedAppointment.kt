package uk.gov.justice.digital.hmpps.whereabouts.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import java.time.LocalDateTime

enum class RepeatPeriod {
  WEEKLY,
  DAILY,
  WEEKDAYS,
  MONTHLY,
  FORTNIGHTLY,
}

@Entity(name = "RECURRING_APPOINTMENT")
data class RecurringAppointment(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val repeatPeriod: RepeatPeriod,
  val count: Long,
  val startTime: LocalDateTime,

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "RECURRING_APPOINTMENT_ID")
  val relatedAppointments: MutableList<RelatedAppointment>? = null,
)

@Entity(name = "APPOINTMENT")
data class RelatedAppointment(
  @Id
  val id: Long,
)
