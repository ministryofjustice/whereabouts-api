package uk.gov.justice.digital.hmpps.whereabouts.services

import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup
import java.util.function.Predicate

@Service("defaultLocationGroupService")
class LocationGroupFromEliteService (private val elite2ApiService: Elite2ApiService) : LocationGroupService {

  override fun getLocationGroupsForAgency(agencyId: String): List<LocationGroup> =getLocationGroups(agencyId)

  override fun getLocationGroups(agencyId: String): List<LocationGroup> = elite2ApiService.getLocationGroups(agencyId)

  override fun locationGroupFilter(agencyId: String, groupName: String): Predicate<Location> {
    val prefixToMatch = "$agencyId-${groupName.replace('_', '-')}-"
    return Predicate { (_, _, _, _, _, _, _, locationPrefix) -> locationPrefix.startsWith(prefixToMatch) }
  }

}