package uk.gov.justice.digital.hmpps.whereabouts.dto

import io.swagger.annotations.ApiModel

@ApiModel(description = "Cell move response")
data class CellMoveResponse(val cellMoveResult: CellMoveResult)
