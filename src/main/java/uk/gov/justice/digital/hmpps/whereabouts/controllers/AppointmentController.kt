package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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
import uk.gov.justice.digital.hmpps.whereabouts.dto.AppointmentDetailsDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAppointmentSpecification
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreatedAppointmentDetailsDto
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
      ),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class,
      )
    ]
  )
  fun getAppointment(@PathVariable("id") id: Long): AppointmentDetailsDto = appointmentService.getAppointment(id)

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(
    value = "Create an appointment",
    nickname = "createAppointment"
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 400,
        message = "Bad request",
        response = ErrorResponse::class
      ),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  fun createAppointment(@RequestBody createAppointmentSpecification: CreateAppointmentSpecification): CreatedAppointmentDetailsDto =
    appointmentService.createAppointment(createAppointmentSpecification)

  @DeleteMapping(path = ["/{id}"])
  @ApiOperation(
    value = "Delete an appointment",
    nickname = "deleteAppointment"
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 404,
        message = "Appointment not found.",
        response = ErrorResponse::class,
      ),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class,
      )
    ]
  )
  fun deleteAppointment(@PathVariable(value = "id") id: Long) = appointmentService.deleteAppointment(id)
}
