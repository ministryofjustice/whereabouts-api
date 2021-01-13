package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CourtLocationResponse(val courtLocations: Set<String>? = emptySet())

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VideoLinkAppointmentsResponse(val appointments: Set<VideoLinkAppointmentDto>? = emptySet())

data class CreateBookingAppointment(
  val appointmentType: String,
  val locationId: Long,
  val comment: String? = null,
  val startTime: String,
  val endTime: String
)

@ApiModel(description = "Video link appointment details")
data class VideoLinkAppointmentDto(
  @ApiModelProperty(value = "Court appointment id", example = "1")
  val id: Long,
  @ApiModelProperty(value = "Offender booking id", example = "1")
  val bookingId: Long,
  @ApiModelProperty(value = "Appointment id, maps to nomis event id", example = "1")
  val appointmentId: Long,
  @ApiModelProperty(value = "The location of the court that requires the appointment", example = "York Crown Court")
  val court: String,
  @ApiModelProperty(value = "Type of court hearing", example = "MAIN, PRE , POST")
  val hearingType: HearingType,
  @ApiModelProperty(value = "Username of the appointment creator", example = "john1")
  val createdByUsername: String?,
  @ApiModelProperty(value = "Determines if the appointment was made by the court")
  val madeByTheCourt: Boolean? = true
)

data class Event(
  val eventId: Long,
  val agencyId: String,
)
