package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Absences response")
data class AbsencesResponse(
  @ApiModelProperty(value = "Description of absence reason", example = "Refused to attend - incentive level warning added")
  val description: String,
  @ApiModelProperty(value = "List of absences")
  val absences: List<AbsenceDto>,
)
