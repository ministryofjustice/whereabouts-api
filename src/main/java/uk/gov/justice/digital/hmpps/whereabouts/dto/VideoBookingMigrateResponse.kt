package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBookingEventType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@ApiModel(description = "Video booking migrate response")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class VideoBookingMigrateResponse(
  @ApiModelProperty(value = "Video Link booking Id", example = "1", required = true)
  val videoBookingId: Long,

  @ApiModelProperty(value = "Offender booking Id", example = "1", required = true)
  val offenderBookingId: Long,

  @ApiModelProperty(value = "The code/ID of the court this booking is for", example = "YORKMAG")
  val courtCode: String?,

  @ApiModelProperty(value = "The description for the court.", example = "York Magistrates")
  val courtName: String?,

  @ApiModelProperty(value = "True if this booking was made by a court user", example = "true", required = true)
  val madeByTheCourt: Boolean,

  @ApiModelProperty(value = "The prison agency code", example = "WWI", required = true)
  val prisonCode: String,

  @ApiModelProperty(value = "True if this is a probation PPOC court", example = "true", required = true)
  val probation: Boolean,

  @ApiModelProperty(value = "Free text comments", example = "Requires special access")
  val comment: String?,

  @ApiModelProperty(value = "List of associated events")
  val pre: AppointmentLocationTimeSlot?,

  @ApiModelProperty(value = "Main appointment", required = true)
  val main: AppointmentLocationTimeSlot,

  @ApiModelProperty(value = "Post-hearing appointment")
  val post: AppointmentLocationTimeSlot?,

  @ApiModelProperty(value = "The events associated with this booking")
  val events: List<VideoBookingMigrateEvent>? = emptyList(),
)

@ApiModel(description = "Video booking migrate event")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class VideoBookingMigrateEvent(
  @ApiModelProperty(value = "Event ID", example = "1", required = true)
  val eventId: Long,

  @ApiModelProperty(value = "The time that this event was recorded", example = "2023-10-01 07:50:54", required = true)
  val eventTime: LocalDateTime,

  @ApiModelProperty(value = "The type of event (CREATE, UPDATE, DELETE)", example = "CREATE", required = true)
  val eventType: VideoLinkBookingEventType,

  @ApiModelProperty(value = "The username of person who created this event", example = "user@mail.net", required = true)
  val createdByUsername: String,

  @ApiModelProperty(value = "The prison agency code", example = "WWI", required = true)
  val prisonCode: String,

  @ApiModelProperty(value = "The code/ID of the court this booking is for", example = "YORKMAG", required = true)
  val courtCode: String?,

  @ApiModelProperty(value = "The description for the court.", example = "York Magistrates", required = false)
  val courtName: String?,

  @ApiModelProperty(value = "True if this booking was made by a court user", example = "true", required = true)
  val madeByTheCourt: Boolean,

  @ApiModelProperty(value = "Free text comments", example = "Requires special access")
  val comment: String?,

  @ApiModelProperty(value = "List of associated events")
  val pre: AppointmentLocationTimeSlot?,

  @ApiModelProperty(value = "Main appointment", required = true)
  val main: AppointmentLocationTimeSlot,

  @ApiModelProperty(value = "Post-hearing appointment")
  val post: AppointmentLocationTimeSlot?,
)

@ApiModel(description = "Class describing the location, date, start and end times for an appointment")
data class AppointmentLocationTimeSlot(

  @ApiModelProperty(value = "The location identifier", example = "1", required = true)
  val locationId: Long,

  @ApiModelProperty(value = "Date of the appointment. ISO date format", example = "2020-12-23", required = true)
  val date: LocalDate,

  @ApiModelProperty(value = "Start time", example = "09:00", required = true)
  val startTime: LocalTime,

  @ApiModelProperty(value = "End time", example = "09:30", required = true)
  val endTime: LocalTime,
)
