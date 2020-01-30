package uk.gov.justice.digital.hmpps.whereabouts.services;

import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Set;

public class WhereaboutsEnabledService {

    private final LocationGroupService service;
    private final Set<String> enabledAgencies;

    public WhereaboutsEnabledService(
            @Qualifier("overrideLocationGroupService") final LocationGroupService service,
            @Qualifier("whereaboutsEnabled") final Set<String> enabledAgencies) {
        this.service = service;
        this.enabledAgencies = enabledAgencies;
    }

    public boolean isEnabled(final String agencyId) {
        return !service.getLocationGroups(agencyId).isEmpty() || enabledAgencies.contains(agencyId);
    }
}
