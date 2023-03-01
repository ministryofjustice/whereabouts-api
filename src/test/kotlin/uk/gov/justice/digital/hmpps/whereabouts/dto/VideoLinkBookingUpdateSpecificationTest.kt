package uk.gov.justice.digital.hmpps.whereabouts.dto

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class VideoLinkBookingUpdateSpecificationTest {
  private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun `valid object`() {
    Assertions.assertThat(validator.validate(validObject.copy())).isEmpty()
  }

  @Test
  fun `absent pre and post is valid`() {
    Assertions.assertThat(
      validator.validate(validObject.copy(pre = null, post = null))
    ).isEmpty()
  }

  @Test
  fun `zero length courtId is invalid`() {
    Assertions.assertThat(
      validator.validate(validObject.copy(courtId = ""))
    ).hasSize(1)
  }

  companion object {
    val validObject = VideoLinkBookingUpdateSpecification(
      courtId = "CRT",
      pre = VideoLinkAppointmentSpecification(
        startTime = LocalDateTime.of(2021, 1, 1, 8, 45),
        endTime = LocalDateTime.of(2021, 1, 1, 9, 0),
        locationId = 1L
      ),
      main = VideoLinkAppointmentSpecification(
        startTime = LocalDateTime.of(2021, 1, 1, 9, 0),
        endTime = LocalDateTime.of(2021, 1, 1, 9, 30),
        locationId = 2L
      ),
      post = VideoLinkAppointmentSpecification(
        startTime = LocalDateTime.of(2021, 1, 1, 9, 30),
        endTime = LocalDateTime.of(2021, 1, 1, 9, 45),
        locationId = 3L
      )
    )
  }
}
