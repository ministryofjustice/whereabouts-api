package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Video Link search details")
data class VideoLinkBookingSearchDetails(
  @ApiModelProperty(value = "court id", example = "CMBGMC")
  val courtId: String,

  @ApiModelProperty(value = "prison ids", example = "LII, MDI", required = true)
  val prisonIds: List<String>,
)
