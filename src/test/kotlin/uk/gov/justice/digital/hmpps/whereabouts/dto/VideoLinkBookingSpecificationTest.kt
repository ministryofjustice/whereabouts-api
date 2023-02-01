package uk.gov.justice.digital.hmpps.whereabouts.dto

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class VideoLinkBookingSpecificationTest {
  private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun `valid object`() {
    assertThat(validator.validate(validObject.copy())).isEmpty()
  }

  @Test
  fun `valid object with pre and post`() {
    assertThat(validator.validate(validObjectAllFields.copy())).isEmpty()
  }

  companion object {
    val validObject = VideoLinkBookingSpecification(
      bookingId = 10L,
      court = "The Court",
      courtId = null,
      madeByTheCourt = false,
      main = VideoLinkAppointmentSpecification(
        startTime = LocalDateTime.of(2021, 1, 1, 9, 0),
        endTime = LocalDateTime.of(2021, 1, 1, 9, 30),
        locationId = 1L,
      )
    )

    val validObjectAllFields = validObject.copy(
      courtId = "CRT",
      pre = VideoLinkAppointmentSpecification(
        startTime = LocalDateTime.of(2021, 1, 1, 8, 45),
        endTime = LocalDateTime.of(2021, 1, 1, 9, 0),
        locationId = 2L,
      ),
      post = VideoLinkAppointmentSpecification(
        startTime = LocalDateTime.of(2021, 1, 1, 9, 30),
        endTime = LocalDateTime.of(2021, 1, 1, 9, 45),
        locationId = 3L,
      )
    )
  }
}
