package uk.gov.justice.digital.hmpps.whereabouts.model

import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany

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
  val relatedAppointments: MutableList<RelatedAppointment>? = null
)

@Entity(name = "APPOINTMENT")
data class RelatedAppointment(
  @Id
  val id: Long
)
