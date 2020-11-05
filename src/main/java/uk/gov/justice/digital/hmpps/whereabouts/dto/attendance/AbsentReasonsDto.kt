package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason

data class AbsentReasonsDto(
  @ApiModelProperty(value = "List of paid absent reasons", example = "[AcceptableAbsence]", position = 1)
  val paidReasons: Set<AbsentReason>? = null,

  @ApiModelProperty(value = "List of unpaid absent reasons", example = "[UnacceptableAbsence]", position = 2)
  val unpaidReasons: Set<AbsentReason>? = null,

  @ApiModelProperty(value = "List of reasons that trigger IEP Warnings", example = "[Refused]", position = 3)
  val triggersIEPWarning: Set<AbsentReason>? = null
)
