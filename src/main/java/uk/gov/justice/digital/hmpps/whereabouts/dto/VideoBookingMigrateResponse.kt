package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse.LocationTimeslot

@ApiModel(description = "Video booking migrate response")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class VideoBookingMigrateResponse(
  @ApiModelProperty(value = "Video Link booking Id", example = "1", required = true)
  val videoBookingId: Long,

  @ApiModelProperty(value = "Offender booking Id", example = "1", required = true)
  val offenderBookingId: Long,

  @ApiModelProperty(value = "The code/ID of the court this booking is for", example = "YORKMAG", required = true)
  val courtCode: String?,

  @ApiModelProperty(value = "The description for the court.", example = "York Magistrates", required = false)
  val courtName: String?,

  @ApiModelProperty(value = "True if this booking was made by a court user", example = "true", required = true)
  val madeByTheCourt: Boolean,

  @ApiModelProperty(value = "The prison agency code", example = "WWI", required = true)
  val prisonCode: String,

  @ApiModelProperty(value = "True if this is a probation PPOC court", example = "true", required = true)
  val isProbation: Boolean,

  @ApiModelProperty(value = "Free text comments", example = "Requires special access")
  val comment: String?,

  @ApiModelProperty(value = "List of associated events")
  val pre: LocationTimeslot?,

  @ApiModelProperty(value = "Main appointment", required = true)
  val main: LocationTimeslot,

  @ApiModelProperty(value = "Post-hearing appointment")
  val post: LocationTimeslot?,

  @ApiModelProperty(value = "The events associated with this booking")
  val events: List<VideoBookingEvent>? = emptyList(),
)
