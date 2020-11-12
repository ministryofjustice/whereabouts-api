package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Cell move reason response")
data class CellMoveReasonsResponse(
  val reasons: Set<Reason>
)

@ApiModel(value = "Cell move reason")
data class Reason(
  @ApiModelProperty(required = true, value = "Reason code", example = "ADM", position = 1)
  val code: String,
  @ApiModelProperty(required = true, value = "Reason description", example = "Administrative", position = 2)
  val description: String
)
