package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class VideoLinkBookingUpdateSpecification(

  @ApiModelProperty(value = "Booking placed by the court", required = true)
  @NotNull
  val madeByTheCourt: Boolean?,

  @ApiModelProperty(value = "Free text comments", example = "Requires special access")
  val comment: String? = null,

  @ApiModelProperty(value = "Pre-hearing appointment")
  @Valid
  val pre: VideoLinkAppointmentSpecification? = null,

  @ApiModelProperty(value = "Main appointment", required = true)
  @field:Valid
  val main: VideoLinkAppointmentSpecification,

  @ApiModelProperty(value = "Post-hearing appointment")
  @Valid
  val post: VideoLinkAppointmentSpecification? = null
)
