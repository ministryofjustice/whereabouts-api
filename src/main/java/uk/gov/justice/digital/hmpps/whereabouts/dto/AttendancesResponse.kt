package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ApiModel(description = "Attendances response")
data class AttendancesResponse(
    @ApiModelProperty(value = "Set of attendances")
    var attendances: Set<AttendanceDto>? = null,

    @ApiModelProperty(value = "Set of absences")
    var absences: Set<AbsenceDto>? = null
)

