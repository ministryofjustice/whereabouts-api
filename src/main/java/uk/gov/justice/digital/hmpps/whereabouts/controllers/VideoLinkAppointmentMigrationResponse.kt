package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.ApiModel

@ApiModel(description = "Information about booking migration ")
data class VideoLinkAppointmentMigrationResponse(
  val videoLinkAppointmentRemaining: Long,
  val videoLinkBookingRemaining: Long,
)
