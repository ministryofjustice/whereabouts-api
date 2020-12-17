package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime

@ApiModel(description = "Video Link Booking details")
data class VideoLinkBookingResponse(

  @ApiModelProperty(value = "Video Link booking Id", example = "1", required = true)
  val videoLinkBookingId: Long,

  @ApiModelProperty(value = "Offender booking Id", example = "1", required = true)
  val bookingId: Long,

  @ApiModelProperty(value = "Agency Id", example = "WWI", required = true)
  val agencyId: String,

  @ApiModelProperty(
    value = "The location of the court that requires the appointment",
    example = "York Crown Court",
    required = true
  )
  val court: String,

  @ApiModelProperty(value = "Free text comments", example = "Requires special access")
  val comment: String? = null,

  @ApiModelProperty(value = "Pre-hearing appointment")
  val pre: LocationTimeslot? = null,

  @ApiModelProperty(value = "Main appointment", required = true)
  val main: LocationTimeslot,

  @ApiModelProperty(value = "Post-hearing appointment")
  val post: LocationTimeslot? = null
) {

  @ApiModel(description = "Detail of a booked location, either pre, main or post")
  data class LocationTimeslot(

    @ApiModelProperty(value = "The location identifier", example = "1", required = true)
    val locationId: Long,

    @ApiModelProperty(value = "Start Time. ISO-8601 date-time format", example = "2020-12-23T09:00:00", required = true)
    val startTime: LocalDateTime,

    @ApiModelProperty(value = "Finish Time. ISO-8601 date-time format", example = "2020-12-23T09:30:00", required = true)
    val endTime: LocalDateTime
  )
}
