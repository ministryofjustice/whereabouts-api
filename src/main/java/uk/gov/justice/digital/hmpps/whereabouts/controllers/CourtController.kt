package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.whereabouts.dto.CourtAppointmentsResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.CourtLocationResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateCourtAppointment
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

  @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/add-court-appointment"])
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "Create court video booking appointment")
  fun createCourtVideoLinkBooking(@RequestBody @Valid createCourtAppointment: CreateCourtAppointment) = courtService.addCourtAppointment(createCourtAppointment)

  @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/court-appointments"])
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "Court appointments", response = CourtAppointmentsResponse::class, notes = "Return court appointments")
  fun getCourtAppointments(@RequestBody appointmentIds: Set<Long>) : CourtAppointmentsResponse {
    val courtAppointments= courtService.getCourtAppointments(appointmentIds)

    if (courtAppointments.isEmpty()) return CourtAppointmentsResponse()

    return CourtAppointmentsResponse(appointments = courtAppointments)
  }
}

