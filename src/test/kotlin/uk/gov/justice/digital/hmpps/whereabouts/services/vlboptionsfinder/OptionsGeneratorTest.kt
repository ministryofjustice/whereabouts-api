package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime

class OptionsGeneratorTest {
  private val mainOnlyReference = VideoLinkBookingOption(
    main = LocationAndInterval(
      1L,
      Interval(LocalTime.of(7, 0), LocalTime.of(8, 0))
    )
  )

  @Test
  fun `generates option`() {
    val options = OptionsGenerator(
      LocalTime.of(9, 0),
      LocalTime.of(10, 0),
      Duration.ofMinutes(15)
    ).getOptionsInPreferredOrder(mainOnlyReference)
      .toList()

    assertThat(options).containsExactly(
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 0))
    )
  }

  @Test
  fun `generates multiple options from start to end times inclusive`() {
    val options = OptionsGenerator(
      LocalTime.of(9, 0),
      LocalTime.of(11, 0),
      Duration.ofMinutes(15)
    ).getOptionsInPreferredOrder(mainOnlyReference)
      .toList()

    assertThat(options).containsExactly(
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 0)),
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 15)),
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 30)),
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 45)),
      mainOnlyReference.copyStartingAt(LocalTime.of(10, 0)),
    )
  }

  @Test
  fun `options are in reverse order when preferred time is after end of day`() {
    val options = OptionsGenerator(
      LocalTime.of(9, 0),
      LocalTime.of(11, 0),
      Duration.ofMinutes(15)
    ).getOptionsInPreferredOrder(mainOnlyReference.copyStartingAt(LocalTime.of(17, 0)))
      .toList()

    assertThat(options).containsExactly(
      mainOnlyReference.copyStartingAt(LocalTime.of(10, 0)),
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 45)),
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 30)),
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 15)),
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 0)),
    )
  }

  @Test
  fun `options are sorted by proximity to preferred option`() {
    val options = OptionsGenerator(
      LocalTime.of(9, 0),
      LocalTime.of(11, 0),
      Duration.ofMinutes(15)
    ).getOptionsInPreferredOrder(mainOnlyReference.copyStartingAt(LocalTime.of(9, 20)))
      .toList()

    assertThat(options).containsExactly(
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 15)),
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 30)),
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 0)),
      mainOnlyReference.copyStartingAt(LocalTime.of(9, 45)),
      mainOnlyReference.copyStartingAt(LocalTime.of(10, 0)),
    )
  }
}
