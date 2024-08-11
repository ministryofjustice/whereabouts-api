package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.whereabouts.model.CellWithAttributes
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationIdAndDescription
import java.util.Properties
import java.util.function.Predicate

class LocationServiceTest {

  private val prisonApiService: PrisonApiService = mock()
  private val locationGroupService: LocationGroupService = mock()
  private val groupsProperties: Properties = mock()

  private val locationService = LocationService(prisonApiService, locationGroupService, groupsProperties)

  private val cell1 = aLocation(locationPrefix = "cell1")
  private val cell2 = aLocation(locationPrefix = "cell2")
  private val cell3 = aLocation(locationPrefix = "cell3")
  private val cell4 = aLocation(locationPrefix = "cell4")

  @Test
  fun `getVideoLinkRoomsForPrison - should return id and user-friendly location description but only for VIDE location types`() {
    val location1 = Location(
      locationId = 1, locationType = "VIDE", description = "video-room-a", locationUsage = "APP",
      agencyId = "MDI", parentLocationId = 123, currentOccupancy = 2, locationPrefix = "MDI-prefix",
      operationalCapacity = 2, userDescription = "Video Room A", internalLocationCode = "Room 1",
    )

    val location2 = Location(
      locationId = 2, locationType = "VIDE", description = "video-room-b", locationUsage = "APP",
      agencyId = "MDI", parentLocationId = 123, currentOccupancy = 2, locationPrefix = "MDI-prefix",
      operationalCapacity = 2, userDescription = "Video Room B", internalLocationCode = "Room 2",
    )

    val location3 = Location(
      locationId = 3, locationType = "MEETING ROOM", description = "video-room-c", locationUsage = "APP",
      agencyId = "MDI", parentLocationId = 123, currentOccupancy = 2, locationPrefix = "MDI-prefix",
      operationalCapacity = 2, userDescription = "Video Room C", internalLocationCode = "Room 3",
    )

    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted("MDI", "APP"))
      .thenReturn(listOf(location1, location2, location3))

