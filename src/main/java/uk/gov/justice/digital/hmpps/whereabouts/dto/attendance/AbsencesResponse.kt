package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Absences response")
data class AbsencesResponse(
  @ApiModelProperty(value = "Description of absence reason", example = "Refused to attend with warning")
  val description: String,
  @ApiModelProperty(value = "Set of absences")
  val absences: Set<AbsenceDto>,
)
