package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.CreateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import javax.validation.Valid

@Tag(name = "attendance")
@RestController
@RequestMapping(value = ["attendance"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AttendanceController(private val attendanceService: AttendanceService) {

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Create new attendance",
    description = "Stores new attendance record, posts attendance details back up to PNOMIS. IEP warnings are triggered when certain absence reasons are used."
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class),
      ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse::class),
      ApiResponse(code = 406, message = "Conflict creating an attendance.", response = ErrorResponse::class),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  fun postAttendance(
    @Parameter(name = "Attendance details", required = true)
    @RequestBody
    @Valid
    attendance: CreateAttendanceDto
  ): AttendanceDto = attendanceService.createAttendance(attendance)

  @PutMapping(path = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
  @Operation(
    summary = "Updates existing attendance information",
    description = "Updates the attendance record, posts attendance details back up to PNOMIS. IEP warnings are triggered when certain absence reasons are used."
  )
  @ApiResponses(
    value = [
      ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse::class),
      ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse::class),
      ApiResponse(
        code = 500,
        message = "Unrecoverable error occurred whilst processing request.",
        response = ErrorResponse::class
      )
    ]
  )
  fun putAttendance(
    @PathVariable("id") id: Long,
    @RequestBody @Valid attendance: UpdateAttendanceDto
  ): ResponseEntity<Any> {
    attendanceService.updateAttendance(id, attendance)
    return ResponseEntity.noContent().build()
  }
}
