package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.whereabouts.model.CourtHearingType

@ApiModel(description = "Video Link Booking details")
data class VideoLinkBookingSpecification(

  @ApiModelProperty(value = "Offender booking Id", example = "1")
  @field:NotNull
  val bookingId: Long?,

  @ApiModelProperty(
    value = "The location of the court that requires the appointment",
    example = "York Crown Court",
    required = false,
    notes = "One of court or courtId must contain a value.  courtId takes precedence over court.",
  )
  val court: String?,

  @ApiModelProperty(
    value = "The identifier of the court that requires the appointment.  This must be one of the court identifier values from the courts register service.",
    example = "CMBGMC",
    required = false,
    notes = "One of court or courtId must contain a value.  courtId takes precedence over court.",
  )
  val courtId: String?,

  @ApiModelProperty(
    value = "The type of the court hearing.",
    example = "APPEAL",
    required = false,
  )
  val courtHearingType: CourtHearingType?,

  @ApiModelProperty(value = "Booking placed by the court", required = true)
  @NotNull
  val madeByTheCourt: Boolean?,

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
  override val post: VideoLinkAppointmentSpecification? = null,
) : VideoLinkAppointmentsSpecification
