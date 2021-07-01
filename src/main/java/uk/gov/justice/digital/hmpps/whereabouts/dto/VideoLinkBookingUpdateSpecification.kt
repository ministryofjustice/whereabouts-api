package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid

data class VideoLinkBookingUpdateSpecification(

  @ApiModelProperty(
    value = "The identifier of the court that requires the appointment.  This must be one of the court identifier values from the courts register service.",
    example = "CMBGMC",
    required = false,
    notes = "One of court or courtId must contain a value.  courtId takes precedence over court."
  )
  val courtId: String? = null,

  @ApiModelProperty(value = "Free text comments", example = "Requires special access")
  override val comment: String? = null,

  @ApiModelProperty(value = "Pre-hearing appointment")
  @Valid
  override val pre: VideoLinkAppointmentSpecification? = null,

  @ApiModelProperty(value = "Main appointment", required = true)
  @field:Valid
  override val main: VideoLinkAppointmentSpecification,

  @ApiModelProperty(value = "Post-hearing appointment")
  @Valid
  override val post: VideoLinkAppointmentSpecification? = null
) : VideoLinkAppointmentsSpecification
