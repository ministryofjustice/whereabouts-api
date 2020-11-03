package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Absences response")
data class AbsencesResponse(
  @ApiModelProperty(value = "Set of absences")
  var absences: Set<AbsenceDto>? = null
)
