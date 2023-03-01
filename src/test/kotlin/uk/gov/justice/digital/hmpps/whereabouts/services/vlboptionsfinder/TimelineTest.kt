package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.TimelineAssertions.Companion.givenTheseEvents
import java.time.LocalTime

class TimelineTest {
  @Nested
  inner class ConstructingTimelines {

    @Test
    fun `Can build a timeline from no events`() {
      givenTheseEvents()
        .expectTheseEmptyPeriods(
          period(LocalTime.MIN, LocalTime.MAX),
        )
    }

    @Test
    fun `Can build a timeline from a pair of events`() {
      givenTheseEvents(
        start(9, 0),
        end(10, 0),

      ).expectTheseEmptyPeriods(
        period(LocalTime.MIN, hm(9, 0)),
        period(hm(10, 0), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from multiple disjoint pairs of events`() {
      givenTheseEvents(
        start(9, 0),
        end(10, 0),
        start(11, 0),
        end(12, 0),
        start(16, 0),
        end(17, 0),
      ).expectTheseEmptyPeriods(
        period(LocalTime.MIN, hm(9, 0)),
        period(hm(10, 0), hm(11, 0)),
        period(hm(12, 0), hm(16, 0)),
        period(hm(17, 0), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from multiple adjacent pairs of events`() {
      givenTheseEvents(
        start(9, 0),
        end(10, 0),
        start(10, 0),
        end(11, 0),
        start(11, 0),
        end(12, 0),
      ).expectTheseEmptyPeriods(
        period(LocalTime.MIN, hm(9, 0)),
        period(hm(12, 0), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from two appointments one completely contained in the other`() {
      givenTheseEvents(
        start(9, 0),
        end(10, 0),
        start(9, 15),
        end(9, 45),
      ).expectTheseEmptyPeriods(
        period(LocalTime.MIN, hm(9, 0)),
        period(hm(10, 0), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from overlapping appointments`() {
      givenTheseEvents(
        start(9, 0),
        end(10, 0),
        start(9, 15),
        end(10, 15),
      ).expectTheseEmptyPeriods(
        period(LocalTime.MIN, hm(9, 0)),
        period(hm(10, 15), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from two identical appointments`() {
      givenTheseEvents(
        start(9, 0),
        end(10, 0),
        start(9, 0),
        end(10, 0),
      ).expectTheseEmptyPeriods(
        period(LocalTime.MIN, hm(9, 0)),
        period(hm(10, 0), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from appointments that end at the same time`() {
      givenTheseEvents(
        start(9, 0),
        end(10, 0),
        start(9, 30),
        end(10, 0),
      ).expectTheseEmptyPeriods(
        period(LocalTime.MIN, hm(9, 0)),
        period(hm(10, 0), LocalTime.MAX),
      )
    }

    @Test
    fun `Can build a timeline from appointments that start at the same time`() {
      givenTheseEvents(
        start(9, 0),
        end(9, 30),
        start(9, 0),
        end(10, 0),
      ).expectTheseEmptyPeriods(
        period(LocalTime.MIN, hm(9, 0)),
        period(hm(10, 0), LocalTime.MAX),
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
        ),
      ).emptyPeriods()
      assertThat(emptyPeriods).containsExactly(
        period(LocalTime.MIN, hm(9, 0)),
        period(hm(11, 0), LocalTime.MAX),
      )
    }
  }

  @Nested
  inner class TestingFreeTime {
    @Test
    fun `empty timeline is always free`() {
      givenTheseEvents()
        .thisPeriodCanBeBooked(LocalTime.MIN, LocalTime.MAX)
        .thisPeriodCanBeBooked(hm(9, 0), hm(10, 0))
    }

    @Test
    fun `free time is free`() {
      givenTheseEvents(
        start(9, 0),
        end(10, 0),
      )
        .thisPeriodCanBeBooked(LocalTime.MIN, hm(9, 0))
        .thisPeriodCanBeBooked(hm(10, 0), LocalTime.MAX)
    }

    @Test
    fun `A period fully within occupied time is unavailable`() {
      givenTheseEvents(
        start(9, 0),
        end(10, 0),
      )
        .thisPeriodCannotBeBooked(hm(9, 15), hm(9, 45))
    }

    @Test
    fun `A period that overlaps  occupied time is unavailable`() {
      givenTheseEvents(
        start(9, 0),
        end(10, 0),
      ).thisPeriodCannotBeBooked(hm(8, 45), hm(9, 15))
    }

    @Test
    fun `A period that fully encloses occupied time is unavailable`() {
      givenTheseEvents(start(9, 0), end(10, 0))
        .thisPeriodCannotBeBooked(hm(8, 45), hm(10, 15))
    }

    @Test
    fun `A period that encloses more than one period of occupied time is unavailable`() {
      givenTheseEvents(
        start(9, 0),
        end(10, 0),
        start(11, 0),
        end(12, 0),
      ).thisPeriodCannotBeBooked(hm(8, 45), hm(12, 15))
    }
  }

  companion object {
    fun hm(hour: Int, minute: Int) = LocalTime.of(hour, minute, 0)
    fun start(hour: Int, minute: Int) = StartEvent(hm(hour, minute))
    fun end(hour: Int, minute: Int) = EndEvent(hm(hour, minute))

    fun period(start: LocalTime, end: LocalTime) = Pair(start, end)
  }
}

class TimelineAssertions(events: List<Event>) {
  private val timeline: Timeline = Timeline(events)

  fun expectTheseEmptyPeriods(vararg periods: Pair<LocalTime, LocalTime>): TimelineAssertions {
    assertThat(timeline.emptyPeriods()).containsExactly(*periods)
    return this
  }

  fun thisPeriodCanBeBooked(start: LocalTime, end: LocalTime): TimelineAssertions {
    assertThat(timeline.isFreeForInterval(Interval(start, end))).isTrue
    return this
  }

  fun thisPeriodCannotBeBooked(start: LocalTime, end: LocalTime): TimelineAssertions {
    assertThat(timeline.isFreeForInterval(Interval(start, end))).isFalse
    return this
  }

  companion object {
    fun givenTheseEvents(vararg events: Event) = TimelineAssertions(listOf(*events))
  }
}
