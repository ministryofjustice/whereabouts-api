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
import uk.gov.justice.digital.hmpps.whereabouts.dto.CourtLocationResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentsResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingUpdateSpecification
import uk.gov.justice.digital.hmpps.whereabouts.services.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.ValidationException
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkAppointmentLinker
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.AppointmentLocationsService
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.AppointmentLocationsSpecification
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.AvailableLocations
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Api(tags = ["court"])
@RestController
@RequestMapping(value = ["court"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CourtController(
  private val courtService: CourtService,
  private val appointmentLinker: VideoLinkAppointmentLinker,
  private val appointmentLocationsService: AppointmentLocationsService
) {
  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/all-courts"])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(
    value = "All court locations",
    response = CourtLocationResponse::class,
    notes = "Return all court locations"
  )
  fun getCourtLocations() = CourtLocationResponse(courtLocations = courtService.getCourtLocations())

  @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-appointments"])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(
    value = "Video link appointments",
    response = VideoLinkAppointmentsResponse::class,
    notes = "Return video link appointments"
  )
  fun getVideoLinkAppointments(@RequestBody appointmentIds: Set<Long>): VideoLinkAppointmentsResponse {
    val courtAppointments = courtService.getVideoLinkAppointments(appointmentIds)

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
  ) = courtService.getVideoLinkBooking(videoBookingId)

  @Deprecated("This only retrieves bookings for Wandsworth, Move to requesting bookings for a specific prison")
  @GetMapping(path = ["/video-link-bookings/date/{date}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation("Get all video link bookings at Wandsworth for the specified date, optionally filtering by court.")
  fun getVideoLinkBookingsByDateAndCourt(
    @ApiParam(value = "Return video link bookings for this date only. ISO-8601 date format")
    @PathVariable(name = "date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    date: LocalDate,

    @ApiParam(
      value = "The identifier for a court.  If present the response will only contain video link bookings for this court. Otherwise all bookings will be returned.",
      required = false,
      example = "Wimbledon"
    )
    @RequestParam(name = "court", required = false)
    court: String?
  ): List<VideoLinkBookingResponse> {
    return courtService.getVideoLinkBookingsForPrisonAndDateAndCourt("WWI", date, court)
  }

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
      value = "The identifier for a court.  If present the response will only contain video link bookings for this court. Otherwise all bookings will be returned.",
      required = false,
      example = "Wimbledon"
    )
    @RequestParam(name = "court", required = false)
    court: String?
  ): List<VideoLinkBookingResponse> {
    return courtService.getVideoLinkBookingsForPrisonAndDateAndCourt(agencyId, date, court)
  }

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-bookings"])
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "Create a Video Link Booking")
  fun createVideoLinkBooking(
    @RequestBody
    @Valid
    videoLinkBookingSpecification: VideoLinkBookingSpecification
  ) =
    courtService.createVideoLinkBooking(videoLinkBookingSpecification)

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
    courtService.updateVideoLinkBooking(videoBookingId!!, videoLinkBookingUpdateSpecification!!)
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
  ) = courtService.deleteVideoLinkBooking(videoBookingId)

  @PostMapping(path = ["/appointment-linker"])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiOperation(value = "Create Video Link Bookings for dangling Video Link Appointments")
  fun linkDanglingAppointments(@RequestBody chunkSize: Int?) {
    appointmentLinker.linkAppointments(chunkSize)
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

    @RequestBody
    comment: String?
  ): ResponseEntity<Void> {
    courtService.updateVideoLinkBookingComment(videoLinkBookingId, comment)
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
    validateSpecification(specification)
    return appointmentLocationsService.findLocationsForAppointmentIntervals(specification)
  }

  private fun validateSpecification(specification: AppointmentLocationsSpecification) {
    specification.appointmentIntervals
      .filterNot { it.start.isBefore(it.end) }
      .forEach { throw ValidationException("Invalid $it. Start must precede end") }
  }
}
