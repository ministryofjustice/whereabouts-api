package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalTime

class VideoLinkBookingOptionTest {
  private val mainOnly = VideoLinkBookingOption(
    main = LocationAndInterval(
      1L,
      Interval(
        LocalTime.of(10, 0),
        LocalTime.of(11, 0)
      )
    )
  )

  private val preMainAndPost = VideoLinkBookingOption(
    pre = LocationAndInterval(
      1L,
      Interval(
        LocalTime.of(9, 45),
        LocalTime.of(10, 0)
      )
    ),
    main = LocationAndInterval(
      1L,
      Interval(
        LocalTime.of(10, 0),
        LocalTime.of(10, 30)
      )
    ),
    post = LocationAndInterval(
      1L,
      Interval(
        LocalTime.of(10, 30),
        LocalTime.of(10, 45)
      )
    )
  )

  @Test
  fun `earliest start time, main only`() {
    assertThat(mainOnly.earliestStartTime()).isEqualTo(LocalTime.of(10, 0))
  }

  @Test
  fun `earliest start time, pre, main and post`() {
    assertThat(preMainAndPost.earliestStartTime()).isEqualTo(LocalTime.of(9, 45))
  }

  @Test
  fun `latest end time, main only`() {
    assertThat(mainOnly.latestEndTime()).isEqualTo(LocalTime.of(11, 0))
  }

  @Test
  fun `latest end time, pre, main and post`() {
    assertThat(preMainAndPost.latestEndTime()).isEqualTo(LocalTime.of(10, 45))
  }

  @Test
  fun `copy main only`() {
    assertThat(mainOnly.copyStartingAt(LocalTime.of(9, 0))).isEqualTo(
      VideoLinkBookingOption(
        main = LocationAndInterval(
          1L,
          Interval(
            LocalTime.of(9, 0),
            LocalTime.of(10, 0)
          )
        )
      )
    )
  }

  @Test
  fun `copy pre, main and post`() {
    assertThat(preMainAndPost.copyStartingAt(LocalTime.of(9, 0))).isEqualTo(
      VideoLinkBookingOption(
        pre = LocationAndInterval(
          1L,
          Interval(
            LocalTime.of(9, 0),
            LocalTime.of(9, 15)
          )
        ),
        main = LocationAndInterval(
          1L,
          Interval(
            LocalTime.of(9, 15),
            LocalTime.of(9, 45)
          )
        ),
        post = LocationAndInterval(
          1L,
          Interval(
            LocalTime.of(9, 45),
            LocalTime.of(10, 0)
          )
        )
      )
    )
  }
}
