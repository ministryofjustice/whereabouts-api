package uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi

/**
 * Data from prison api
 */
data class OffenderAttendance(
  var eventDate: String,
  var outcome: String? = null,
  val prisonId: String? = null,
  val activity: String? = null,
  val description: String? = null,
  val comment: String? = null
)
