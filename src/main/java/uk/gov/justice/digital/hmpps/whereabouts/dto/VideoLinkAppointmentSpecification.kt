package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

@ApiModel(description = "Detail of a single Video Link Appointment, either pre, main or post")
data class VideoLinkAppointmentSpecification(

  @ApiModelProperty(name = "The identifier of the appointment's location")
  @field:NotNull
  val locationId: Long?,

  @ApiModelProperty(name = "Start Time of the appointment. ISO-8601 date-time format")
  val startTime: LocalDateTime,

  @ApiModelProperty(name = "Finish Time of the appointment. ISO-8601 date-time format")
  val endTime: LocalDateTime
)
