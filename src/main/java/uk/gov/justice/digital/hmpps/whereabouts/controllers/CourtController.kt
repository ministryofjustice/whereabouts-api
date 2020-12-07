package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.CourtLocationResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateVideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentsResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.services.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkAppointmentLinker
import javax.validation.Valid

@Api(tags = ["court"])
@RestController
@RequestMapping(value = ["court"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CourtController(
  private val courtService: CourtService,
  private val appointmentLinker: VideoLinkAppointmentLinker
) {
  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/all-courts"])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(
    value = "All court locations",
    response = CourtLocationResponse::class,
    notes = "Return all court locations"
  )
  fun getCourtLocations() = CourtLocationResponse(courtLocations = courtService.getCourtLocations())

  @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/add-video-link-appointment"])
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "Create video booking appointment")
  fun createVideoLinkAppointment(@RequestBody @Valid createVideoLinkAppointment: CreateVideoLinkAppointment) =
    courtService.createVideoLinkAppointment(createVideoLinkAppointment)

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
  fun getVideoLinkBooking(@ApiParam(
    value = "Video link booking id",
    required = true
  ) @PathVariable("videoBookingId") videoBookingId: Long) = courtService.getVideoLinkBooking(videoBookingId)

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-bookings"])
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "Create a Video Link Booking")
  fun createVideoLinkBooking(@RequestBody @Valid videoLinkBookingSpecification: VideoLinkBookingSpecification) =
    courtService.createVideoLinkBooking(videoLinkBookingSpecification)

  @PostMapping(path = ["/appointment-linker"])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiOperation(value = "Create Video Link Bookings for dangling Video Link Appointments")
  fun linkDanglingAppointments(@RequestBody chunkSize: Int?) {
    appointmentLinker.linkAppointments(chunkSize)
  }

  @DeleteMapping(path = ["/video-link-bookings/{videoBookingId}"])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiOperation(value = "Delete a Video Link Booking")
  fun deleteVideoLinkBooking(@ApiParam(
    value = "Video link booking id",
    required = true
  ) @PathVariable("videoBookingId") videoBookingId: Long) = courtService.deleteVideoLinkBooking(videoBookingId)
}
