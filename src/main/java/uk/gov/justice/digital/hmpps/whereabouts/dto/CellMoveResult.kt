package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Cell move result")
data class CellMoveResult(
  @ApiModelProperty(value = "Offender booking id", example = "1000")
  val bookingId: Long,
  @ApiModelProperty(value = "Id of the establishment", example = "MDI")
  val agencyId: String,
  @ApiModelProperty(value = "Id of the cell location the offender has been moved to", example = "25700")
  val assignedLivingUnitId: Long,
  @ApiModelProperty(value = "Description of cell the offender has been moved to", example = "MDI-2-2-006")
  val assignedLivingUnitDesc: String,
  @ApiModelProperty(
    value = "Bed assignment sequence associated with the entry created for this cell move",
    example = "2",
  )
  val bedAssignmentHistorySequence: Int,
  @ApiModelProperty(
    value = "Case note id",
    example = "2",
  )
  val caseNoteId: Long? = null,
)
