package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.CourtLocationResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateVideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentsResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkBookingSpecification
import uk.gov.justice.digital.hmpps.whereabouts.services.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.VideoLinkAppointmentLinker
import java.time.LocalDate
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
  fun getVideoLinkBooking(
    @ApiParam(value = "Video link booking id", required = true)
    @PathVariable("videoBookingId")
    videoBookingId: Long
  ) = courtService.getVideoLinkBooking(videoBookingId)

  @GetMapping(path = ["/video-link-bookings/date/{date}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation("Get all video link bookings for the specified date, optionally filtering by court.")
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
    return courtService.getVideoLinkBookingsForDateAndCourt(date, court)
  }

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
  fun deleteVideoLinkBooking(
    @ApiParam(value = "Video link booking id", required = true)
    @PathVariable("videoBookingId")
    videoBookingId: Long
  ) = courtService.deleteVideoLinkBooking(videoBookingId)
}
