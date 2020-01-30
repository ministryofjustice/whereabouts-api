package uk.gov.justice.digital.hmpps.whereabouts.services;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.model.Location;
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup;

import java.util.List;
import java.util.function.Predicate;

@Service("defaultLocationGroupService")
@Slf4j
public class LocationGroupFromEliteService implements LocationGroupService {

    private final Elite2ApiService elite2ApiService;

    LocationGroupFromEliteService(final Elite2ApiService elite2ApiService) {
        this.elite2ApiService = elite2ApiService;
    }

    @Override
    public List<LocationGroup> getLocationGroupsForAgency(final String agencyId) {
        return getLocationGroups(agencyId);
    }

    @Override
    public List<LocationGroup> getLocationGroups(final String agencyId) {
        return elite2ApiService.getLocationGroups(agencyId);
    }

    @Override
    public Predicate<Location> locationGroupFilter(final String agencyId, final String groupName) {
        val prefixToMatch = agencyId + '-' + groupName.replace('_', '-') + '-';
        return (Location location) -> location.getLocationPrefix().startsWith(prefixToMatch);
    }
}
