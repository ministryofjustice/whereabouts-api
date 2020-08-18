package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import java.util.function.Predicate

class LocationServiceTest {

  private val prisonApiService: PrisonApiService = mock()
  private val locationGroupService: LocationGroupService = mock()
  private val locationService = LocationService(prisonApiService, locationGroupService)

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

    val group = locationService.getCellLocationsForGroup("LEI", "mylist");

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

    val group = locationService.getCellLocationsForGroup("LEI", "mylist");

    assertThat(group).extracting("description").containsExactlyInAnyOrder("YOI Something", "HMP Something")
  }

  @Test
  fun `getCellLocationsForGroup - no cells match predicate - returns nothing`() {
    whenever(prisonApiService.getAgencyLocationsForType("LEI", "CELL"))
        .thenReturn(listOf(cell1, cell2, cell3, cell4))
    whenever(locationGroupService.locationGroupFilter("LEI", "mylist"))
        .thenReturn(Predicate { false })

    val group = locationService.getCellLocationsForGroup("LEI", "mylist");

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
        locationType = ""
    )
  }


}
