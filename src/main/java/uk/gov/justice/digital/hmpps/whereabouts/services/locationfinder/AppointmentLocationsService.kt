package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingOptions
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingOptionsFinder
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingSearchSpecification

@Service
class AppointmentLocationsService(
  private val prisonApiService: PrisonApiService,
  private val appointmentLocationsFinderService: AppointmentLocationsFinderService,
  private val videoLinkBookingRepository: VideoLinkBookingRepository,
  private val videoLinkBookingOptionsFinder: VideoLinkBookingOptionsFinder
) {
  fun findLocationsForAppointmentIntervals(specification: AppointmentLocationsSpecification): List<AvailableLocations> {
    val allLocations = fetchVideoLinkBookingLocations(specification)
    val excludedAppointmentIds = appointmentIdsFromBookingIds(specification.vlbIdsToExclude)
    val scheduledAppointments = fetchScheduledAppointments(specification)
      .filterNot { excludedAppointmentIds.contains(it.id) }

    val locationsForAppointmentIntervals =
      appointmentLocationsFinderService.find(
        specification.appointmentIntervals,
        allLocations.map { it.locationId },
        scheduledAppointments
      )

    return toAvailableLocations(locationsForAppointmentIntervals, allLocations)
  }

  private fun appointmentIdsFromBookingIds(vlbIdsToExclude: List<Long>): Set<Long> =
    if (vlbIdsToExclude.isEmpty())
      emptySet()
    else
      videoLinkBookingRepository
        .findAllById(vlbIdsToExclude)
        .flatMap { booking -> booking.appointments.values.map { it.appointmentId } }
        .toSet()

  private fun fetchVideoLinkBookingLocations(specification: AppointmentLocationsSpecification) =
    allVideoLinkLocationsForAgency(specification.agencyId)

  fun allVideoLinkLocationsForAgency(agencyId: String): List<LocationIdAndDescription> =
    prisonApiService
      .getAgencyLocationsForTypeUnrestricted(agencyId, "APP")
      .filter { it.locationType == "VIDE" }
      .map { LocationIdAndDescription(it.locationId, it.userDescription ?: it.description) }

  private fun fetchScheduledAppointments(specification: AppointmentLocationsSpecification) =
    prisonApiService
      .getScheduledAppointments(specification.agencyId, specification.date)
      .filter { it.endTime != null }

  fun findVideoLinkBookingOptions(specification: VideoLinkBookingSearchSpecification): VideoLinkBookingOptions {
    val excludedAppointmentIds =
      specification.vlbIdToExclude?.let { appointmentIdsFromBookingIds(listOf(it)) } ?: emptyList()

    val locationIds = setOfNotNull(
      specification.preAppointment?.locationId,
      specification.mainAppointment.locationId,
      specification.postAppointment?.locationId
    )

    val scheduledAppointments =
      prisonApiService
        .getScheduledAppointments(specification.agencyId, specification.date).asSequence()
        .filter { it.endTime != null }
        .filter { locationIds.contains(it.locationId) }
        .filterNot { excludedAppointmentIds.contains(it.id) }

    return videoLinkBookingOptionsFinder.findOptions(specification, scheduledAppointments)
  }

  companion object {
    private fun toAvailableLocations(
      locationsForAppointmentIntervals: List<AppointmentIntervalLocations>,
      locations: List<LocationIdAndDescription>
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
