package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.whereabouts.dto.CourtLocationResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateVideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.dto.VideoLinkAppointmentsResponse
import uk.gov.justice.digital.hmpps.whereabouts.services.CourtService
import javax.validation.Valid

@Api(tags = ["court"])
@RestController
@RequestMapping(value = ["court"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CourtController(private val courtService: CourtService) {
  @Value("\${courts}")
  val courts: String? = null

  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/all-courts"])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "All court locations", response = CourtLocationResponse::class, notes = "Return all court locations")
  fun getCourtLocations() = CourtLocationResponse(courtLocations = courts?.split(",")!!.toSet())

  @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/add-video-link-appointment"])
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "Create video booking appointment")
  fun createVideoLinkAppointment(@RequestBody @Valid createVideoLinkAppointment: CreateVideoLinkAppointment) = courtService.createVideoLinkAppointment(createVideoLinkAppointment)

  @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/video-link-appointments"])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "Video link appointments", response = VideoLinkAppointmentsResponse::class, notes = "Return video link appointments")
  fun getVideoLinkAppointments(@RequestBody appointmentIds: Set<Long>) : VideoLinkAppointmentsResponse {
    val courtAppointments= courtService.getVideoLinkAppointments(appointmentIds)

    if (courtAppointments.isEmpty()) return VideoLinkAppointmentsResponse()

    return VideoLinkAppointmentsResponse(appointments = courtAppointments)
  }
}

