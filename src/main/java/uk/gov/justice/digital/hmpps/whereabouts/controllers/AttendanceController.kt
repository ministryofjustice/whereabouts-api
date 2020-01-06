package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import lombok.extern.slf4j.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendAllDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.AttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.CreateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceExists
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceLocked
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceNotFound
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import javax.validation.Valid

@Api(tags = ["attendance"])
@RestController
@RequestMapping(value = ["attendance"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Slf4j
class AttendanceController(private val attendanceService: AttendanceService) {

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "Create new attendance", response = AttendAllDto::class, notes = "Stores new attendance record, posts attendance details back up to PNOMIS. IEP warnings are triggered when certain absence reasons are used.")
  fun postAttendance(
      @ApiParam(value = "Attendance details", required = true)
      @RequestBody
      @Valid
      attendance: CreateAttendanceDto): ResponseEntity<Any> {
    val createdAttendance: AttendanceDto =  try {
      attendanceService.createAttendance(attendance)
    } catch (e: AttendanceExists) {
      return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.TEXT_PLAIN).body("Attendance already exists")
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(createdAttendance)
  }

  @PutMapping(path = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
  @ApiOperation(value = "Updates existing attendance information", notes = "Updates the attendance record, posts attendance details back up to PNOMIS. IEP warnings are triggered when certain absence reasons are used.")
  fun putAttendance(@PathVariable("id") id: Long, @RequestBody @Valid attendance: UpdateAttendanceDto): ResponseEntity<Any> {
    try {
      attendanceService.updateAttendance(id, attendance)
    } catch (e: AttendanceNotFound) {
      return ResponseEntity.notFound().build()
    } catch (e: AttendanceLocked) {
      return ResponseEntity.badRequest().build()
    }

    return ResponseEntity.noContent().build()
  }
}
