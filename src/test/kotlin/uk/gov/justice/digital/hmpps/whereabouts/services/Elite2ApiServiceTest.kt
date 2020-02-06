package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.whereabouts.model.Location
import javax.persistence.EntityNotFoundException

class Elite2ApiServiceTest {

  private val restTemplate: OAuth2RestTemplate = mock()
  private val elite2ApiService = Elite2ApiService(restTemplate)

  @Test
  fun `getAgencyLocationsForType - data returned ok`() {
    val someAgencyId = "SYI"
    val someLocationType = "CELL"
    whenever(restTemplate.exchange(
        anyString(),
        eq(HttpMethod.GET),
        eq(null),
        any<ParameterizedTypeReference<List<Location>>>(),
        eq(someAgencyId), eq(someLocationType)
    )).thenReturn(ResponseEntity(listOf(aLocation(someAgencyId, someLocationType)), HttpStatus.OK))

    val response = elite2ApiService.getAgencyLocationsForType(someAgencyId, someLocationType)

    assertThat(response).containsExactly(aLocation(someAgencyId, someLocationType))
  }

  @Test(expected = EntityNotFoundException::class)
  fun `getAgencyLocationsForType - not found raises exception`() {
    whenever(restTemplate.exchange(
        anyString(),
        eq(HttpMethod.GET),
        eq(null),
        any<ParameterizedTypeReference<List<Location>>>(),
        eq("any agency"), eq("any locationType")
    )).thenThrow(HttpClientErrorException(HttpStatus.NOT_FOUND))

    elite2ApiService.getAgencyLocationsForType("any agency", "any locationType")
  }

  private fun aLocation(agencyId: String, locationType: String) =
      Location(
          locationId = 1L,
          locationType = locationType,
          description = "$agencyId-A-1-1",
          agencyId = agencyId,
          operationalCapacity = 1,
          currentOccupancy = 0,
          locationPrefix = agencyId,
          userDescription = "A/1 1-1",
          locationUsage = "APP",
          internalLocationCode = "1",
          parentLocationId = null)

}