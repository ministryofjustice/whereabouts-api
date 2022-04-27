package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "agencies")
@RestController
@RequestMapping(value = ["agencies"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AgencyController(
  @Qualifier("locationGroupServiceSelector") private val locationGroupService: LocationGroupService,
  private val whereaboutsEnabledService: WhereaboutsEnabledService
) {

  @GetMapping("/{agencyId}/locations/groups")
  @Operation(
    description = "List of all available Location Groups at agency.",
    summary = "getAvailableLocationGroups"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = LocationGroup::class, responseContainer = "List"),
      ApiResponse(
        code = 400,
        message = "Invalid request.",
        response = ErrorResponse::class,
        responseContainer = "List"
      ),
      ApiResponse(
        code = 404,
        message = "Requested resource not found.",
        response = ErrorResponse::class,
        responseContainer = "List"
      ),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class,
        responseContainer = "List"
      )
    ]
  )
  fun getAvailableLocationGroups(
    @Parameter(
      name = "The prison",
      required = true
    ) @PathVariable("agencyId") agencyId: String
  ): List<LocationGroup> =
    locationGroupService.getLocationGroupsForAgency(agencyId)

  @GetMapping("/{agencyId}/locations/whereabouts")
  @Operation(
    description = "Whereabouts details (e.g. whether enabled) for prison.",
    summary = "getWhereabouts"
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 200,
        message = "OK",
        response = WhereaboutsConfig::class
      ),
      ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class),
      ApiResponse(
        code = 404,
        message = "Requested resource not found.",
        response = ErrorResponse::class
      ),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  fun getWhereabouts(
    @Parameter(
      name = "The prison",
      required = true
    ) @PathVariable("agencyId") agencyId: String
  ): WhereaboutsConfig =
    WhereaboutsConfig(whereaboutsEnabledService.isEnabled(agencyId))
}
