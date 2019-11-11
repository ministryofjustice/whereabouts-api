package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor

@ApiModel(description = "Absences response")
open data class AbsencesResponse(
    @ApiModelProperty(value = "Set of absences")
    var absences: Set<AbsenceDto>? = null
)

