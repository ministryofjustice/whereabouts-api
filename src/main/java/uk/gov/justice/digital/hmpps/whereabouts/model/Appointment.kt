package uk.gov.justice.digital.hmpps.whereabouts.model

import javax.persistence.CascadeType
import javax.persistence.Entity
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

@Entity(name = "MAIN_RECURRING_APPOINTMENT")
data class MainRecurringAppointment(
  @Id
  val id: Long,

  val repeatPeriod: RepeatPeriod,
  val count: Long,

  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "MAIN_RECURRING_APPOINTMENT_ID")
  val recurringAppointments: List<RecurringAppointment>? = null
)

@Entity(name = "RECURRING_APPOINTMENT")
data class RecurringAppointment(
  @Id
  val id: Long
)
