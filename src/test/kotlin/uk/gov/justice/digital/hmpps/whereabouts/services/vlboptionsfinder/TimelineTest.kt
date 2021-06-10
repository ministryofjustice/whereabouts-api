package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalTime

class TimelineTest {
  @Nested
  inner class ConstructingTimelines {
    @Test
    fun `Can build a timeline from no events`() {
      val emptyPeriods = Timeline(emptyList()).emptyPeriods()
      assertThat(emptyPeriods).containsExactly(Pair(LocalTime.MIN, LocalTime.MAX))
    }

    @Test
    fun `Can build a timeline from a pair of events`() {
      val emptyPeriods = Timeline(
        listOf(
          start(9, 0), end(10, 0)
        )
      ).emptyPeriods()
      assertThat(emptyPeriods).containsExactly(
        Pair(LocalTime.MIN, hm(9, 0)),
        Pair(hm(10, 0), LocalTime.MAX)
      )
    }

    @Test
    fun `Can build a timeline from multiple disjoint pairs of events`() {
      val emptyPeriods = Timeline(
        listOf(
          start(9, 0), end(10, 0),
          start(11, 0), end(12, 0),
          start(16, 0), end(17, 0)
        )
      ).emptyPeriods()
      assertThat(emptyPeriods).containsExactly(
        Pair(LocalTime.MIN, hm(9, 0)),
        Pair(hm(10, 0), hm(11, 0)),
        Pair(hm(12, 0), hm(16, 0)),
        Pair(hm(17, 0), LocalTime.MAX)
      )
    }

    @Test
    fun `Can build a timeline from multiple adjacent pairs of events`() {
      val emptyPeriods = Timeline(
        listOf(
          start(9, 0), end(10, 0),
          start(10, 0), end(11, 0),
          start(11, 0), end(12, 0)
        )
      ).emptyPeriods()
      assertThat(emptyPeriods).containsExactly(
        Pair(LocalTime.MIN, hm(9, 0)),
        Pair(hm(12, 0), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from two appointments one completely contained in the other`() {
      val emptyPeriods = Timeline(
        listOf(
          start(9, 0), end(10, 0),
          start(9, 15), end(9, 45),
        )
      ).emptyPeriods()
      assertThat(emptyPeriods).containsExactly(
        Pair(LocalTime.MIN, hm(9, 0)),
        Pair(hm(10, 0), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from overlapping appointments`() {
      val emptyPeriods = Timeline(
        listOf(
          start(9, 0), end(10, 0),
          start(9, 15), end(10, 15),
        )
      ).emptyPeriods()
      assertThat(emptyPeriods).containsExactly(
        Pair(LocalTime.MIN, hm(9, 0)),
        Pair(hm(10, 15), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from two identical appointments`() {
      val emptyPeriods = Timeline(
        listOf(
          start(9, 0), end(10, 0),
          start(9, 0), end(10, 0),
        )
      ).emptyPeriods()
      assertThat(emptyPeriods).containsExactly(
        Pair(LocalTime.MIN, hm(9, 0)),
        Pair(hm(10, 0), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from appointments that end at the same time`() {
      val emptyPeriods = Timeline(
        listOf(
          start(9, 0), end(10, 0),
          start(9, 30), end(10, 0),
        )
      ).emptyPeriods()
      assertThat(emptyPeriods).containsExactly(
        Pair(LocalTime.MIN, hm(9, 0)),
        Pair(hm(10, 0), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from appointments that start at the same time`() {
      val emptyPeriods = Timeline(
        listOf(
          start(9, 0), end(9, 30),
          start(9, 0), end(10, 0),
        )
      ).emptyPeriods()
      assertThat(emptyPeriods).containsExactly(
        Pair(LocalTime.MIN, hm(9, 0)),
        Pair(hm(10, 0), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from multiple overlapping and contiguous appointments`() {
      val emptyPeriods = Timeline(
        listOf(
          start(9, 0), end(9, 30),
          start(9, 0), end(10, 0),
          start(9, 30), end(10, 0),
          start(9, 30), end(10, 30),
          start(10, 30), end(11, 0),
        )
      ).emptyPeriods()
      assertThat(emptyPeriods).containsExactly(
        Pair(LocalTime.MIN, hm(9, 0)),
        Pair(hm(11, 0), LocalTime.MAX),
      )
    }
  }

  @Nested
  inner class TestingFreeTime {
    @Test
    fun `empty timeline is always free`() {
      assertThat(Timeline(emptyList()).isFreeForPeriod(LocalTime.MIN, LocalTime.MAX)).isTrue
      assertThat(Timeline(emptyList()).isFreeForPeriod(hm(9, 0), hm(10, 0))).isTrue
    }

    @Test
    fun `free time is free`() {
      val timeline = Timeline(listOf(start(9, 0), end(10, 0)))
      assertThat(timeline.isFreeForPeriod(LocalTime.MIN, hm(9, 0))).isTrue
      assertThat(timeline.isFreeForPeriod(hm(10, 0), LocalTime.MAX)).isTrue
    }

    @Test
    fun `A period fully within occupied time is unavailable`() {
      val timeline = Timeline(listOf(start(9, 0), end(10, 0)))
      assertThat(timeline.isFreeForPeriod(hm(9, 15), hm(9, 45))).isFalse
    }

    @Test
    fun `A period that overlaps  occupied time is unavailable`() {
      val timeline = Timeline(listOf(start(9, 0), end(10, 0)))
      assertThat(timeline.isFreeForPeriod(hm(8, 45), hm(9, 15))).isFalse
      assertThat(timeline.isFreeForPeriod(hm(9, 45), hm(10, 45))).isFalse
    }

    @Test
    fun `A period that fully encloses occupied time is unavailable`() {
      val timeline = Timeline(listOf(start(9, 0), end(10, 0)))
      assertThat(timeline.isFreeForPeriod(hm(8, 45), hm(10, 15))).isFalse
    }

    @Test
    fun `A period that encloses more than one period of occupied time is unavailable`() {
      val timeline = Timeline(
        listOf(
          start(9, 0), end(10, 0),
          start(11, 0), end(12, 0)
        )
      )
      assertThat(timeline.isFreeForPeriod(hm(8, 45), hm(12, 15))).isFalse
    }
  }

  companion object {
    fun hm(hour: Int, minute: Int) = LocalTime.of(hour, minute, 0)
    fun start(hour: Int, minute: Int) = StartEvent(hm(hour, minute))
    fun end(hour: Int, minute: Int) = EndEvent(hm(hour, minute))
  }
}
