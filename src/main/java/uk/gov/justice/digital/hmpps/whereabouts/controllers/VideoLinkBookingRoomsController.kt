package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationIdAndDescription
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationInsidePrisonIdAndDescription
import uk.gov.justice.digital.hmpps.whereabouts.services.LocationService

@Tag(name = "video-link-booking-rooms")
@RestController
@Deprecated(message = "Location service provides this data")
class VideoLinkBookingRoomsController(
  private val locationService: LocationService,

) {
  @GetMapping(
    path = ["/video-link-rooms/{agencyId}"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    description = "List of all the Video Link Booking rooms in the prison.",
    summary = "getVideoLinkBookingRooms",
  )
  fun getVideoLinkBookingRooms(
    @Parameter(description = "The prison", required = true)
    @PathVariable("agencyId")
    agencyId: String,
  ): List<LocationIdAndDescription> = locationService.getVideoLinkRoomsForPrison(agencyId)

  @GetMapping(
    path = ["/location/video-link-rooms/{prisonId}"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    description = "List of all the Video Link Booking rooms in the prison.",
    summary = "getVideoLinkBookingRooms",
  )
  fun getVideoLinkBookingRoomsFromLocationApi(
    @Parameter(description = "The prison", required = true)
    @PathVariable("prisonId")
    prisonId: String,
  ): List<LocationInsidePrisonIdAndDescription> = locationService.getVideoLinkRoomsForPrisonFromLocationApi(prisonId)
}
