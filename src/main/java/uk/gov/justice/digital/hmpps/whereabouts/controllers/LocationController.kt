package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.LocationPrefixDto
import uk.gov.justice.digital.hmpps.whereabouts.model.CellWithAttributes
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import uk.gov.justice.digital.hmpps.whereabouts.services.LocationService

@Tag(name = "locations")
@RestController
@RequestMapping(value = ["locations"], produces = [MediaType.APPLICATION_JSON_VALUE])
class LocationController(private val locationService: LocationService) {

  @GetMapping("/groups/{agencyId}/{name}")
  @Operation(
    description = "List of cell locations by group at agency location.",
    summary = "getLocationGroup"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = Location::class, responseContainer = "List"),
      ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class),
      ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse::class),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  fun getLocationGroup(
    @Parameter(name = "The prison", required = true) @PathVariable("agencyId") agencyId: String,
    @Parameter(name = "The group name", required = true) @PathVariable("name") name: String
  ): List<Location> =
    locationService.getCellLocationsForGroup(agencyId, name)

  @GetMapping("/cellsWithCapacity/{agencyId}/{group}")
  @Operation(
    description = "List of cells by group at agency location which have capacity.",
    summary = "getCellsWithCapacityForGroup"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = CellWithAttributes::class, responseContainer = "List"),
      ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class),
      ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse::class),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  fun getCellsWithCapacityForGroup(
    @Parameter(name = "The prison", required = true) @PathVariable("agencyId") agencyId: String,
    @Parameter(name = "The group name", required = true) @PathVariable("group") group: String,
    @Parameter(name = "Cell attribute") @RequestParam(name = "attribute") attribute: String?
  ): List<CellWithAttributes> =
    locationService.getCellsWithCapacityForGroup(agencyId, group, attribute)

  @GetMapping("/{agencyId}/{group}/location-prefix")
  @Operation(description = "Get location prefix by group", summary = "getLocationPrefixFromGroup")
  @ApiResponses(
    value = [
      ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class),
      ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse::class),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  fun getLocationPrefixFromGroup(
    @Parameter(name = "The prison", required = true) @PathVariable("agencyId") agencyId: String,
    @Parameter(name = "The group name", required = true, example = "Houseblock 1") @PathVariable("group") group: String
  ): LocationPrefixDto =
    locationService.getLocationPrefixFromGroup(agencyId, group)
}
