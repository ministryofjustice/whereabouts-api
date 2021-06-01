package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid

data class VideoLinkBookingUpdateSpecification(

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
