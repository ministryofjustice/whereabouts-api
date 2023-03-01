package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime

class LocationAndIntervalTest {
  private val locationAndInterval = LocationAndInterval(
    1,
    Interval(
      LocalTime.of(9, 0),
      LocalTime.of(10, 0),
    ),
  )

  @Test
  fun `shift by zero is identity`() {
    assertThat(locationAndInterval.shift(Duration.ZERO)).isEqualTo(locationAndInterval)
  }

  @Test
  fun `shift by positive duration moves forward through time`() {
    assertThat(locationAndInterval.shift(Duration.ofHours(1)))
      .extracting(
        "interval.start",
        "interval.end",
      )
      .containsExactly(
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
      )
  }
}
