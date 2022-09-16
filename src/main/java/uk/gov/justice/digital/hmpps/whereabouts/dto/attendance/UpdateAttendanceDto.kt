package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ApiModel(description = "Attendance update details")
data class UpdateAttendanceDto(
  @ApiModelProperty(required = true, value = "Flag to indicate the offender attended the event", example = "true")
  @NotNull
  val attended: Boolean,

  @ApiModelProperty(required = true, value = "Flag to indicate the offender should be paid", example = "true")
  @NotNull
  val paid: Boolean,

  @ApiModelProperty(value = "Reason the offender did not attendance the event", example = "Refused")
  val absentReason: AbsentReason? = null,

  @ApiModelProperty(value = "Absence reason the offender did not attendance the event", example = "Courses")
  val absentSubReason: AbsentSubReason? = null,

  @ApiModelProperty(value = "Comments about non attendance. This also gets used for the IEP warning text ")
  @Size(max = 240)
  val comments: String? = null
)
