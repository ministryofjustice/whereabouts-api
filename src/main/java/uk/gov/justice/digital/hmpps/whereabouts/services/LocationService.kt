package uk.gov.justice.digital.hmpps.whereabouts.services

import org.apache.commons.text.WordUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.CellWithAttributes
import uk.gov.justice.digital.hmpps.whereabouts.model.Location

@Service
class LocationService(
        private val prisonApiService: PrisonApiService,
        @Qualifier("locationGroupServiceSelector") private val locationGroupService: LocationGroupService) {

  fun getCellLocationsForGroup(agencyId: String, groupName: String): List<Location> =
      prisonApiService.getAgencyLocationsForType(agencyId, "CELL")
          .filter(locationGroupService.locationGroupFilter(agencyId, groupName)::test)
          .toMutableList()
          .map { it.copy(description = it.description.formatLocation()) }
          .toList()

  fun getCellsWithCapacityForGroup(agencyId: String, groupName: String, attribute: String?): List<CellWithAttributes> =
          prisonApiService.getCellsWithCapacity(agencyId, attribute)
                  .filter{ locationGroupService.locationGroupFilter(agencyId, groupName).test(it.mapToLocation(agencyId)) }
                  .toMutableList()
                  .toList()

  private fun String.formatLocation(): String =
      WordUtils.capitalizeFully(this)
          .replace(Regex("hmp|Hmp"), "HMP")
          .replace(Regex("yoi|Yoi"), "YOI")

  private fun CellWithAttributes.mapToLocation(agencyId: String): Location =
      Location(locationId=this.id,
      description = this.description,
      locationType = "CELL",
      agencyId = agencyId,
      currentOccupancy = this.noOfOccupants,
      operationalCapacity = this.capacity,
      internalLocationCode = "",
      locationPrefix = this.description)
}