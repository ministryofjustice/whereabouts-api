package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import java.time.LocalTime

private abstract class Event(val locationId: Long, val time: LocalTime) {
  infix fun happensAfter(otherTime: LocalTime) = time > otherTime
  infix fun happensBefore(otherTime: LocalTime) = time < otherTime
  abstract fun isStart(): Boolean
  abstract fun isEnd(): Boolean
  abstract fun updateStartCount(currentCount: Long?): Long
}

private class StartEvent(locationId: Long, time: LocalTime) : Event(locationId, time) {
  override fun isStart() = true
  override fun isEnd() = false
  override fun updateStartCount(currentCount: Long?) = (currentCount ?: 0L) + 1L
}

private class EndEvent(locationId: Long, time: LocalTime) : Event(locationId, time) {
  override fun isStart() = false
  override fun isEnd() = true
  override fun updateStartCount(currentCount: Long?) = (currentCount ?: 0L) - 1L
}

@Service
class AppointmentLocationsFinderService {
  /**
   * Determine the set of available locations for each appointmentInterval.  The returned list
   * has the same order as the list of appointmentIntervals from which the finder was constructed.
   */
  fun find(
    appointmentIntervals: List<Interval>,
    locationIds: List<Long>,
    scheduledAppointments: List<ScheduledAppointmentDto>
  ): List<AppointmentIntervalLocations> {
    /**
     * Convert each scheduled appointment to a pair of START and END events, then put into a list sorted by the event's time.
     * The finder walks this list from earliest event to latest (ie from first to last item) updating the locationOccupancy.
     */
    val events = scheduledAppointments
      .flatMap {
        listOf(
          StartEvent(it.locationId, it.startTime.toLocalTime()),
          EndEvent(it.locationId, it.endTime!!.toLocalTime())
        )
      }
      .sortedBy { it.time }

    /**
     * Keep a running tally of the number of appointments at each location.
     * This will continue to work when there are overlapping appointments.
     * Appointments (a combination of an interval and a room) should never overlap, but this cannot be guaranteed.
     */
    val locationOccupancy = locationIds.map { it to 0L }.toMap().toMutableMap()

    /**
     * The set of locations that may be booked for each appointment interval.
     * Initially the set for each location is null.
     * When the finder reaches the first event that occurs after an appointment interval's start time the set of
     * locations that have an occupancy of 0 are currently unoccupied and are copied from locationOccupancy to the
     * LocationsForAppointment.
     * Any location subsequently found to have a BEGIN event before the appointmentInterval's END time is
     * removed from the set.
     */
    val locationsForAppointmentIntervals = appointmentIntervals.map { AppointmentIntervalLocations(it, null) }

    /**
     * Retrieve the set of locations that are currently unoccupied from the locationOccupancy map.
     */
    fun unoccupiedLocations(): MutableSet<Long> = locationOccupancy.filterValues { it == 0L }.keys.toMutableSet()

    events.forEach { event ->

      locationsForAppointmentIntervals.forEach { lfa ->
        if (event happensAfter lfa.appointmentInterval.start) {
          if (lfa.locationIds == null) {
            lfa.locationIds = unoccupiedLocations()
          }
          if (event.isStart() && event happensBefore lfa.appointmentInterval.end) {
            lfa.locationIds?.remove(event.locationId)
          }
        }
      }

      locationOccupancy.compute(event.locationId) { _, value -> event.updateStartCount(value) }
    }

    /**
     * If no event time was later than an appointmentInterval's start time then the initial unoccupied locations
     * snapshot didn't happen. In this case set the appointment interval's locations to the current set of unoccupied
     * locations.
     */
    locationsForAppointmentIntervals.forEach {
      if (it.locationIds == null) {
        it.locationIds = unoccupiedLocations()
      }
    }
    return locationsForAppointmentIntervals
  }
}
