package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationIdAndDescription

@Service
class LocationService(
  private val prisonApiService: PrisonApiService,
) {

  fun getVideoLinkRoomsForPrison(agencyId: String): List<LocationIdAndDescription> =
    prisonApiService
      .getAgencyLocationsForTypeUnrestricted(agencyId, "APP")
      .filter { it.locationType == "VIDE" }
      .map { LocationIdAndDescription(it.locationId, it.userDescription ?: it.description) }

  fun getAllLocationsForPrison(agencyId: String): List<LocationIdAndDescription> =
    prisonApiService
      .getAgencyLocationsForTypeUnrestricted(agencyId, "APP")
      .map { LocationIdAndDescription(it.locationId, it.userDescription ?: it.description) }
}
