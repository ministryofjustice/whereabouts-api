package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.CellWithAttributes
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import uk.gov.justice.digital.hmpps.whereabouts.services.LocationService

@Api(tags = ["locations"])
@RestController
@RequestMapping(value = ["locations"], produces = [MediaType.APPLICATION_JSON_VALUE])
class LocationController(private val locationService: LocationService) {

  @GetMapping("/groups/{agencyId}/{name}")
  @ApiOperation(value = "List of cell locations by group at agency location.", notes = "List of cell locations by group at agency location.", nickname = "getLocationGroup")
  @ApiResponses(value = [
    ApiResponse(code = 200, message = "OK", response = Location::class, responseContainer = "List"),
    ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class, responseContainer = "List"),
    ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse::class, responseContainer = "List"),
    ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse::class, responseContainer = "List")
  ])
  fun getLocationGroup(@ApiParam(value = "The prison", required = true) @PathVariable("agencyId") agencyId: String,
                       @ApiParam(value = "The group name", required = true) @PathVariable("name") name: String): List<Location>
    = locationService.getCellLocationsForGroup(agencyId, name)

  @GetMapping("/cellsWithCapacity/{agencyId}/{group}")
  @ApiOperation(value = "List of cells by group at agency location which have capacity.", notes = "List of cells  by group at agency location which have capacity.", nickname = "getCellsWithCapacityForGroup")
  @ApiResponses(value = [
    ApiResponse(code = 200, message = "OK", response = CellWithAttributes::class, responseContainer = "List"),
    ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class, responseContainer = "List"),
    ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse::class, responseContainer = "List"),
    ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse::class, responseContainer = "List")
  ])
  fun getCellsWithCapacityForGroup(@ApiParam(value = "The prison", required = true) @PathVariable("agencyId") agencyId: String,
                       @ApiParam(value = "The group name", required = true) @PathVariable("group") group: String,
                       @ApiParam(value = "Cell attribute") @RequestParam(name = "attribute") attribute: String?): List<CellWithAttributes>
          = locationService.getCellsWithCapacityForGroup(agencyId, group, attribute)

}