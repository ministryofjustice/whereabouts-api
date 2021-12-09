package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

/**
 * Data from prison api
 */
data class OffenderAttendance(
  var eventDate: String,
  var outcome: String? = null,
  val prisonId: String,
  val activity: String,
  val description: String,
  val comment: String? = null
)
