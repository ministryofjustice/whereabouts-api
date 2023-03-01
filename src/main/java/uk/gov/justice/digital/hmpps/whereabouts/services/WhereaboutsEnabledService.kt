package uk.gov.justice.digital.hmpps.whereabouts.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class WhereaboutsEnabledService(
  @Qualifier("overrideLocationGroupService") private val service: LocationGroupService,
  @Qualifier("whereaboutsEnabled") private val enabledAgencies: Set<String>,
) {

  fun isEnabled(agencyId: String): Boolean {
    return service.getLocationGroups(agencyId).isNotEmpty() || enabledAgencies.contains(agencyId)
  }
}
