package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Attendances aggregate data")
data class AttendanceSummary(
  @ApiModelProperty(name = "Number of acceptable absences in period")
  var acceptableAbsence: Int = 0,
  @ApiModelProperty(name = "Number of unacceptable absences in period")
  var unacceptableAbsence: Int = 0,
  @ApiModelProperty(name = "Total number of attendances in period (which have an outcome)")
  var total: Int = 0,
)
