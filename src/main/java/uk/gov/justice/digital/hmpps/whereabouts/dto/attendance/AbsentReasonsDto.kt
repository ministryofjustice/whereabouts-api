package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason

data class AbsentReasonsDto(
  @ApiModelProperty(value = "List of paid absent reasons", example = "[\"ApprovedCourse\"]", position = 1)
  val paidReasons: List<AbsentReasonDto>,

  @ApiModelProperty(value = "List of unpaid absent reasons", example = "[\"RestInCellOrSick\"]", position = 2)
  val unpaidReasons: List<AbsentReasonDto>,

  @ApiModelProperty(
    value = "List of reasons that trigger IEP Warnings",
    example = "[\"UnacceptableAbsence\"]",
    position = 3
  )
  val triggersIEPWarning: List<AbsentReason>,

  @ApiModelProperty(
    value = "List of reasons that trigger an absence sub reason",
    example = "[\"UnacceptableAbsence\"]"
  )
  val triggersAbsentSubReason: List<AbsentReason>,

  @ApiModelProperty(value = "List of paid absence sub reasons")
  val paidSubReasons: List<AbsentSubReasonDto>,

  @ApiModelProperty(value = "List of unpaid absence sub reasons")
  val unpaidSubReasons: List<AbsentSubReasonDto>,
)

data class AbsentReasonDto(val code: AbsentReason, val name: String)
data class AbsentSubReasonDto(val code: AbsentSubReason, val name: String)
