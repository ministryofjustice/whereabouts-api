package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CourtLocationResponse(val courtLocations: Set<String>? = emptySet())

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CourtAppointmentsResponse(val appointments: Set<CourtAppointmentDto>? = emptySet())

@ApiModel(description = "Court appointment details")
data class CourtAppointmentDto (
    @ApiModelProperty(required = true, value = "Court appointment id", example = "1")
    val id: Long? = null,
    @ApiModelProperty(required = true, value = "Offender booking id", example = "1")
    val bookingId: Long? = null,
    @ApiModelProperty(required = true, value = "Appointment id, maps to nomis event id", example = "1")
    val appointmentId: Long? = null,
    @ApiModelProperty(required = true, value = "The location of the court that requires the appointment", example = "York Crown Court")
    val court: String? = null,
    @ApiModelProperty(required = true, value = "Type of court hearing", example = "MAIN, PRE , POST")
    val hearingType: HearingType? = null
)


data class CreateBookingAppointment (
    val appointmentType: String,
    val locationId: Long,
    val comment: String? = null,
    val startTime: String,
    val endTime: String
)
@ApiModel(description = "Information required to create a court appointment")
data class CreateCourtAppointment(
    @NotNull
    @ApiModelProperty(required = true, value = "Offender booking id", example = "1")
    val bookingId: Long,

    @NotNull
    @ApiModelProperty(required = true, value = "Location id of the appointment", example = "1")
    val locationId: Long,

    @ApiModelProperty(required = true, value = "Comments", example = "Requires special access")
    val comment: String? = null,

    @NotNull
    @ApiModelProperty(required = true, value = "Start time of the appointment", example = "2020-12-23T:09:00:00")
    val startTime: String,

    @NotNull
    @ApiModelProperty(required = true, value = "End time of the appointment", example = "2020-12-23T:09:00:00")
    val endTime: String,

    @NotNull
    @ApiModelProperty(required = true, value = "The location of the court that requires the appointment", example = "York Crown Court")
    val court: String,

    @ApiModelProperty(required = true, value = "Type of court hearing", example = "MAIN, PRE , POST defaults to MAIN")
    val hearingType: HearingType = HearingType.MAIN
)

data class Event(val eventId: Long)
