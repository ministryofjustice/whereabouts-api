package uk.gov.justice.digital.hmpps.whereabouts.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.whereabouts.dto.ErrorResponse
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceExists
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceLocked
import uk.gov.justice.digital.hmpps.whereabouts.services.InvalidCourtLocation
import uk.gov.justice.digital.hmpps.whereabouts.services.ValidationException
import javax.persistence.EntityNotFoundException

@RestControllerAdvice
class ControllerAdvice {
  @ExceptionHandler(RestClientResponseException::class)
  fun handleException(e: RestClientResponseException): ResponseEntity<ByteArray> {
    log.error("Unexpected exception {}", e.message)
    return ResponseEntity
      .status(e.rawStatusCode)
      .body(e.responseBodyAsByteArray)
  }

  @ExceptionHandler(RestClientException::class)
  fun handleException(e: RestClientException): ResponseEntity<ErrorResponse> = handleServerError(e)

  @ExceptionHandler(WebClientResponseException.Unauthorized::class)
  fun handleWebClientUnAuthorised(e: Exception): ResponseEntity<ErrorResponse> {
    log.debug("Unauthorised (401) returned {}", e.message)
    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.UNAUTHORIZED.value())
          .build()
      )
  }

  @ExceptionHandler(WebClientResponseException.Forbidden::class)
  fun handleWebClientForbidden(e: Exception): ResponseEntity<ErrorResponse> {
    log.debug("Forbidden (403) returned {}", e.message)
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.FORBIDDEN.value())
          .build()
      )
  }

  @ExceptionHandler(WebClientResponseException::class)
  fun handleException(e: WebClientResponseException): ResponseEntity<ErrorResponse> {
    if (e.statusCode.value() == 401) return handleWebClientUnAuthorised(e)
    if (e.statusCode.value() == 403) return handleWebClientForbidden(e)

    return handleServerError(e)
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleException(e: AccessDeniedException): ResponseEntity<ErrorResponse> = handleWebClientUnAuthorised(e)

  @ExceptionHandler(AttendanceExists::class)
  fun handleAttendanceExists(e: AttendanceExists): ResponseEntity<ErrorResponse> {
    log.debug("Attendance already exists exception {}", e.message)
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
    log.debug("Attendance locked exception {}", e.message)
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

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
    log.debug("Required field missing {}", e.message)
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

  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun handleException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
    log.debug("Required field missing {}", e.message)
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

  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
    log.debug("Failed to map into body data structure {}", e.message)
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

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
    log.debug("Request parameters not valid {}", e.message)
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

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
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

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: Exception): ResponseEntity<ErrorResponse> {
    log.error("Validation exception {}", e.message)
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
    log.error("InvalidCourtLocation exception {}", e.message)
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
    log.debug("Not found (404) returned with message {}", e.message)
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

  fun handleServerError(e: Exception): ResponseEntity<ErrorResponse> {
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

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
