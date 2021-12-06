package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Attendances aggregate data")
data class AttendanceSummary(
  @ApiModelProperty(value = "Number of acceptable absences in period")
  var acceptableAbsence: Int = 0,
  @ApiModelProperty(value = "Number of unacceptable absences in period")
  var unacceptableAbsence: Int = 0,
  @ApiModelProperty(value = "Total number of attendances in period (which have an outcome)")
  var total: Int = 0,
)
