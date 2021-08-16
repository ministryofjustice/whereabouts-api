package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import javax.validation.Validation
import javax.validation.Validator

class VideoLinkBookingSearchSpecificationTest {
  private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun `valid object`() {
    assertThat(validator.validate(validSpec.copy())).isEmpty()
  }

  @Test
  fun `invalid agencyId`() {
    assertThat(validator.validate(validSpec.copy(agencyId = ""))).hasSize(1)
  }

  @Test
  fun `invalid intervals`() {
    assertThat(
      validator.validate(
        validSpec.copy(
          preAppointment = LocationAndInterval(1L, Interval(T09_30, T09_00)),
          mainAppointment = LocationAndInterval(2L, Interval(T10_00, T10_00)),
          postAppointment = LocationAndInterval(3L, Interval(T10_30, T09_00))
        )
      )
    ).hasSize(3)
  }

  @Test
  fun `absent pre and post appointments are valid`() {
    assertThat(
      validator.validate(
        validSpec.copy(
          preAppointment = null,
          postAppointment = null
        )
      )
    ).hasSize(0)
  }

  companion object {
    private val T09_00: LocalTime = LocalTime.of(9, 0)
    private val T09_30: LocalTime = LocalTime.of(9, 30)
    private val T10_00: LocalTime = LocalTime.of(10, 0)
    private val T10_30: LocalTime = LocalTime.of(10, 30)

    val validSpec = VideoLinkBookingSearchSpecification(
      agencyId = "WWI",
      date = LocalDate.of(2021, Month.MAY, 29),
      preAppointment = LocationAndInterval(1L, Interval(T09_00, T09_30)),
      mainAppointment = LocationAndInterval(1L, Interval(T09_30, T10_00)),
      postAppointment = LocationAndInterval(1L, Interval(T10_00, T10_30)),
      vlbIdToExclude = 1L
    )
  }
}
