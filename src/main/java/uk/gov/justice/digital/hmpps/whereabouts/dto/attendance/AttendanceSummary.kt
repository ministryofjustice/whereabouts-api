package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import java.time.YearMonth

data class AttendanceSummary(
  val month: YearMonth,
  var acceptableAbsence: Int = 0,
  var unacceptableAbsence: Int = 0,
  var total: Int = 0,
)
