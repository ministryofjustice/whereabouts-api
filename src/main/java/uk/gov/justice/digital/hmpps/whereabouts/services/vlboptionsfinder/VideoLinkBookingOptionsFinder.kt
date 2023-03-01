package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentSearchDto
import java.time.LocalTime
import java.util.TreeMap

sealed class Event(val time: LocalTime)
class StartEvent(time: LocalTime) : Event(time)
class EndEvent(time: LocalTime) : Event(time)

/**
 * Takes a list of Events representing the start and end of appointments during a day.
 * Uses this information to answer the question 'is the Timeline available (free of appointments) during this interval'
 * (isFreeForInterval(interval: Interval) below.
 */
class Timeline(events: List<Event>) {

  /*
    The periods of time when there are no appointments.
    The keys are the times at which an empty period started
    while the values are the corresponding times when the empty period ended.
   */
  private val emptyPeriods = TreeMap<LocalTime, LocalTime>()

  init {
    // The number of bookings in this room.
    var currentBookings = 0

    // The time of the previous event
    var previousEventTime: LocalTime = LocalTime.MIN

    // The time when the room last became unoccupied.
    // This will be null when the room is currently occupied.
    var freePeriodStarted: LocalTime? = LocalTime.MIN

    fun updateStateForPreviousEventTime() {
      when (currentBookings) {
        0 -> {
          // The room became unoccupied or continued to be unoccupied
          if (freePeriodStarted == null) {
            // The room became unoccupied. Remember the time at which this happened
            freePeriodStarted = previousEventTime
          }
        }
        else -> {
          // The room became occupied or continued to be occupied at previousEventTime
          if (freePeriodStarted != null) {
            // The room became occupied at previousEventTime.  Record the empty period that ended at previousEventTime
            emptyPeriods[freePeriodStarted!!] = previousEventTime
            freePeriodStarted = null
          }
        }
      }
    }

    events
      .plus(StartEvent(LocalTime.MAX))
      .sortedBy { it.time }
      .forEach {
        if (it.time > previousEventTime) {
          /**
           * More than one event can occur at a given time:
           * One booking may end at the same time that another begins or there may be overlapping bookings.
           *
           * If the time of the current event is greater than the time of the previous event then
           * all events for that earlier time have been seen and we can make decisions about
           * that previousEventTime (not the time of the current event):
           */
          updateStateForPreviousEventTime()
        }
        // Now update state from the current event.
        currentBookings += when (it) {
          is StartEvent -> 1
          is EndEvent -> -1
        }
        previousEventTime = it.time
      }

    // Finally, handle the end of the day
    updateStateForPreviousEventTime()
  }

  /**
   * A List of pairs of start and end times representing the free periods.  Ordered by start time ascending.
   * Mainly used for testing
   */
  fun emptyPeriods() = emptyPeriods.navigableKeySet().map {
    Pair(it, emptyPeriods[it])
  }

  /**
   * Answer the question 'is this Timeline free of appointments during the specified interval'.
   */
  fun isFreeForInterval(interval: Interval): Boolean {
    if (interval.end.isBefore(interval.start)) {
      throw IllegalArgumentException("start must precede end")
    }
    val freePeriod = emptyPeriods.floorEntry(interval.start)
    return interval.end.isBefore(freePeriod.value) || interval.end == freePeriod.value
  }
}

@Service
class VideoLinkBookingOptionsFinder(
  private val optionsGenerator: OptionsGenerator,

  @Value("\${video-link-booking.booking-options-finder.max-alternatives}")
  private val maxAlternatives: Int
) {

  fun findOptions(
    specification: VideoLinkBookingSearchSpecification,
    scheduledAppointments: List<ScheduledAppointmentSearchDto>
  ): VideoLinkBookingOptions {
    val timelinesByLocationId = timelinesByLocationId(scheduledAppointments)

    val preferredOption = VideoLinkBookingOption.from(specification)

    if (optionIsBookable(preferredOption, timelinesByLocationId)) {
      return VideoLinkBookingOptions(matched = true, alternatives = emptyList())
    }

    val alternatives = optionsGenerator
      .getOptionsInPreferredOrder(preferredOption)
      .filter { optionIsBookable(it, timelinesByLocationId) }
      .take(maxAlternatives)
      .sortedBy { it.main.interval.start }

    return VideoLinkBookingOptions(matched = false, alternatives = alternatives.toList())
  }

  companion object {

    fun optionIsBookable(
      option: VideoLinkBookingOption,
      timelinesByLocationId: Map<Long, Timeline>
    ) =
      option
        .toLocationsAndIntervals()
        // An absent Timeline represents a location that has no appointments; the location must be free.
        .all { timelinesByLocationId[it.locationId]?.isFreeForInterval(it.interval) ?: true }

    fun timelinesByLocationId(scheduledAppointments: List<ScheduledAppointmentSearchDto>): Map<Long, Timeline> =
      scheduledAppointments
        .groupBy { it.locationId }
        .mapValues { (_, scheduledAppointments) -> scheduledAppointments.flatMap(::toEvents) }
        .mapValues { Timeline(it.value) }

    private fun toEvents(appointment: ScheduledAppointmentSearchDto) =
      if (appointment.endTime == null) {
        emptyList()
      } else {
        listOf(
          StartEvent(appointment.startTime.toLocalTime()),
          EndEvent(appointment.endTime.toLocalTime())
        )
      }
  }
}