    assertThat(locationService.getVideoLinkRoomsForPrison("MDI"))
      .isEqualTo(
        listOf(
          LocationIdAndDescription(locationId = 1, description = "Video Room A"),
          LocationIdAndDescription(locationId = 2, description = "Video Room B"),
        ),
      )
      .doesNotContain(LocationIdAndDescription(locationId = 3, description = "Video Room C"))
  }

  @Test
  fun `getVideoLinkRoomsForPrison - should return id plus hyphenated description rather than user-friendly description for location1`() {
    val location1 = Location(
      locationId = 1, locationType = "VIDE", description = "video-room-a", locationUsage = "APP",
      agencyId = "MDI", parentLocationId = 123, currentOccupancy = 2, locationPrefix = "MDI-prefix",
      operationalCapacity = 2, userDescription = null, internalLocationCode = "Room 1",
    )

    val location2 = Location(
      locationId = 2, locationType = "VIDE", description = "video-room-b", locationUsage = "APP",
      agencyId = "MDI", parentLocationId = 123, currentOccupancy = 2, locationPrefix = "MDI-prefix",
      operationalCapacity = 2, userDescription = "Video Room B", internalLocationCode = "Room 2",
    )

    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted("MDI", "APP"))
      .thenReturn(listOf(location1, location2))

    assertThat(locationService.getVideoLinkRoomsForPrison("MDI"))
      .isEqualTo(
        listOf(
          LocationIdAndDescription(locationId = 1, description = "video-room-a"),
          LocationIdAndDescription(locationId = 2, description = "Video Room B"),
        ),
      )
  }

  @Test
  fun `getAllLocationsForPrison - should return id and user-friendly location description for all location types`() {
    val location1 = Location(
      locationId = 1, locationType = "VIDE", description = "video-room-a", locationUsage = "APP",
      agencyId = "MDI", parentLocationId = 123, currentOccupancy = 2, locationPrefix = "MDI-prefix",
      operationalCapacity = 2, userDescription = "Video Room A", internalLocationCode = "Room 1",
    )

    val location2 = Location(
      locationId = 2, locationType = "VIDE", description = "video-room-b", locationUsage = "APP",
      agencyId = "MDI", parentLocationId = 123, currentOccupancy = 2, locationPrefix = "MDI-prefix",
      operationalCapacity = 2, userDescription = "Video Room B", internalLocationCode = "Room 2",
    )

    val location3 = Location(
      locationId = 3, locationType = "MEETING ROOM", description = "video-room-c", locationUsage = "APP",
      agencyId = "MDI", parentLocationId = 123, currentOccupancy = 2, locationPrefix = "MDI-prefix",
      operationalCapacity = 2, userDescription = "Video Room C", internalLocationCode = "Room 3",
    )

    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted("MDI", "APP"))
      .thenReturn(listOf(location1, location2, location3))

    assertThat(locationService.getAllLocationsForPrison("MDI"))
      .isEqualTo(
        listOf(
          LocationIdAndDescription(locationId = 1, description = "Video Room A"),
          LocationIdAndDescription(locationId = 2, description = "Video Room B"),
          LocationIdAndDescription(locationId = 3, description = "Video Room C"),
        ),
      )
  }

  @Test
  fun `getAllLocationsForPrison - should return id plus hyphenated description rather than user-friendly description for location1`() {
    val location1 = Location(
      locationId = 1, locationType = "VIDE", description = "video-room-a", locationUsage = "APP",
      agencyId = "MDI", parentLocationId = 123, currentOccupancy = 2, locationPrefix = "MDI-prefix",
      operationalCapacity = 2, userDescription = null, internalLocationCode = "Room 1",
    )

    val location2 = Location(
      locationId = 2, locationType = "VIDE", description = "video-room-b", locationUsage = "APP",
      agencyId = "MDI", parentLocationId = 123, currentOccupancy = 2, locationPrefix = "MDI-prefix",
      operationalCapacity = 2, userDescription = "Video Room B", internalLocationCode = "Room 2",
    )

    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted("MDI", "APP"))
      .thenReturn(listOf(location1, location2))

    assertThat(locationService.getAllLocationsForPrison("MDI"))
      .isEqualTo(
        listOf(
          LocationIdAndDescription(locationId = 1, description = "video-room-a"),
          LocationIdAndDescription(locationId = 2, description = "Video Room B"),
        ),
      )
  }

  @Test
  fun `getCellLocationsForGroup - cells match predicate - returns cells`() {
    whenever(prisonApiService.getAgencyLocationsForType("LEI", "CELL"))
      .thenReturn(listOf(cell1, cell2, cell3, cell4))
    whenever(locationGroupService.locationGroupFilter("LEI", "mylist"))
      .thenReturn(locationPrefixPredicate("cell4", "cell1", "cell3"))

    val group = locationService.getCellLocationsForGroup("LEI", "mylist")

    assertThat(group).containsExactlyInAnyOrder(cell1, cell3, cell4)
  }

  @Test
  fun `getCellLocationsForGroup - descriptions need formatting - formatted correctly`() {
    val cell5 = aLocation(locationPrefix = "cell5", description = "yoi something")
    val cell6 = aLocation(locationPrefix = "cell6", description = "hmp something")

    whenever(prisonApiService.getAgencyLocationsForType("LEI", "CELL"))
      .thenReturn(listOf(cell5, cell6))
    whenever(locationGroupService.locationGroupFilter("LEI", "mylist"))
      .thenReturn(Predicate { true })

    val group = locationService.getCellLocationsForGroup("LEI", "mylist")

    assertThat(group).extracting("description").containsExactlyInAnyOrder("YOI Something", "HMP Something")
  }

  @Test
  fun `getCellLocationsForGroup - no cells match predicate - returns nothing`() {
    whenever(prisonApiService.getAgencyLocationsForType("LEI", "CELL"))
      .thenReturn(listOf(cell1, cell2, cell3, cell4))
    whenever(locationGroupService.locationGroupFilter("LEI", "mylist"))
      .thenReturn(Predicate { false })

    val group = locationService.getCellLocationsForGroup("LEI", "mylist")

    assertThat(group).isEmpty()
  }

  @Test
  fun `getCellsWithCapacityForGroup - cells match predicate`() {
    whenever(prisonApiService.getCellsWithCapacity("LEI", null))
      .thenReturn(
        listOf(
          CellWithAttributes(
            id = 1L,
            description = "LEI-1-1",
            userDescription = "Dormitory",
            noOfOccupants = 1,
            capacity = 2,
          ),
          CellWithAttributes(
            id = 2L,
            description = "LEI-1-2",
            userDescription = "Dormitory",
            noOfOccupants = 1,
            capacity = 2,
          ),
        ),
      )
    whenever(locationGroupService.locationGroupFilter("LEI", "myList"))
      .thenReturn(locationPrefixPredicate("LEI-1-1", "LEI-1-2"))

    val group = locationService.getCellsWithCapacityForGroup("LEI", "myList", null)

    assertThat(group).extracting("description").containsExactlyInAnyOrder("LEI-1-1", "LEI-1-2")
  }

  @Test
  fun `getCellsWithCapacityForGroup - no cells match predicate`() {
    whenever(prisonApiService.getCellsWithCapacity("LEI", null))
      .thenReturn(
        listOf(
          CellWithAttributes(
            id = 1L,
            description = "LEI-1-1",
            userDescription = "Dormitory",
            noOfOccupants = 1,
            capacity = 2,
          ),
          CellWithAttributes(
            id = 2L,
            description = "LEI-1-2",
            userDescription = "Dormitory",
            noOfOccupants = 1,
            capacity = 2,
          ),
        ),
      )
    whenever(locationGroupService.locationGroupFilter("LEI", "myList"))
      .thenReturn(Predicate { false })

    val group = locationService.getCellsWithCapacityForGroup("LEI", "myList", null)

    assertThat(group).isEmpty()
  }

  private fun locationPrefixPredicate(vararg cells: String): Predicate<Location>? {
    return listOf(*cells)
      .map { s -> Predicate { l: Location -> s == l.locationPrefix } }
      .reduce(Predicate<Location>::or)
  }

  private fun aLocation(locationPrefix: String, description: String = ""): Location {
    return Location(
      locationPrefix = locationPrefix,
      locationId = 0L,
      description = description,
      parentLocationId = null,
      userDescription = null,
      currentOccupancy = 0,
      operationalCapacity = 0,
      agencyId = "",
      internalLocationCode = "",
      locationUsage = "",
      locationType = "",
    )
  }
}
