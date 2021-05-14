package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDetailsDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.services.AppointmentService

@Api(tags = ["appointment"])
@RestController
@RequestMapping(value = ["appointment"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AppointmentController(private val appointmentService: AppointmentService) {

  @GetMapping(path = ["/{id}"])
  @ApiOperation(
    value = "Return appointment details",
    nickname = "getAppointment"
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = AppointmentDetailsDto::class),
      ApiResponse(
        code = 404,
        message = "Appointment not found.",
        response = ErrorResponse::class,
        responseContainer = "List"
      ),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class,
        responseContainer = "List"
      )
    ]
  )
  fun getAppointment(@PathVariable("id") id: Long): AppointmentDetailsDto = appointmentService.getAppointment(id)
}
