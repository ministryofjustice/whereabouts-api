package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Cell move reason")
data class CellMoveReasonDto(
  @ApiModelProperty(value = "Offender booking id", position = 1, example = "1")
  val bookingId: Long,
  @ApiModelProperty(
    value = "Bed assignment sequence. Used as a primary key when combined with the booking id",
    position = 2,
    example = "2"
  )
  val bedAssignmentsSequence: Int,
  @ApiModelProperty(value = "Id of the case note created", position = 3, example = "3")
  val caseNoteId: Long
)
