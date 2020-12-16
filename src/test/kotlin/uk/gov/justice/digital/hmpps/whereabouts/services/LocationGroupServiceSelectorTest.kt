package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.model.LocationGroup

class LocationGroupServiceSelectorTest {

  private val defaultService: LocationGroupService = mock()
  private val overrideService: LocationGroupService = mock()
  private var service: LocationGroupService = LocationGroupServiceSelector(defaultService, overrideService)

  @Test
  fun locationGroupsCallsDefaultWhenNoOverride() {
    whenever(defaultService.getLocationGroups("LEI")).thenReturn(listOf(LG1))
    assertThat(service.getLocationGroups("LEI")).contains(LG1)
    verify(overrideService).getLocationGroups("LEI")
  }

  @Test
  fun locationGroupsDoesNotCallDefaultWhenOverridden() {
    whenever(overrideService.getLocationGroups("LEI")).thenReturn(listOf(LG1))
    assertThat(service.getLocationGroups("LEI")).contains(LG1)
    verify(overrideService).getLocationGroups("LEI")
    verifyNoMoreInteractions(defaultService)
  }

  @Test
  fun locationGroupsForAgencyDelegatesToGetLocationGroups() {
    whenever(defaultService.getLocationGroups("LEI")).thenReturn(listOf(LG1))
    assertThat(service.getLocationGroupsForAgency("LEI")).contains(LG1)
    verify(overrideService).getLocationGroups("LEI")
  }

  @Test
  fun locationGroupsFiltersCallsDefaultWhenNoOverride() {
    service.locationGroupFilter("LEI", "Z")
    verify(defaultService).locationGroupFilter("LEI", "Z")
  }

  @Test
  fun locationGroupsFiltersCallsOverrideOnlyIfOverridden() {
    whenever(overrideService.getLocationGroups("LEI")).thenReturn(listOf(LG1))
    service.locationGroupFilter("LEI", "Z")
    verify(overrideService).locationGroupFilter("LEI", "Z")
    verifyNoMoreInteractions(defaultService)
  }

  companion object {
    private val LG1 = LocationGroup(key = "A", name = "A")
  }
}
