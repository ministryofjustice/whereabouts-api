package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import uk.gov.justice.digital.hmpps.whereabouts.model.CellWithAttributes
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import java.util.Properties
import java.util.function.Predicate
import javax.persistence.EntityNotFoundException

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
            capacity = 2
          ),
          CellWithAttributes(
            id = 2L,
            description = "LEI-1-2",
            userDescription = "Dormitory",
            noOfOccupants = 1,
            capacity = 2
          )
        )
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
            capacity = 2
          ),
          CellWithAttributes(
            id = 2L,
            description = "LEI-1-2",
            userDescription = "Dormitory",
            noOfOccupants = 1,
            capacity = 2
          )
        )
      )
    whenever(locationGroupService.locationGroupFilter("LEI", "myList"))
      .thenReturn(Predicate { false })

    val group = locationService.getCellsWithCapacityForGroup("LEI", "myList", null)

    assertThat(group).isEmpty()
  }

  @Test
  fun `should throw entity not found error when trying to load the location prefix`() {
    whenever(groupsProperties.getProperty(anyString())).thenReturn(null)

    Assertions.assertThrows(EntityNotFoundException::class.java) {
      locationService.getLocationPrefixFromGroup("XXX", "1")
    }
  }

  @Test
  fun `should return location prefix for group`() {
    whenever(groupsProperties.getProperty(anyString())).thenReturn("MDI-2-")

    val locationPrefixDto = locationService.getLocationPrefixFromGroup("MDI", "Houseblock 7")

    assertThat(locationPrefixDto.locationPrefix).isEqualTo("MDI-2-")
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
      locationType = ""
    )
  }
}
