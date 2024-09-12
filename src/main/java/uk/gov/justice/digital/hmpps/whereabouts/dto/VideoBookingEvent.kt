package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse.LocationTimeslot
import java.time.LocalDateTime

@ApiModel(description = "Video booking event")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class VideoBookingEvent(
  @ApiModelProperty(value = "Event ID", example = "1", required = true)
  val eventId: Long,

  @ApiModelProperty(value = "The time that this event was recorded", example = "2023-10-01 07:50:54", required = true)
  val eventTime: LocalDateTime,

  @ApiModelProperty(value = "The type of event (CREATE, UPDATE, DELETE)", example = "CREATE", required = true)
  val eventType: String,

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
  val pre: LocationTimeslot?,

  @ApiModelProperty(value = "Main appointment", required = true)
  val main: LocationTimeslot,

  @ApiModelProperty(value = "Post-hearing appointment")
  val post: LocationTimeslot?,
)
