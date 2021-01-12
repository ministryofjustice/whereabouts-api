package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto

@Service
class AppointmentLocationsFinderService {
  fun find(
    specification: AppointmentLocationsSpecification,
    allLocations: List<Location>,
    scheduledAppointments: List<ScheduledAppointmentDto>
  ) =
    AppointmentLocationsFinder(
      specification.appointmentIntervals,
      allLocations.map { it.locationId },
      scheduledAppointments
    ).findLocationsForAppointmentIntervals()
}
