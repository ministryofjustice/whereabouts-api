package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AbsentReasonDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AbsentReasonsDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AbsentReasonsV2Dto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AbsentSubReasonDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Companion.absentSubReasonTriggers
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Companion.iepTriggers
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Companion.paidReasons
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason.Companion.unpaidReasons
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason.Companion.paidSubReasons
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason.Companion.unpaidSubReasons

@Api(tags = ["absence-reasons"])
@RestController
@RequestMapping(value = ["absence-reasons"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AbsentReasonsController {

  @GetMapping
  fun reasons(): AbsentReasonsDto =
    AbsentReasonsDto(
      paidReasons,
      unpaidReasons,
      iepTriggers,
    )

  @GetMapping(value = ["/v2"])
  fun reasonsV2(): AbsentReasonsV2Dto =
    AbsentReasonsV2Dto(
      paidReasons.toReasonDto(),
      unpaidReasons.toReasonDto(),
      iepTriggers.toList(),
      absentSubReasonTriggers,
      paidSubReasons.toSubReasonDto(),
      unpaidSubReasons.toSubReasonDto(),
    )
}

private fun List<AbsentSubReason>.toSubReasonDto() = map { AbsentSubReasonDto(it, it.label) }
private fun Set<AbsentReason>.toReasonDto() = toList().map { AbsentReasonDto(it, it.label) }
