package uk.gov.justice.digital.hmpps.whereabouts.services

import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup
import java.util.function.Predicate

interface LocationGroupService {
  fun getLocationGroupsForAgency(agencyId: String): List<LocationGroup>
  fun getLocationGroups(agencyId: String): List<LocationGroup>

  /**
   * Supply a filter predicate for LocationGroups.
   *
   * @param agencyId
   * @param groupName
   * @return a suitable predicate.
   */
  fun locationGroupFilter(agencyId: String, groupName: String): Predicate<Location>
}
