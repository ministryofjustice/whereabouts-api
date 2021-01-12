package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService

@Service
class AppointmentLocationsService(
  private val prisonApiService: PrisonApiService,
  private val appointmentLocationsFinderService: AppointmentLocationsFinderService,
  private val videoLinkBookingRepository: VideoLinkBookingRepository
) {
  fun findLocationsForAppointmentIntervals(specification: AppointmentLocationsSpecification): List<AvailableLocations> {
    val allLocations = fetchVideoLinkBookingLocations(specification)
    val excludedAppointmentIds = appointmentIdsFromBookingIds(specification.vlbIdsToExclude)
    val scheduledAppointments = fetchScheduledAppointments(specification)
      .filterNot { excludedAppointmentIds.contains(it.id) }

    val locationsForAppointmentIntervals =
      appointmentLocationsFinderService.find(specification, allLocations, scheduledAppointments)
    return toAvailableLocations(locationsForAppointmentIntervals, allLocations)
  }

  private fun appointmentIdsFromBookingIds(vlbIdsToExclude: List<Long>): Set<Long> =
    if (vlbIdsToExclude.isEmpty())
      emptySet()
    else
      videoLinkBookingRepository
        .findAllById(vlbIdsToExclude)
        .flatMap { booking ->
          val ids = mutableListOf(booking.main.appointmentId)
          booking.pre?.apply { ids.add(appointmentId) }
          booking.post?.apply { ids.add(appointmentId) }
          ids
        }.toSet()

  private fun fetchVideoLinkBookingLocations(specification: AppointmentLocationsSpecification) =
    prisonApiService
      .getAgencyLocationsForTypeUnrestricted(specification.agencyId, "APP")
      .filter { it.locationType == "VIDE" }
      .map { Location(it.locationId, it.description) }

  private fun fetchScheduledAppointments(specification: AppointmentLocationsSpecification) =
    prisonApiService
      .getScheduledAppointmentsByAgencyAndDate(specification.agencyId, specification.date)
      .filter { it.appointmentTypeCode == "VLB" }
      .filter { it.endTime != null }

  companion object {
    private fun toAvailableLocations(
      locationsForAppointmentIntervals: List<LocationsForAppointmentIntervals>,
      locations: List<Location>
    ): List<AvailableLocations> {
      val locationsById = locations.associateBy { it.locationId }
      return locationsForAppointmentIntervals
        .map { lfa ->
          AvailableLocations(
            lfa.appointmentInterval,
            lfa.locationIds!!.mapNotNull { locationId -> locationsById[locationId] }
          )
        }
    }
  }
}
