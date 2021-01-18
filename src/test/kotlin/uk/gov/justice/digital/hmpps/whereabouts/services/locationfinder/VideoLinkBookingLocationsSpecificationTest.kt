package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.Validation
import javax.validation.Validator

class VideoLinkBookingLocationsSpecificationTest {

  private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun `valid object`() {
    val spec = validVlbSpec.copy()

    assertThat(validator.validate(spec)).isEmpty()
  }

  @Test
  fun `invalid agencyId`() {
    val spec = validVlbSpec.copy(agencyId = "")

    assertThat(validator.validate(spec)).hasSize(1)
  }

  @Test
  fun `invalid intervals`() {
    val spec = validVlbSpec.copy(
      preInterval = Interval(T09_30, T09_00),
      mainInterval = Interval(T09_30, T09_00),
      postInterval = Interval(T09_30, T09_00)
    )

    assertThat(validator.validate(spec)).hasSize(3)
  }

  @Test
  fun `converts to AppointmentLocationsSpecification`() {
    assertThat(validVlbSpec.copy().toAppointmentLocationsSpecification())
      .isEqualTo(defaultAls.copy())
  }

  @Test
  fun `converts pre and main to AppointmentLocationsSpecification`() {
    assertThat(
      validVlbSpec.copy(
        preInterval = Interval(T09_00, T09_30)
      )
        .toAppointmentLocationsSpecification()
    )
      .isEqualTo(
        defaultAls.copy(
          appointmentIntervals = listOf(
            Interval(T09_00, T09_30),
            Interval(T09_30, T10_00)
          )
        )
      )
  }

  @Test
  fun `converts main and post to AppointmentLocationsSpecification`() {
    assertThat(validVlbSpec.copy(postInterval = Interval(T10_00, T10_30)).toAppointmentLocationsSpecification())
      .isEqualTo(
        defaultAls.copy(
          appointmentIntervals = listOf(
            Interval(T09_30, T10_00),
            Interval(T10_00, T10_30)
          )
        )
      )
  }

  @Test
  fun `converts pre and main and post to AppointmentLocationsSpecification`() {
    assertThat(
      validVlbSpec.copy(
        preInterval = Interval(T09_00, T09_30),
        postInterval = Interval(T10_00, T10_30)
      ).toAppointmentLocationsSpecification()
    )
      .isEqualTo(
        defaultAls.copy(
          appointmentIntervals = listOf(
            Interval(T09_00, T09_30),
            Interval(T09_30, T10_00),
            Interval(T10_00, T10_30)
          )
        )
      )
  }

  companion object {
    val T09_00: LocalTime = LocalTime.of(9, 0)
    val T09_30: LocalTime = LocalTime.of(9, 30)
    val T10_00: LocalTime = LocalTime.of(10, 0)
    val T10_30: LocalTime = LocalTime.of(10, 30)

    val aDate: LocalDate = LocalDate.of(2020, 1, 1)

    val validVlbSpec = VideoLinkBookingLocationsSpecification(
      date = aDate,
      agencyId = "WWI",
      vlbIdsToExclude = emptyList(),
      preInterval = null,
      mainInterval = Interval(T09_30, T10_00),
      postInterval = null
    )

    val defaultAls = AppointmentLocationsSpecification(
      date = aDate,
      agencyId = "WWI",
      vlbIdsToExclude = emptyList(),
      appointmentIntervals = listOf(Interval(T09_30, T10_00))
    )
  }
}
