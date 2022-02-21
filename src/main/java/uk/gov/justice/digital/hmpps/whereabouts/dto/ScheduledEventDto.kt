package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * Scheduled Event
 */
@ApiModel(description = "Scheduled Event")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ScheduledEventDto(
  @ApiModelProperty(required = true, value = "Offender booking id")
  val bookingId: Long,

  @ApiModelProperty(required = true, value = "Class of event")
  val eventClass: @NotBlank String? = null,

  @ApiModelProperty(value = "Activity id if any. Used to attend or pay an activity.")
  val eventId: Long? = null,

  @ApiModelProperty(required = true, value = "Status of event")
  val eventStatus: @NotBlank String? = null,

  @ApiModelProperty(required = true, value = "Type of scheduled event (as a code)")
  val eventType: @NotBlank String? = null,

  @ApiModelProperty(required = true, value = "Description of scheduled event type")
  val eventTypeDesc: @NotBlank String? = null,

  @ApiModelProperty(required = true, value = "Sub type (or reason) of scheduled event (as a code)")
  val eventSubType: @NotBlank String? = null,

  @ApiModelProperty(required = true, value = "Description of scheduled event sub type")
  val eventSubTypeDesc: @NotBlank String? = null,

  @ApiModelProperty(required = true, value = "Date on which event occurs")
  val eventDate: @NotNull LocalDate? = null,

  @ApiModelProperty(value = "Date and time at which event starts")
  val startTime: LocalDateTime? = null,

  @ApiModelProperty(value = "Date and time at which event ends")
  val endTime: LocalDateTime? = null,

  @ApiModelProperty(value = "Location at which event takes place (could be an internal location, agency or external address).")
  val eventLocation: String? = null,

  @ApiModelProperty(value = "Id of an internal event location")
  val eventLocationId: Long? = null,

  @ApiModelProperty(value = "The agency ID for the booked internal location", example = "WWI")
  val agencyId: String? = null,

  @ApiModelProperty(required = true, value = "Code identifying underlying source of event data")
  val eventSource: @NotBlank String? = null,

  @ApiModelProperty(value = "Source-specific code for the type or nature of the event")
  val eventSourceCode: String? = null,

  @ApiModelProperty(value = "Source-specific description for type or nature of the event")
  val eventSourceDesc: String? = null,

  @ApiModelProperty(value = "Activity attendance, possible values are the codes in the 'PS_PA_OC' reference domain.")
  val eventOutcome: String? = null,

  @ApiModelProperty(value = "Activity performance, possible values are the codes in the 'PERFORMANCE' reference domain.")
  val performance: String? = null,

  @ApiModelProperty(value = "Activity no-pay reason.")
  val outcomeComment: String? = null,

  @ApiModelProperty(value = "Activity paid flag.")
  val paid: Boolean? = null,

  @ApiModelProperty(value = "Amount paid per activity session in pounds")
  val payRate: BigDecimal? = null,

  @ApiModelProperty(value = "The code for the activity location")
  val locationCode: String? = null,

  @ApiModelProperty(value = "Staff member who created the appointment")
  val createUserId: String? = null,
)
