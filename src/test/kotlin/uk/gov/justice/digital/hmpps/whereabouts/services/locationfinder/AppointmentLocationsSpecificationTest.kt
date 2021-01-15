package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.Validation
import javax.validation.Validator

class AppointmentLocationsSpecificationTest {

  private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun `valid object`() {
    val spec = AppointmentLocationsSpecification(
      date = aDate,
      agencyId = "WWI",
      vlbIdsToExclude = emptyList(),
      appointmentIntervals = listOf(Interval(T09_00, T09_30))
    )

    assertThat(validator.validate(spec)).isEmpty()
  }

  @Test
  fun `invalid agencyId`() {
    val spec = AppointmentLocationsSpecification(
      date = aDate,
      agencyId = "",
      vlbIdsToExclude = emptyList(),
      appointmentIntervals = listOf()
    )

    assertThat(validator.validate(spec)).hasSize(1)
  }

  @Test
  fun `invalid intervals`() {
    val spec = AppointmentLocationsSpecification(
      date = aDate,
      agencyId = "WWI",
      vlbIdsToExclude = emptyList(),
      appointmentIntervals = listOf(
        Interval(T09_00, T09_00),
        Interval(T09_30, T09_00)
      )
    )

    assertThat(validator.validate(spec))
      .hasSize(2)
      .extracting("message").contains("start must precede end")
  }

  companion object {
    val T09_00: LocalTime = LocalTime.of(9, 0)
    val T09_30: LocalTime = LocalTime.of(9, 30)

    val aDate: LocalDate = LocalDate.of(2020, 1, 1)
  }
}
