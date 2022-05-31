package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ApiModel(description = "Scheduled response")
data class ScheduledResponse(
  @ApiModelProperty(value = "List of scheduled events")
  val scheduled: List<PrisonerScheduleDto>
)

@Schema(description = "Prisoner Schedule")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PrisonerScheduleDto(
  @Schema(required = true, description = "Offender number (e.g. NOMS Number)")
  val offenderNo: String,

  @Schema(description = "Activity id if any. Used to attend or pay the event")
  val eventId: Long?,

  @Schema(description = "Booking id for offender")
  val bookingId: Long?,

  @Schema(
    required = true,
    description = "The number which (uniquely) identifies the internal location associated with the Scheduled Event (Prisoner Schedule)"
  )
  val locationId: @NotNull Long?,

  @Schema(required = true, description = "Offender first name")
  val firstName: @NotBlank String?,

  @Schema(required = true, description = "Offender last name")
  val lastName: @NotBlank String?,

  @Schema(required = true, description = "Offender cell")
  val cellLocation: @NotBlank String?,

  @Schema(required = true, description = "Event code")
  val event: @NotBlank String?,

  @Schema(required = true, description = "Event type, e.g. VISIT, APP, PRISON_ACT")
  val eventType: @NotBlank String?,

  @Schema(required = true, description = "Description of event code")
  val eventDescription: @NotBlank String?,

  @Schema(required = true, description = "Location of the event")
  val eventLocation: String?,

  @Schema(description = "Id of an internal event location")
  val eventLocationId: Long?,

  @Schema(required = true, description = "The event's status. Includes 'CANC', meaning cancelled for 'VISIT'")
  val eventStatus: @NotBlank String?,

  @Schema(required = true, description = "Comment")
  val comment: @Size(max = 4000) String?,

  @Schema(required = true, description = "Date and time at which event starts")
  val startTime: @NotNull LocalDateTime?,

  @Schema(description = "Date and time at which event ends")
  val endTime: LocalDateTime?,

  @Schema(description = "Attendance, possible values are the codes in the 'PS_PA_OC' reference domain")
  val eventOutcome: String?,

  @Schema(description = "Possible values are the codes in the 'PERFORMANCE' reference domain")
  val performance: String?,

  @Schema(description = "No-pay reason")
  val outcomeComment: String?,

  @Schema(description = "Activity paid flag")
  val paid: Boolean?,

  @Schema(description = "Amount paid per activity session in pounds")
  val payRate: BigDecimal?,

  @Schema(description = "Activity excluded flag")
  val excluded: Boolean?,

  @Schema(description = "Activity time slot")
  val timeSlot: TimePeriod?,

  @Schema(description = "The code for the activity location")
  val locationCode: String?,

  @Schema(description = "Event scheduled has been suspended")
  val suspended: Boolean?,
)
