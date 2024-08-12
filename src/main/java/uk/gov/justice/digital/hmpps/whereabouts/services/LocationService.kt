package uk.gov.justice.digital.hmpps.whereabouts.services

import org.apache.commons.text.WordUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationIdAndDescription

@Service
class LocationService(
  private val prisonApiService: PrisonApiService,
  @Qualifier("locationGroupServiceSelector") private val locationGroupService: LocationGroupService,
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

  fun getCellLocationsForGroup(agencyId: String, groupName: String): List<Location> =
    prisonApiService.getAgencyLocationsForType(agencyId, "CELL")
      .filter(locationGroupService.locationGroupFilter(agencyId, groupName)::test)
      .toMutableList()
      .map { it.copy(description = it.description.formatLocation()) }
      .toList()

  private fun String.formatLocation(): String =
    WordUtils.capitalizeFully(this)
      .replace(Regex("hmp|Hmp"), "HMP")
      .replace(Regex("yoi|Yoi"), "YOI")
}
