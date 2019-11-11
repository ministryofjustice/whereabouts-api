package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty


@ApiModel(description = "Attendances response")
data class AttendancesResponse(
    @ApiModelProperty(value = "Set of attendances")
    var attendances: Set<AttendanceDto>? = null
)

