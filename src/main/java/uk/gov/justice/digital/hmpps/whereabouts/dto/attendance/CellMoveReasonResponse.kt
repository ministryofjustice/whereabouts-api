package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import io.swagger.annotations.ApiModel
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveReasonDto

@ApiModel(description = "Cell move reason response")
data class CellMoveReasonResponse(val cellMoveReason: CellMoveReasonDto)
