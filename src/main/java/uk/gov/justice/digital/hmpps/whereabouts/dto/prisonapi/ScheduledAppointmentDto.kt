package uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi

import java.time.LocalDateTime

data class ScheduledAppointmentDto(
  val id: Long,
  val agencyId: String,
  val locationId: Long,
  val appointmentTypeCode: String,
  val startTime: LocalDateTime,
  val endTime: LocalDateTime?,
  val offenderNo: String
)
