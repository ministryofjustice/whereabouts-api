package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@ApiModel(description = "Video Link Booking details")
data class VideoLinkBookingSpecification(

  @ApiModelProperty(value = "Offender booking Id", example = "1")
  @field:NotNull
  val bookingId: Long?,

  @ApiModelProperty(
    value = "The location of the court that requires the appointment",
    example = "York Crown Court",
    required = true
  )
  @NotEmpty
  val court: String,

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

@ApiModel(description = "Detail of a single Video Link Appointment, either pre, main or post")
data class VideoLinkAppointmentSpecification(

  @ApiModelProperty(value = "The identifier of the appointment's location", example = "1", required = true)
  @field:NotNull
  val locationId: Long?,

  @ApiModelProperty(value = "Start Time of the appointment. ISO-8601 date-time format", example = "2020-12-23T09:00:00", required = true)
  val startTime: LocalDateTime,

  @ApiModelProperty(value = "Finish Time of the appointment. ISO-8601 date-time format", example = "2020-12-23T09:30:00", required = true)
  val endTime: LocalDateTime
)
