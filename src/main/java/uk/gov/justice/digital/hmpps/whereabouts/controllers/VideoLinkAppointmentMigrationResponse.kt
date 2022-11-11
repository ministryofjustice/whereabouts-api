package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.ApiModel
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkBookingMigrationService.Outcome

@ApiModel(description = "Information about booking migration ")
data class VideoLinkAppointmentMigrationResponse(
  val videoLinkBookingScanned: Int,
  val outcomes: Map<Outcome, Int>,
)
