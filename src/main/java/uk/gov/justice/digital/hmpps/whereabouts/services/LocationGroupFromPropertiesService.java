package uk.gov.justice.digital.hmpps.whereabouts.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.whereabouts.model.Location;
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An implementation of LocationGroupService backed by a properties file.
 */
@Service("overrideLocationGroupService")
@Slf4j
public class LocationGroupFromPropertiesService implements LocationGroupService {

    private final Properties groupsProperties;

    public LocationGroupFromPropertiesService(@Qualifier("whereaboutsGroups") final Properties groupsProperties) {
        this.groupsProperties = groupsProperties;
    }

    @Override
    public List<LocationGroup> getLocationGroupsForAgency(final String agencyId) {
        return getLocationGroups(agencyId);
    }

    /**
     * Return the set of Location Groups for an agency, including any nested sub-groups.
     *
     * @param agencyId The agency identifier
     * @return A list of LocationGroup, sorted by name, with each item containing its nested LocationGroups, also sorted by name.
     */
    @Override
    public List<LocationGroup> getLocationGroups(final String agencyId) {
        final var fullKeys = groupsProperties.stringPropertyNames();

        return fullKeys.stream()
                .filter(key -> key.startsWith(agencyId))
                .map(key -> key.substring(agencyId.length() + 1))
                .filter(key -> !key.contains("_"))
                .sorted()
                .map(key -> new LocationGroup(key, key, getAvailableSubGroups(agencyId, key)))
                .collect(Collectors.toList());
    }

    /**
     * Get the available sub-groups (sub-locations) for the named group/agency.
     *
     * @param agencyId  The agency identifier
     * @param groupName The  name of a group
     * @return Alphabetically sorted List of subgroups matching the criteria
     */
    private List<LocationGroup> getAvailableSubGroups(final String agencyId, final String groupName) {

        final var fullKeys = groupsProperties.stringPropertyNames();

        final var agencyAndGroupName = agencyId + '_' + groupName + '_';

        return fullKeys.stream()
                .filter(key -> key.startsWith(agencyAndGroupName))
                .map(key -> key.substring(agencyAndGroupName.length()))
                .sorted()
                .map(key -> new LocationGroup(key, key, Collections.emptyList()))
                .collect(Collectors.toList());
    }

    @Override
    public Predicate<Location> locationGroupFilter(final String agencyId, final String groupName) {
        final var patterns = groupsProperties.getProperty(agencyId + '_' + groupName);
        if (patterns == null) {
            throw new EntityNotFoundException(
                    "Group '" + groupName + "' does not exist for agencyId '" + agencyId + "'.");
        }
        final var patternStrings = patterns.split(",");

        return Arrays.stream(patternStrings)
                .map(Pattern::compile)
                .map(pattern -> (Predicate<Location>) l -> pattern.matcher(l.getLocationPrefix()).matches())
                .reduce(Predicate::or)
                .orElseThrow();
    }

}
