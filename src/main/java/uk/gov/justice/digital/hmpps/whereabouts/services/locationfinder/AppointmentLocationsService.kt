package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService

private fun toAppointmentIds(videoLinkBooking: VideoLinkBooking) = with(videoLinkBooking) {
  sequenceOf(main.appointmentId, post?.appointmentId, pre?.appointmentId).filterNotNull()
}

@Service
class AppointmentLocationsService(
  private val prisonApiService: PrisonApiService,
  private val appointmentLocationsFinderService: AppointmentLocationsFinderService,
  private val videoLinkBookingRepository: VideoLinkBookingRepository
) {
  fun findLocationsForAppointmentIntervals(specification: AppointmentLocationsSpecification): List<AvailableLocations> {
    val allLocations = fetchVideoLinkBookingLocations(specification)
    val excludedAppointmentIds = appointmentIdsFromVideoLinkBookingIds(specification.vlbIdsToExclude)
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

  private fun appointmentIdsFromVideoLinkBookingIds(videoLinkBookingIds: List<Long>): Set<Long> =
    if (videoLinkBookingIds.isEmpty())
      emptySet()
    else
      videoLinkBookingRepository
        .findAllById(videoLinkBookingIds)
        .asSequence()
        .flatMap(::toAppointmentIds)
        .toSet()

  private fun fetchVideoLinkBookingLocations(specification: AppointmentLocationsSpecification) =
    prisonApiService
      .getAgencyLocationsForTypeUnrestricted(specification.agencyId, "APP")
      .filter { it.locationType == "VIDE" }
      .map { LocationIdAndDescription(it.locationId, it.userDescription ?: it.description) }

  private fun fetchScheduledAppointments(specification: AppointmentLocationsSpecification) =
    prisonApiService
      .getScheduledAppointments(specification.agencyId, specification.date)
      .filter { it.endTime != null }

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
