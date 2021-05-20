package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CourtLocationResponse(val courtLocations: Set<String>? = emptySet())

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CourtIdsResponse(val courtIds: Set<String>? = emptySet())

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VideoLinkAppointmentsResponse(val appointments: Set<VideoLinkAppointmentDto>? = emptySet())

data class CreateBookingAppointment(
  val appointmentType: String,
  val locationId: Long,
  val comment: String? = null,
  val startTime: String,
  val endTime: String
)

data class CreateAppointmentSpecification(
  val bookingId: Long? = null,
  val locationId: Long,
  val appointmentType: String,
  val comment: String? = null,
  val startTime: LocalDateTime,
  val endTime: LocalDateTime? = null
)

@ApiModel(description = "The data related to a single appointment.")
data class AppointmentDto(
  @ApiModelProperty(required = true, value = "The event Id associated with this appointment")
  val id: Long,
  @ApiModelProperty(required = true, value = "The Id of the agency where the appointment is", example = "MDI")
  val agencyId: String,
  @ApiModelProperty(required = true, value = "The Id of the location to be used for this appointment")
  val locationId: Long,
  @ApiModelProperty(required = true, value = "The code for the type of appointment this is", example = "INTERV")
  val appointmentTypeCode: String,
  @ApiModelProperty(required = true, value = "The NOMS Id of the offender who this appointment is for")
  val offenderNo: String,
  @ApiModelProperty(required = true, value = "When the appointment is scheduled to start")
  val startTime: LocalDateTime,
  @ApiModelProperty(required = false, value = "When the appointment is scheduled to end")
  val endTime: LocalDateTime?
)

@ApiModel(description = "The data related to a single appointment.")
data class AppointmentSearchDto(
  @ApiModelProperty(required = true, value = "The event Id associated with this appointment")
  val id: Long,
  @ApiModelProperty(required = true, value = "The Id of the agency where the appointment is", example = "MDI")
  val agencyId: String,
  @ApiModelProperty(required = true, value = "The Id of the location to be used for this appointment")
  val locationId: Long,
  @ApiModelProperty(required = true, value = "The description of the location")
  val locationDescription: String,
  @ApiModelProperty(required = true, value = "The code for the type of appointment this is", example = "INTERV")
  val appointmentTypeCode: String,
  @ApiModelProperty(required = true, value = "The description of the appointment type")
  val appointmentTypeDescription: String,
  @ApiModelProperty(required = true, value = "The NOMS Id of the offender who this appointment is for")
  val offenderNo: String,
  @ApiModelProperty(required = true, value = "Offender first name")
  val firstName: String,
  @ApiModelProperty(required = true, value = "Offender last name")
  val lastName: String,
  @ApiModelProperty(required = true, value = "When the appointment is scheduled to start")
  val startTime: LocalDateTime,
  @ApiModelProperty(required = false, value = "When the appointment is scheduled to end")
  val endTime: LocalDateTime?,
  @ApiModelProperty(required = true, value = "The name of the user who created this appointment", example = "ASMITH")
  val createUserId: String
)

data class VideoLinkBookingDto(
  val id: Long,
  val main: VideoLinkAppointmentDto,
  val pre: VideoLinkAppointmentDto? = null,
  val post: VideoLinkAppointmentDto? = null
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

data class AppointmentDetailsDto(
  val appointment: AppointmentDto? = null,
  val videoLinkBooking: VideoLinkBookingDto? = null
)

data class Event(
  val eventId: Long,
  val agencyId: String,
)

data class AppointmentCreatedDto(
  val mainAppointmentId: Long,
  val recurringAppointments: Set<Long>? = null
)

fun VideoLinkBooking.preAppointmentDto() = pre?.let {
  VideoLinkAppointmentDto(
    id = it.id!!,
    bookingId = bookingId,
    appointmentId = it.appointmentId,
    hearingType = HearingType.PRE,
    court = court,
    createdByUsername = createdByUsername,
    madeByTheCourt = madeByTheCourt
  )
}

fun VideoLinkBooking.mainAppointmentDto() =
  VideoLinkAppointmentDto(
    id = main.id!!,
    bookingId = bookingId,
    appointmentId = main.appointmentId,
    hearingType = HearingType.MAIN,
    court = court,
    createdByUsername = createdByUsername,
    madeByTheCourt = madeByTheCourt
  )

fun VideoLinkBooking.postAppointmentDto() = post?.let {
  VideoLinkAppointmentDto(
    id = it.id!!,
    bookingId = bookingId,
    appointmentId = it.appointmentId,
    hearingType = HearingType.POST,
    court = court,
    createdByUsername = createdByUsername,
    madeByTheCourt = madeByTheCourt
  )
}
