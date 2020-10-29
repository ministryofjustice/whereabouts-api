package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.ApiModel
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveResult

@ApiModel(description = "Cell move response")
data class CellMoveResponse(val cellMoveResult: CellMoveResult)
