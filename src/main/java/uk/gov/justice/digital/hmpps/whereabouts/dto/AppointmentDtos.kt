package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonInclude
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CourtLocationResponse(val courtLocations: Set<String>? = emptySet())

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CourtAppointmentsResponse(val appointments: Set<CourtAppointmentDto>? = emptySet())

data class CourtAppointmentDto (
    val id: Long? = null,
    val bookingId: Long? = null,
    val appointmentId: Long? = null,
    val court: String? = null,
    val hearingType: HearingType? = null
)


data class CreateBookingAppointment (
    val appointmentType: String,
    val locationId: Long,
    val comment: String? = null,
    val startTime: String,
    val endTime: String
)

data class CreateCourtAppointment(
    @NotNull
    val bookingId: Long,
    @NotNull
    val locationId: Long,
    val comment: String? = null,
    @NotNull
    val startTime: String,
    @NotNull
    val endTime: String,
    @NotNull
    val court: String,
    val hearingType: HearingType = HearingType.MAIN
)

data class Event(val eventId: Long)
