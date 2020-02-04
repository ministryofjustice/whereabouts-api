package uk.gov.justice.digital.hmpps.whereabouts.services

import org.apache.commons.text.WordUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.Location

internal const val CELL = "CELL"

@Service
class LocationService(
    private val elite2ApiService: Elite2ApiService,
    @Qualifier("defaultLocationGroupService") private val locationGroupService: LocationGroupService) {

  fun getCellLocationsForGroup(agencyId: String, groupName: String): List<Location> =
      elite2ApiService.getAgencyLocationsForType(agencyId, CELL)
          .filter(locationGroupService.locationGroupFilter(agencyId, groupName)::test)
          .toMutableList()
          .map { it.copy(description = it.description.formatLocation()) }
          .toList()

  private fun String.formatLocation(): String =
      WordUtils.capitalizeFully(this)
          .replace(Regex("hmp|Hmp"), "HMP")
          .replace(Regex("yoi|Yoi"), "YOI")
}