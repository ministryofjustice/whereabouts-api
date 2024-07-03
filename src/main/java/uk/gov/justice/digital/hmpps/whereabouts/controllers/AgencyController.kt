package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
  private val whereaboutsEnabledService: WhereaboutsEnabledService,
) {

  @GetMapping("/{agencyId}/locations/groups")
  @Operation(
    description = "List of all available Location Groups at agency.",
    summary = "getAvailableLocationGroups",
  )
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(
      responseCode = "400",
      description = "Invalid request.",
      content =
      [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = ErrorResponse::class),
        ),
      ],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Requested resource not found.",
      content =
      [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = ErrorResponse::class),
        ),
      ],
    ),
    ApiResponse(
      responseCode = "500",
      description = "Unrecoverable error occurred whilst processing request.",
      content =
      [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = ErrorResponse::class),
        ),
      ],
    ),
  )
  @Deprecated(message = "Replaced by endpoint in locations inside in prison https://locations-inside-prison-api.hmpps.service.justice.gov.uk/locations/prison/{prisonId}/groups")
  fun getAvailableLocationGroups(
    @Parameter(
      description = "The prison",
      required = true,
    )
    @PathVariable("agencyId")
    agencyId: String,
  ): List<LocationGroup> =
    locationGroupService.getLocationGroupsForAgency(agencyId)

  @GetMapping("/{agencyId}/locations/whereabouts")
  @Operation(
    description = "Whereabouts details (e.g. whether enabled) for prison.",
    summary = "getWhereabouts",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK",
      ),
      ApiResponse(responseCode = "400", description = "Invalid request."),
      ApiResponse(
        responseCode = "404",
        description = "Requested resource not found.",
      ),
      ApiResponse(
        responseCode = "500",
        description = "Unrecoverable error occurred whilst processing request.",
      ),
    ],
  )
  @Deprecated(message = "Not used - will be removed")
  fun getWhereabouts(
    @Parameter(
      description = "The prison",
      required = true,
    )
    @PathVariable("agencyId")
    agencyId: String,
  ): WhereaboutsConfig =
    WhereaboutsConfig(whereaboutsEnabledService.isEnabled(agencyId))
}
