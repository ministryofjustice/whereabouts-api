package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Cell move details")
data class CellMoveDetails(
  @ApiModelProperty(required = true, value = "Offender booking id", example = "1000")
  val bookingId: Long,
  @ApiModelProperty(required = true, value = "The new cell destination ", example = "MDI-2-2-006")
  val internalLocationDescriptionDestination: String,
  @ApiModelProperty(required = true, value = "Cell move reason code ", example = "ADM")
  val cellMoveReasonCode: String
)
