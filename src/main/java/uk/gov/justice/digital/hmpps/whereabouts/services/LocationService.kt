package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationIdAndDescription
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationInsidePrisonIdAndDescription

@Service
class LocationService(
  private val prisonApiService: PrisonApiService,
  private val locationApiClient: LocationApiClient,
) {

  fun getVideoLinkRoomsForPrison(agencyId: String): List<LocationIdAndDescription> =
    prisonApiService
      .getAgencyLocationsForTypeUnrestricted(agencyId, "APP")
      .filter { it.locationType == "VIDE" }
      .map { LocationIdAndDescription(it.locationId, it.userDescription ?: it.description) }

  fun getVideoLinkRoomsForPrisonFromLocationApi(prisonId: String): List<LocationInsidePrisonIdAndDescription> =
    locationApiClient.getNonResidentialLocationForPrison(prisonId, "APPOINTMENT")
      .filter { it.leafLevel && it.locationType == "VIDEO_LINK" }
      .map { LocationInsidePrisonIdAndDescription(it.id, it.localName ?: it.pathHierarchy) }

  fun getAllLocationsForPrison(agencyId: String): List<LocationIdAndDescription> =
    prisonApiService
      .getAgencyLocationsForTypeUnrestricted(agencyId, "APP")
      .map { LocationIdAndDescription(it.locationId, it.userDescription ?: it.description) }
}
