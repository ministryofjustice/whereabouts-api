package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.LocationPrefixDto
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import uk.gov.justice.digital.hmpps.whereabouts.services.LocationService

@Tag(name = "locations")
@RestController
@RequestMapping(value = ["locations"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Deprecated(message = "Replaced by endpoints in locations inside in prison https://locations-inside-prison-api.hmpps.service.justice.gov.uk")
class LocationController(private val locationService: LocationService) {

  @GetMapping("/groups/{agencyId}/{name}")
  @Operation(
    description = "List of cell locations by group at agency location.",
    summary = "getLocationGroup",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "OK"),
      ApiResponse(responseCode = "400", description = "Invalid request."),
      ApiResponse(responseCode = "404", description = "Requested resource not found."),
      ApiResponse(
        responseCode = "500",
        description = "Unrecoverable error occurred whilst processing request.",
      ),
    ],
  )
  @Deprecated(message = "Replaced by endpoint in locations inside in prison https://locations-inside-prison-api.hmpps.service.justice.gov.uk")
  fun getLocationGroup(
    @Parameter(description = "The prison", required = true)
    @PathVariable("agencyId")
    agencyId: String,
    @Parameter(description = "The group name", required = true)
    @PathVariable("name")
    name: String,
  ): List<Location> =
    locationService.getCellLocationsForGroup(agencyId, name)

  @GetMapping("/{agencyId}/{group}/location-prefix")
  @Operation(description = "Get location prefix by group", summary = "getLocationPrefixFromGroup")
  @ApiResponses(
    value = [
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
    ],
  )
  @Deprecated(message = "Replaced by endpoint in locations inside in prison https://locations-inside-prison-api.hmpps.service.justice.gov.uk")
  fun getLocationPrefixFromGroup(
    @Parameter(description = "The prison", required = true)
    @PathVariable("agencyId")
    agencyId: String,
    @Parameter(description = "The group name", required = true, example = "Houseblock 1")
    @PathVariable("group")
    group: String,
  ): LocationPrefixDto =
    locationService.getLocationPrefixFromGroup(agencyId, group)
}
