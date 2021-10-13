package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import java.time.LocalDate

data class OffenderAttendance(
  val eventDate: LocalDate,
  val outcome: String?,
)
