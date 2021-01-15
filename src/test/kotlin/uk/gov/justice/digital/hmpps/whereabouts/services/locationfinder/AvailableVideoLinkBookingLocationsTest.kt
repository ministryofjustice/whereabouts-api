package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalTime

class AvailableVideoLinkBookingLocationsTest {
  @Test
  fun `fromAvailableLocations no locations`() {
    assertThatThrownBy {
      AvailableVideoLinkBookingLocations.fromAvailableLocations(listOf(), false)
    }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `fromAvailableLocations 4 locations`() {
    assertThatThrownBy {
      AvailableVideoLinkBookingLocations.fromAvailableLocations(
        listOf(
          AvailableLocations(interval, listOf()),
          AvailableLocations(interval, listOf()),
          AvailableLocations(interval, listOf()),
          AvailableLocations(interval, listOf())
        ),
        false
      )
    }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `fromAvailableLocations 3 locations`() {
    assertThat(
      AvailableVideoLinkBookingLocations.fromAvailableLocations(
        listOf(
          AvailableLocations(interval, l1),
          AvailableLocations(interval, l2),
          AvailableLocations(interval, l3),
        ),
        false
      )
    ).isEqualTo(
      AvailableVideoLinkBookingLocations(pre = l1, main = l2, post = l3)
    )
  }

  @Test
  fun `fromAvailableLocations 2 locations pre no post`() {
    assertThat(
      AvailableVideoLinkBookingLocations.fromAvailableLocations(
        listOf(
          AvailableLocations(interval, l1),
          AvailableLocations(interval, l2),
        ),
        true
      )
    ).isEqualTo(
      AvailableVideoLinkBookingLocations(pre = l1, main = l2, post = null)
    )
  }

  @Test
  fun `fromAvailableLocations 2 locations post no pre`() {
    assertThat(
      AvailableVideoLinkBookingLocations.fromAvailableLocations(
        listOf(
          AvailableLocations(interval, l1),
          AvailableLocations(interval, l2),
        ),
        false
      )
    ).isEqualTo(
      AvailableVideoLinkBookingLocations(pre = null, main = l1, post = l2)
    )
  }

  companion object {
    val interval = Interval(LocalTime.of(10, 0), LocalTime.of(10, 30))

    val l1 = listOf(LocationIdAndDescription(1, "L1"))
    val l2 = listOf(LocationIdAndDescription(2, "L2"))
    val l3 = listOf(LocationIdAndDescription(3, "L3"))
  }
}
