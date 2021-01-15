package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalTime

class IntervalValidatorTest {
  private val validator = IntervalValidator()

  @Test
  fun `start precedes end is valid`() {
    assertThat(validator.isValid(Interval(LocalTime.of(0, 0), LocalTime.of(0, 1)), null)).isTrue
  }

  @Test
  fun `start == end not valid`() {
    assertThat(validator.isValid(Interval(LocalTime.of(1, 1), LocalTime.of(1, 1)), null)).isFalse
  }

  @Test
  fun `start after end not valid`() {
    assertThat(validator.isValid(Interval(LocalTime.of(1, 1), LocalTime.of(1, 0)), null)).isFalse
  }

  @Test
  fun `no interval not valid`() {
    assertThat(validator.isValid(null, null)).isTrue
  }
}
