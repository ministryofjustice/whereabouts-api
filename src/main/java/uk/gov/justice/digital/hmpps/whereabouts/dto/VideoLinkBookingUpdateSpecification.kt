package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

data class VideoLinkBookingUpdateSpecification(

  @ApiModelProperty(
    value = "The identifier of the court that requires the appointment.",
    example = "CMBGMC",
    required = false,
  )
  @field:NotEmpty
  val courtId: String,

  @ApiModelProperty(value = "Free text comments", example = "Requires special access")
  override val comment: String? = null,

  @ApiModelProperty(value = "Pre-hearing appointment")
  @field:Valid
  override val pre: VideoLinkAppointmentSpecification? = null,

  @ApiModelProperty(value = "Main appointment", required = true)
  @field:Valid
  override val main: VideoLinkAppointmentSpecification,

  @ApiModelProperty(value = "Post-hearing appointment")
  @field:Valid
  override val post: VideoLinkAppointmentSpecification? = null
) : VideoLinkAppointmentsSpecification
