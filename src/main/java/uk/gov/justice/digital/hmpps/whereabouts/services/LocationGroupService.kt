package uk.gov.justice.digital.hmpps.whereabouts.services;

import uk.gov.justice.digital.hmpps.whereabouts.model.Location;
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup;

import java.util.List;
import java.util.function.Predicate;

public interface LocationGroupService {

    List<LocationGroup> getLocationGroupsForAgency(String agencyId);

    List<LocationGroup> getLocationGroups(String agencyId);

    /**
     * Supply a filter predicate for LocationGroups.
     *
     * @param agencyId
     * @param groupName
     * @return a suitable predicate.
     */
    Predicate<Location> locationGroupFilter(String agencyId, String groupName);
}
