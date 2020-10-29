package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.CellMoveDetails
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.services.CellService
import javax.validation.Valid

@Api(tags = ["cell"])
@RestController
@RequestMapping(value = ["cell"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CellController {

  @Autowired
  private lateinit var cellService: CellService

  @PostMapping("/make-cell-move")
  @ApiOperation(value = "Make a cell move for an offender")
  @ResponseStatus(HttpStatus.CREATED)
  @ApiResponses(
    value = [
      ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class), ApiResponse(
        code = 404,
        message = "Requested resource not found.",
        response = ErrorResponse::class
      ), ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  fun makeCellMove(
    @RequestBody @Valid cellMoveDetails: CellMoveDetails
  ): CellMoveResponse =
    CellMoveResponse(cellMoveResult = cellService.makeCellMove(cellMoveDetails))
}
