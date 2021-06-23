package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.services.LocationService
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.LocationIdAndDescription

@Api(tags = ["prison"])
@RestController

class VideoLinkBookingRoomsController(
  private val locationService: LocationService,

) {

  @GetMapping(
    path = ["/video-link-rooms/{agencyId}"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )

  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(
    value = "List of all the Video Link Booking rooms in the prison.",
    notes = "List of all the Video Link Booking rooms in the prison.",
    nickname = "getVideoLinkBookingRooms"
  )

  fun getVideoLinkBookingRooms(
    @ApiParam(value = "The prison", required = true)
    @PathVariable("agencyId") agencyId: String
  ): List<LocationIdAndDescription> = locationService.getVideoLinkRoomsForPrison(agencyId)
}
