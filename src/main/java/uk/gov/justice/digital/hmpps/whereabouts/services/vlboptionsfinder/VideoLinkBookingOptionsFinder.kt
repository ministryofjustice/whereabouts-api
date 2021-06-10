package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import java.time.LocalTime
import java.util.TreeMap

sealed class Event(val time: LocalTime)
class StartEvent(time: LocalTime) : Event(time)
class EndEvent(time: LocalTime) : Event(time)

class Timeline(events: List<Event>) {

  /*
    The periods of time when the room is empty.
    This is map representation where the keys are the times at which the room became empty
    and the values are the corresponding times when the room was next occupied.
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

    events
      .plus(StartEvent(LocalTime.MAX))
      .sortedBy { it.time }
      .forEach {
        if (it.time > previousEventTime) {
          /**
           * More than one event can occur at a given time - double bookings or
           * one booking ending at the same time as another begins.
           * We have moved on to the next event time so now we can look back
           * and find out how things changed at the previous event time.
           */
          if (currentBookings > 0) {
            // The room became occupied or continued to be occupied.
            if (freePeriodStarted != null) {
              // The room became occupied at the previous event time.
              // Record the empty period that ended at the previous event time
              emptyPeriods[freePeriodStarted!!] = previousEventTime
              freePeriodStarted = null
            }
          } else {
            /**
             * The room became unoccupied or continued to be unoccupied
             */
            if (freePeriodStarted == null) {
              /**
               * The room became unoccupied so remember the time at which this happened
               */
              freePeriodStarted = previousEventTime
            }
          }
        }
        currentBookings += when (it) {
          is StartEvent -> 1
          is EndEvent -> -1
        }
        previousEventTime = it.time
      }
    /**
     * End of the day.
     */
    if (currentBookings > 0) {
      if (freePeriodStarted != null) {
        emptyPeriods[freePeriodStarted!!] = previousEventTime
        freePeriodStarted = null
      }
    }
  }

  /**
   * A List of pairs of start and end times when the room is free ordered by start time ascending.
   * Mostly used for testing
   */
  fun emptyPeriods() = emptyPeriods.navigableKeySet().map {
    Pair(it, emptyPeriods[it])
  }

  fun isFreeForPeriod(start: LocalTime, end: LocalTime): Boolean {
    if (end.isBefore(start)) {
      throw IllegalArgumentException("start must precede end")
    }
    val freePeriod = emptyPeriods.floorEntry(start)
    return end.isBefore(freePeriod.value) || end == freePeriod.value
  }
}

@Service
class VideoLinkBookingOptionsFinder {
  fun findOptions(
    specification: VideoLinkBookingSearchSpecification,
    scheduledAppointments: List<ScheduledAppointmentDto>
  ): VideoLinkBookingOptions {

    val eventsByLocationId: Map<Long, List<Event>> =
      scheduledAppointments
        .groupBy { it.locationId }
        .mapValues { (k, v) ->
          v.flatMap {
            if (it.endTime == null)
              emptyList()
            else
              listOf(
                StartEvent(it.startTime.toLocalTime()),
                EndEvent(it.endTime.toLocalTime())
              )
          }
        }
    return VideoLinkBookingOptions(matched = true, alternatives = emptyList())
  }
}
