package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@ApiModel(description = "Detail of a single Video Link Appointment, either pre, main or post")
data class VideoLinkAppointmentSpecification(

  @ApiModelProperty(value = "The identifier of the appointment's location", example = "1", required = true)
  @field:NotNull
  val locationId: Long?,

  @ApiModelProperty(value = "Start Time of the appointment. ISO-8601 date-time format", example = "2020-12-23T09:00:00", required = true)
  val startTime: LocalDateTime,

  @ApiModelProperty(value = "Finish Time of the appointment. ISO-8601 date-time format", example = "2020-12-23T09:00:00", required = true)
  val endTime: LocalDateTime,
)
