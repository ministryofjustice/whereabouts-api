package uk.gov.justice.digital.hmpps.whereabouts.services;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.model.Location;
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup;

import java.util.List;
import java.util.function.Predicate;

@Service("locationGroupServiceSelector")
public class LocationGroupServiceSelector implements LocationGroupService {
    private final LocationGroupService defaultService;
    private final LocationGroupService overrideService;

    public LocationGroupServiceSelector(
            @Qualifier("defaultLocationGroupService") final LocationGroupService defaultService,
            @Qualifier("overrideLocationGroupService") final LocationGroupService overrideService) {
        this.defaultService = defaultService;
        this.overrideService = overrideService;
    }

    @Override
    public List<LocationGroup> getLocationGroupsForAgency(final String agencyId) {
        return getLocationGroups(agencyId);
    }

    @Override
    public List<LocationGroup> getLocationGroups(final String agencyId) {
        val groups = overrideService.getLocationGroups(agencyId);
        if (!groups.isEmpty()) {
            return groups;
        }
        return defaultService.getLocationGroups(agencyId);
    }

    @Override
    public Predicate<Location> locationGroupFilter(final String agencyId, final String groupName) {
        if (!overrideService.getLocationGroups(agencyId).isEmpty()) {
            return overrideService.locationGroupFilter(agencyId, groupName);
        }
        return defaultService.locationGroupFilter(agencyId, groupName);
    }
}
