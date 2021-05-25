package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.RepeatPeriod
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CourtLocationsResponse(val courtLocations: Set<String>? = emptySet())

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CourtIdsResponse(val courtIds: Set<String>? = emptySet())

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VideoLinkAppointmentsResponse(val appointments: Set<VideoLinkAppointmentDto>? = emptySet())

data class CreateBookingAppointment(
  val appointmentType: String,
  val locationId: Long,
  val comment: String? = null,
  val startTime: String,
  val endTime: String,
  val repeat: Repeat? = null
)

@ApiModel(description = "The data required to create an appointment")
data class CreateAppointmentSpecification(
  @ApiModelProperty(required = true, value = "The offender booking id")
  val bookingId: Long,
  @ApiModelProperty(required = true, value = "The location id of where the appointment will take place")
  val locationId: Long,
  @ApiModelProperty(required = true, value = "Appointment type", example = "INST")
  val appointmentType: String,
  @ApiModelProperty(required = false, value = "Additional information")
  val comment: String? = null,
  @ApiModelProperty(
    required = true,
    value = "The date and time the appointment is scheduled for",
    example = "2021-05-23T17:00:00"
  )
  val startTime: LocalDateTime,
  @ApiModelProperty(
    required = false,
    value = "The estimated date time the appointment will end",
    example = "2021-05-23T17:00:00"
  )
  val endTime: LocalDateTime? = null,
  @ApiModelProperty(required = false, value = "Describes how many times this appointment is to be repeated")
  val repeat: Repeat? = null
)

data class CreatePrisonAppointment(
  val appointmentDefaults: AppointmentDefaults,
  val appointments: List<Appointment>,
  val repeat: Repeat?
)

data class AppointmentDefaults(
  val appointmentType: String,
  val comment: String? = null,
  val startTime: LocalDateTime,
  val endTime: LocalDateTime?,
  val locationId: Long
)

data class Appointment(
  val bookingId: Long,
  val comment: String? = null,
  val startTime: LocalDateTime,
  val endTime: LocalDateTime?,
)

@ApiModel(description = "Describes how many times this appointment is to be repeated")
data class Repeat(
  @ApiModelProperty(
    required = true,
    value = "Repeat period",
    example = "Daily",
    allowableValues = "Weekly,Daily,Weekday,Monthly,Fortnightly"
  )
  val repeatPeriod: RepeatPeriod,
  @ApiModelProperty(required = true, value = "Specifies the amount of times the repeat period will be applied")
  val count: Long
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
  val offenderNo: String? = null,
  @ApiModelProperty(required = true, value = "When the appointment is scheduled to start")
  val startTime: LocalDateTime,
  @ApiModelProperty(required = false, value = "When the appointment is scheduled to end")
  val endTime: LocalDateTime?,
  @ApiModelProperty(required = false, value = "Created by user id")
  val createUserId: String? = null,
  @ApiModelProperty(required = false, value = "Additional information regarding the appointment")
  val comment: String? = null

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

@ApiModel(description = "Video link appointment booking")
data class VideoLinkBookingDto(
  @ApiModelProperty(required = true, value = "id of the video link appointment booking")
  val id: Long,
  @ApiModelProperty(required = true, value = "Main appointment")
  val main: VideoLinkAppointmentDto,
  @ApiModelProperty(required = false, value = "Pre appointment")
  val pre: VideoLinkAppointmentDto? = null,
  @ApiModelProperty(required = false, value = "Post appointment")
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

  @ApiModelProperty(
    value = "The name of the court that requires the appointment",
    example = "York Crown Court"
  )
  val court: String,

  @ApiModelProperty(
    value = "The identifier of the court that requires the appointment. If present this will be one of the identifier values from the courts register service.",
    example = "CMBGMC"
  )
  val courtId: String?,

  @ApiModelProperty(value = "Type of court hearing", example = "MAIN, PRE , POST")
  val hearingType: HearingType,
  @ApiModelProperty(value = "Username of the appointment creator", example = "john1")
  val createdByUsername: String?,
  @ApiModelProperty(value = "Determines if the appointment was made by the court")
  val madeByTheCourt: Boolean? = true,
  @ApiModelProperty(value = "When the appointment is scheduled to start", example = "2020-12-23T10:00")
  val startTime: LocalDateTime? = null,
  @ApiModelProperty(value = "When the appointment is scheduled to end", example = "2020-12-24T10:00")
  val endTime: LocalDateTime? = null,
  @ApiModelProperty(value = "The location id of where the appointment will take place")
  val locationId: Long? = null,
)

@ApiModel(description = "Recurring appointment")
data class RecurringAppointmentDto(
  @ApiModelProperty(
    required = true,
    value = "Repeat period",
    example = "Daily",
    allowableValues = "Weekly,Daily,Weekday,Monthly,Fortnightly"
  )
  val repeatPeriod: RepeatPeriod,
  @ApiModelProperty(required = true, value = "Specifies the amount of times the repeat period will be applied")
  val count: Long
)

@ApiModel(description = "Appointment details, linking video link bookings and recurring appointments")
data class AppointmentDetailsDto(
  @ApiModelProperty(required = true, value = "Appointment details pulled from NOMIS")
  val appointment: AppointmentDto,
  @ApiModelProperty(required = false, value = "Video link booking details")
  val videoLinkBooking: VideoLinkBookingDto? = null,
  @ApiModelProperty(required = false, value = "Recurring appointment details")
  val recurring: RecurringAppointmentDto? = null
)

data class Event(
  val eventId: Long,
  val agencyId: String
)

@ApiModel(description = "The details of an appointment that has just been created")
data class CreatedAppointmentDetailsDto(
  @ApiModelProperty(value = "The id of the appointment that was created.", example = "123456")
  val appointmentEventId: Long,
  @ApiModelProperty(
    required = true,
    value = "The Booking id of the offender for whom the appointment was created.",
    example = "123456"
  )
  val bookingId: Long,
  @ApiModelProperty(value = "The start time of the appointment.", example = "2018-12-31T23:50")
  val startTime: LocalDateTime,
  @ApiModelProperty(value = "The end time of the appointment.", example = "2018-12-31T23:59")
  val endTime: LocalDateTime? = null,
  @ApiModelProperty(value = "The scheduled event subType", example = "ACTI")
  val appointmentType: String,
  @ApiModelProperty(
    value = "The identifier of the appointments' Location. The location must be situated in the requestor's case load.",
    example = "25",
  )
  val locationId: Long,
)
