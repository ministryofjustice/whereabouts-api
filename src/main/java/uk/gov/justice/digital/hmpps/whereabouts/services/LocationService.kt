package uk.gov.justice.digital.hmpps.whereabouts.services

import jakarta.persistence.EntityNotFoundException
import org.apache.commons.text.WordUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.LocationPrefixDto
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationIdAndDescription
import java.util.Properties

@Service
class LocationService(
  private val prisonApiService: PrisonApiService,
  @Qualifier("locationGroupServiceSelector") private val locationGroupService: LocationGroupService,
  @Qualifier("whereaboutsGroups") private val groupsProperties: Properties,
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

  fun getLocationPrefixFromGroup(agencyId: String, group: String): LocationPrefixDto {
    val agencyGroupKey = "${agencyId}_$group"
    val pattern = groupsProperties.getProperty(agencyGroupKey)
      ?: throw EntityNotFoundException("No mappings found for $agencyGroupKey")

    val locationPrefix = pattern
      .replace(".", "")
      .replace("+", "")

    return LocationPrefixDto(locationPrefix)
  }

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
