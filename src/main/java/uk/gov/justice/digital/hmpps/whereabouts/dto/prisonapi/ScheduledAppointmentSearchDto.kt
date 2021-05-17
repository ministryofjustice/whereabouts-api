package uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi

import java.time.LocalDateTime

data class ScheduledAppointmentSearchDto(
  val id: Long,
  val agencyId: String,
  val locationId: Long,
  val locationDescription: String,
  val appointmentTypeCode: String,
  val appointmentTypeDescription: String,
  val startTime: LocalDateTime,
  val endTime: LocalDateTime?,
  val offenderNo: String,
  val firstName: String,
  val lastName: String,
  val createUserId: String

)
