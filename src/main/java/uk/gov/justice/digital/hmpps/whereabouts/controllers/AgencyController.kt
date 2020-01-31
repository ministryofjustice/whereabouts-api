package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup
import uk.gov.justice.digital.hmpps.whereabouts.model.WhereaboutsConfig
import uk.gov.justice.digital.hmpps.whereabouts.services.LocationGroupService
import uk.gov.justice.digital.hmpps.whereabouts.services.WhereaboutsEnabledService

@Api(tags = ["agencies"])
@RestController
@RequestMapping(value = ["agencies"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AgencyController(
    @Qualifier("locationGroupServiceSelector") private val locationGroupService: LocationGroupService,
    private val whereaboutsEnabledService: WhereaboutsEnabledService
) {

  @GetMapping("/{agencyId}/locations/groups")
  @ApiOperation(value = "List of all available Location Groups at agency.", notes = "List of all available Location Groups at agency.", nickname = "getAvailableLocationGroups")
  @ApiResponses(value = [
    ApiResponse(code = 200, message = "OK", response = LocationGroup::class, responseContainer = "List"),
    ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class, responseContainer = "List"),
    ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse::class, responseContainer = "List"),
    ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse::class, responseContainer = "List")
  ])
  fun getAvailableLocationGroups(@ApiParam(value = "The prison", required = true) @PathVariable("agencyId") agencyId: String): List<LocationGroup>
    = locationGroupService.getLocationGroupsForAgency(agencyId)


  @GetMapping("/{agencyId}/locations/whereabouts")
  @ApiOperation(value = "Whereabouts details (e.g. whether enabled) for prison.", notes = "Whereabouts details (e.g. whether enabled) for prison.", nickname = "getWhereabouts")
  @ApiResponses(value = [ApiResponse(code = 200, message = "OK", response = WhereaboutsConfig::class), ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class), ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse::class), ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse::class)])
  fun getWhereabouts(@ApiParam(value = "The prison", required = true) @PathVariable("agencyId") agencyId: String): WhereaboutsConfig
    = WhereaboutsConfig(whereaboutsEnabledService.isEnabled(agencyId))

}