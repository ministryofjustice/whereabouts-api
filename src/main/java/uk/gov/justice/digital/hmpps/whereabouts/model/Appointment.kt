package uk.gov.justice.digital.hmpps.whereabouts.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

enum class RepeatPeriod {
  WEEKLY,
  DAILY,
  WEEKDAY,
  MONTHLY,
  FORTNIGHTLY,
}

@Entity
data class MainRecurringAppointment(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val repeatPeriod: RepeatPeriod,
  val count: Long,

  @OneToMany
  val recurringAppointments: List<RecurringAppointment>? = null
)

@Entity
data class RecurringAppointment(
  @Id
  val id: Long
)
