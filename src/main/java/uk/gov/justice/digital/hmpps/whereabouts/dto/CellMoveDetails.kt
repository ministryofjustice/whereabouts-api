package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Cell move details")
data class CellMoveDetails(
  @ApiModelProperty(required = true, value = "Offender booking id", example = "1")
  val bookingId: Long,
  @ApiModelProperty(required = true, value = "Offender number", example = "G123V6")
  val offenderNo: String,
  @ApiModelProperty(required = true, value = "Cell to be moved into ", example = "MDI-1-1")
  val internalLocationDescriptionDestination: String,
  @ApiModelProperty(required = true, value = "Cell move reason code ", example = "ADM")
  val cellMoveReasonCode: String,
  @ApiModelProperty(
    required = true,
    value = "Cell move reason comment ",
    example = "The prisoner has been moved for administrative purposes"
  )
  val commentText: String
)
