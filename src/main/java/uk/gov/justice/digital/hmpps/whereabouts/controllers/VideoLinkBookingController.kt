package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.CourtLocationsResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentsResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.model.Court
import uk.gov.justice.digital.hmpps.whereabouts.services.court.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.AppointmentLocationsService
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.AppointmentLocationsSpecification
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.AvailableLocations
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.AvailableVideoLinkBookingLocations
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.LocationIdAndDescription
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.VideoLinkBookingLocationsSpecification
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingOptions
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingSearchSpecification
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Api(tags = ["court"])
@RestController
@RequestMapping(value = ["court"], produces = [MediaType.APPLICATION_JSON_VALUE])
class VideoLinkBookingController(
  private val courtService: CourtService,
  private val videoLinkBookingService: VideoLinkBookingService,
  private val appointmentLocationsService: AppointmentLocationsService
) {
  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/all-courts"])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(
    value = "All court locations",
    response = CourtLocationsResponse::class,
    notes = "Return all court locations"
  )
  fun getCourtNames() = CourtLocationsResponse(courtLocations = courtService.courtNames)

  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/courts"])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(
    value = "All courts",
    notes = "Return information about all courts."
  )
  fun getCourts(): List<Court> = courtService.courts

  @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-appointments"])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(
    value = "Video link appointments",
    response = VideoLinkAppointmentsResponse::class,
    notes = "Return video link appointments"
  )
  fun getVideoLinkAppointments(@RequestBody appointmentIds: Set<Long>): VideoLinkAppointmentsResponse {
    val courtAppointments = videoLinkBookingService.getVideoLinkAppointments(appointmentIds)

    if (courtAppointments.isEmpty()) return VideoLinkAppointmentsResponse()

    return VideoLinkAppointmentsResponse(appointments = courtAppointments)
  }

  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-bookings/{videoBookingId}"])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(
    value = "A video Link Booking",
    response = VideoLinkBookingResponse::class,
    notes = "Return a video Link Booking"
  )
  fun getVideoLinkBooking(
    @ApiParam(value = "Video link booking id", required = true)
    @PathVariable("videoBookingId")
    videoBookingId: Long
  ) = videoLinkBookingService.getVideoLinkBooking(videoBookingId)

  @GetMapping(
    path = ["/video-link-bookings/prison/{agencyId}/date/{date}"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation("Get all video link bookings for the specified date and prison, optionally filtering by court.")
  fun getVideoLinkBookingsByPrisonDateAndCourt(
    @ApiParam(value = "Return video link bookings for this prison only")
    @PathVariable(name = "agencyId")
    agencyId: String,

    @ApiParam(value = "Return video link bookings for this date only. ISO-8601 date format")
    @PathVariable(name = "date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    date: LocalDate,

    @ApiParam(
      value = "The name a court.  If present the response will only contain video link bookings for this court. Otherwise all bookings will be returned.",
      required = false,
      example = "Wimbledon",
    )
    @RequestParam(name = "court", required = false)
    court: String?,

    @ApiParam(
      value = "The identifier of a court.  If present the response will only contain video link bookings for this court. Otherwise all bookings will be returned. Takes precedence over court.",
      required = false,
      example = "CMBGMC"
    )
    @RequestParam(name = "courtId", required = false)
    courtId: String?
  ): List<VideoLinkBookingResponse> {
    return videoLinkBookingService.getVideoLinkBookingsForPrisonAndDateAndCourt(agencyId, date, court, courtId)
  }

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-bookings"])
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "Create a Video Link Booking")
  fun createVideoLinkBooking(
    @RequestBody
    @Valid
    videoLinkBookingSpecification: VideoLinkBookingSpecification
  ) =
    videoLinkBookingService.createVideoLinkBooking(videoLinkBookingSpecification)

  @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-bookings/{videoBookingId}"])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiOperation(value = "Update a Video Link Booking")
  fun updateVideoLinkBooking(
    @ApiParam(value = "Video link booking id", required = true)
    @PathVariable("videoBookingId")
    @NotNull
    videoBookingId: Long?,

    @RequestBody
    @Valid
    videoLinkBookingUpdateSpecification: VideoLinkBookingUpdateSpecification?
  ): ResponseEntity<Void> {
    videoLinkBookingService.updateVideoLinkBooking(videoBookingId!!, videoLinkBookingUpdateSpecification!!)
    /**
     * The Open API implementation used to document this resource contains a bug which manifests
     * when a PUT operation has a Unit return type. For that reason this method returns a ResponseEntity<Void>.
     * When bug is fixed the following line should be removed along with the return type of this method
     * so that it reverts to an implicit type of :Unit
     */
    return ResponseEntity.noContent().build()
  }

  @DeleteMapping(path = ["/video-link-bookings/{videoBookingId}"])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiOperation(value = "Delete a Video Link Booking")
  fun deleteVideoLinkBooking(
    @ApiParam(value = "Video link booking id", required = true)
    @PathVariable("videoBookingId")
    videoBookingId: Long
  ): ResponseEntity<Void> {
    videoLinkBookingService.deleteVideoLinkBooking(videoBookingId)
    return ResponseEntity.noContent().build()
  }

  @PutMapping(
    path = ["/video-link-bookings/{videoLinkBookingId}/comment"],
    consumes = [MediaType.TEXT_PLAIN_VALUE]
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiOperation(value = "Update the comment for a Video Link Booking")
  fun updateVideoLinkBookingComment(
    @ApiParam(value = "Video link booking id", required = true)
    @PathVariable("videoLinkBookingId")
    videoLinkBookingId: Long,

    @RequestBody(required = false)
    comment: String?
  ): ResponseEntity<Void> {
    videoLinkBookingService.updateVideoLinkBookingComment(videoLinkBookingId, comment)
    /**
     * The Open API implementation used to document this resource contains a bug which manifests
     * when a PUT operation has a Unit return type. For that reason this method returns a ResponseEntity<Void>.
     * When bug is fixed the following line should be removed along with the return type of this method
     * so that it reverts to an implicit type of :Unit
     */
    return ResponseEntity.noContent().build()
  }

  @PostMapping(
    path = ["/appointment-location-finder"],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @ApiOperation(
    value = "Request the locations that are available for a series of appointment intervals optionally including locations currently assigned to selected video link bookings.",
    response = AvailableLocations::class,
    responseContainer = "List"
  )
  fun findLocationsForAppointmentIntervals(
    @Valid
    @RequestBody
    specification: AppointmentLocationsSpecification
  ): List<AvailableLocations> {
    return appointmentLocationsService.findLocationsForAppointmentIntervals(specification)
  }

  @PostMapping(
    path = ["/vlb-appointment-location-finder"],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @ApiOperation(
    value = "Request the locations that could be used for a new Video Link Booking, optionally including locations currently assigned to selected current video link bookings.",
    response = AvailableVideoLinkBookingLocations::class
  )
  fun findLocationsForVideoLinkBookingIntervals(
    @Valid
    @RequestBody
    specification: VideoLinkBookingLocationsSpecification
  ): AvailableVideoLinkBookingLocations {
    val availableLocations = appointmentLocationsService
      .findLocationsForAppointmentIntervals(specification.toAppointmentLocationsSpecification())

    return AvailableVideoLinkBookingLocations.fromAvailableLocations(
      availableLocations,
      specification.preInterval != null
    )
  }

  @GetMapping(
    path = ["/vlb-locations/{agencyId}"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "Provide the set of locations within a prison used for Video Link Bookings")
  fun getVideoLinkBookingLocationsForAgency(
    @ApiParam(value = "Agency id", required = true)
    @PathVariable("agencyId")
    agencyId: String
  ): List<LocationIdAndDescription> = appointmentLocationsService.allVideoLinkLocationsForAgency(agencyId)

  @PostMapping()
  fun findAvaliableVideoLinkBookingOptions(
    @Valid
    @RequestBody
    specification: VideoLinkBookingSearchSpecification
  ): VideoLinkBookingOptions =
    appointmentLocationsService.findVideoLinkBookingOptions(specification)
}
