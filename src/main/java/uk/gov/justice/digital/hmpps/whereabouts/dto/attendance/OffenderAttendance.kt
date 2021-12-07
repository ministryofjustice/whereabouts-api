package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

/**
 * Data from prison api
 */
data class OffenderAttendance(
  var eventDate: String,
  var outcome: String? = null,
  val location: String,
  val activity: String,
  val activityDescription: String,
  val comments: String? = null
)
