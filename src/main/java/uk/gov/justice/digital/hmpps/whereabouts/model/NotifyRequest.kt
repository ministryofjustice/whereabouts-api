package uk.gov.justice.digital.hmpps.whereabouts.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class NotifyRequest(
  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val mainHearing: VideoLinkAppointment,
  val preHearing: VideoLinkAppointment?,
  val postHearing: VideoLinkAppointment?,
  val comments: String?,
  val prisonName: String,
  val courtName: String,
) {
  fun constructMapOfNotifyRequest(): Map<String, String> {
    return mapOf(
      "firstName" to firstName,
      "lastName" to lastName,
      "dateOfBirth" to dateOfBirth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
      "date" to mainHearing.startDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
      "mainHearingStartAndEndTime" to formatStartAndEndTime(mainHearing),
      "preHearingStartAndEndTime" to formatStartAndEndTime(preHearing),
      "postHearingStartAndEndTime" to formatStartAndEndTime(postHearing),
      "comments" to (comments ?: "None entered"),
      "prison" to prisonName,
      "hearingLocation" to courtName,
    )
  }
  private fun formatStartAndEndTime(videoLinkAppointment: VideoLinkAppointment?): String {
    if (videoLinkAppointment != null) {
      return "${videoLinkAppointment.startDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME)} to ${videoLinkAppointment.endDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME)}"
    }
    return "None requested"
  }
}
