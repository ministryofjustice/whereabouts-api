package uk.gov.justice.digital.hmpps.whereabouts.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceExists
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceLocked
import uk.gov.justice.digital.hmpps.whereabouts.services.InvalidCourtLocation
import uk.gov.justice.digital.hmpps.whereabouts.services.ValidationException
import javax.persistence.EntityNotFoundException

@RestControllerAdvice(basePackageClasses = [AttendanceController::class, AttendanceStatisticsController::class, AttendancesController::class, CourtController::class, AgencyController::class, LocationController::class, CellMoveController::class])
class ControllerAdvice {
  @ExceptionHandler(RestClientResponseException::class)
  fun handleException(e: RestClientResponseException): ResponseEntity<ByteArray> {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(e.rawStatusCode)
      .body(e.responseBodyAsByteArray)
  }

  @ExceptionHandler(RestClientException::class)
  fun handleException(e: RestClientException): ResponseEntity<ErrorResponse> {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
          .developerMessage(e.message)
          .build()
      )
  }

  @ExceptionHandler(WebClientResponseException::class)
  fun handleException(e: WebClientResponseException): ResponseEntity<ErrorResponse> {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
          .developerMessage(e.message)
          .build()
      )
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleException(e: AccessDeniedException?): ResponseEntity<ErrorResponse> {
    log.debug("Forbidden (403) returned", e)
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.FORBIDDEN.value())
          .build()
      )
  }

  @ExceptionHandler(AttendanceExists::class)
  fun handleAttendanceExists(e: AttendanceExists): ResponseEntity<ErrorResponse> {
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.CONFLICT.value())
          .userMessage(e.message)
          .developerMessage(e.message)
          .build()
      )
  }

  @ExceptionHandler(AttendanceLocked::class)
  fun handleAttendanceLocked(e: AttendanceLocked): ResponseEntity<ErrorResponse> {
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.BAD_REQUEST.value())
          .userMessage(e.message)
          .developerMessage(e.message)
          .build()
      )
  }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.BAD_REQUEST.value())
          .developerMessage(e.message)
          .build()
      )
  }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: Exception): ResponseEntity<ErrorResponse> {
    log.error("Validation exception", e)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.BAD_REQUEST.value())
          .userMessage(e.message)
          .developerMessage(e.message)
          .build()
      )
  }

  @ExceptionHandler(InvalidCourtLocation::class)
  fun handleValidationException(e: InvalidCourtLocation): ResponseEntity<ErrorResponse> {
    log.error("InvalidCourtLocation exception", e)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.BAD_REQUEST.value())
          .userMessage(e.message)
          .developerMessage(e.message)
          .build()
      )
  }

  @ExceptionHandler(EntityNotFoundException::class)
  fun handleNotFoundException(e: Exception): ResponseEntity<ErrorResponse> {
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.NOT_FOUND.value())
          .developerMessage(e.message)
          .build()
      )
  }

  companion object {
    val log = LoggerFactory.getLogger(this::class.java)
  }
}
