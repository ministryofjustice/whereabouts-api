package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import java.time.LocalTime

private enum class EventType { START, END }

private data class Event(val locationId: Long, val time: LocalTime, val type: EventType)

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
  ): List<LocationsForAppointmentIntervals> {
    /**
     * Convert each scheduled appointment to a pair of START and END events, then put into a list sorted by the event's time.
     * The finder walks this list from earliest event to latest (ie from first to last item) updating the locationOccupancy.
     */
    val events = scheduledAppointments
      .flatMap {
        listOf(
          Event(it.locationId, it.startTime.toLocalTime(), EventType.START),
          Event(it.locationId, it.endTime!!.toLocalTime(), EventType.END)
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
     * locations that have an occupancy of 0 are copied from locationOccupancy to the LocationsForAppointment.
     * Any location that is subsequently found to have a BEGIN event before the appointmentInterval's END time is
     * removed from the set.
     */
    val locationsForAppointmentIntervals = appointmentIntervals.map { LocationsForAppointmentIntervals(it, null) }

    /**
     * Retrieve from the locationOccupancy map the set of locations that are currently unoccupied.
     */
    fun unoccupiedLocations(): MutableSet<Long> = locationOccupancy.filterValues { it == 0L }.keys.toMutableSet()

    events.forEach { event ->

      locationsForAppointmentIntervals.forEach { lfa ->
        if (event.time > lfa.appointmentInterval.start) {
          if (lfa.locationIds == null) {
            lfa.locationIds = unoccupiedLocations()
          }
          if (event.type == EventType.START && event.time < lfa.appointmentInterval.end) {
            lfa.locationIds?.remove(event.locationId)
          }
        }
      }

      locationOccupancy.compute(event.locationId) { _, value ->
        when (event.type) {
          EventType.START -> (value ?: 0L).inc()
          EventType.END -> (value ?: 0L).dec()
        }
      }
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
