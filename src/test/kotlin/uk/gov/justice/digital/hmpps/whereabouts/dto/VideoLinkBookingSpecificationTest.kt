package uk.gov.justice.digital.hmpps.whereabouts.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import javax.validation.Validation
import javax.validation.Validator

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

  @Test
  fun `invalid pre, main and post locationIds`() {
    assertThat(
      validator.validate(
        validObjectAllFields.copy(
          main = validObjectAllFields.main.copy(locationId = null),
          pre = validObjectAllFields.pre!!.copy(locationId = null),
          post = validObjectAllFields.post!!.copy(locationId = null),

        )
      )
    ).hasSize(3)
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
