package uk.gov.justice.digital.hmpps.whereabouts.model
data class NotifyRequest(
  val firstName: String,
  val lastName: String,
  val dateOfBirth: String,
  val offenderId: String,
  val videoLinkBookingId: String,
  val mainAppointmentDate: String,
  val mainAppointmentStartTime: String,
  val mainAppointmentEndTime: String,
  val preHearingStartTime: String,
  val preHearingEndTime: String,
  val postHearingStartTime: String,
  val postHearingEndTime: String,
  val comments: String,
  val prisonName: String,
  val prisonEmail: String,
)
