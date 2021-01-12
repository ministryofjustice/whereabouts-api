package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import uk.gov.justice.digital.hmpps.whereabouts.model.Location as PrisonApiLocation

class AppointmentLocationsServiceTest {

  private val prisonApiService: PrisonApiService = mock()
  private val appointmentLocationsFinderService: AppointmentLocationsFinderService = mock()

  @Test
  fun `it does not fall over`() {
    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted(anyString(), anyString())).thenReturn(listOf())
    whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any())).thenReturn(emptyList())
    whenever(appointmentLocationsFinderService.find(any(), anyList(), anyList())).thenReturn(listOf())

    assertThat(
      AppointmentLocationsService(prisonApiService, appointmentLocationsFinderService)
        .findLocationsForAppointmentIntervals(
          AppointmentLocationsSpecification(
            LocalDate.now(),
            "WWI",
            listOf(),
            listOf()
          )
        )
    ).isEmpty()
  }

  @Test
  fun `it filters and translates locations from prisonApiService`() {
    whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any())).thenReturn(emptyList())
    whenever(appointmentLocationsFinderService.find(any(), anyList(), anyList())).thenReturn(listOf())

    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted(anyString(), anyString()))
      .thenReturn(
        listOf(
          PrisonApiLocation(
            locationId = 1L,
            locationType = "X",
            description = "Not a VIDE location",
            locationUsage = "Don't care",
            agencyId = "WWI",
            currentOccupancy = 0,
            locationPrefix = "Don't care",
            operationalCapacity = 0,
            userDescription = "Not Used",
            internalLocationCode = "Not Used"
          ),
          PrisonApiLocation(
            locationId = 2L,
            locationType = "VIDE",
            description = "A VIDE location",
            locationUsage = "Not Used",
            agencyId = "WWI",
            currentOccupancy = 0,
            locationPrefix = "Not Used",
            operationalCapacity = 0,
            userDescription = "Not Used",
            internalLocationCode = "Not Used"
          )
        )
      )

    val specification = AppointmentLocationsSpecification(
      LocalDate.now(),
      "WWI",
      listOf(),
      listOf()
    )
    AppointmentLocationsService(prisonApiService, appointmentLocationsFinderService)
      .findLocationsForAppointmentIntervals(specification)

    verify(prisonApiService).getAgencyLocationsForTypeUnrestricted("WWI", "APP")
    verify(appointmentLocationsFinderService)
      .find(
        eq(specification),
        eq(listOf(Location(2L, "A VIDE location"))),
        eq(listOf())
      )
  }

  @Test
  fun `it filters ScheduledAppointmentDtos from prisonApiService`() {
    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted(anyString(), anyString())).thenReturn(listOf())
    whenever(appointmentLocationsFinderService.find(any(), anyList(), anyList())).thenReturn(listOf())

    whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any()))
      .thenReturn(
        listOf(
          ScheduledAppointmentDto(
            id = 1L,
            agencyId = "WWI",
            locationId = 10L,
            appointmentTypeCode = "XXX",
            startTime = LocalDateTime.of(2020, 1, 1, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 1, 0)
          ),
          ScheduledAppointmentDto(
            id = 2L,
            agencyId = "WWI",
            locationId = 11L,
            appointmentTypeCode = "VLB",
            startTime = LocalDateTime.of(2020, 1, 1, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 1, 0)
          ),
          ScheduledAppointmentDto(
            id = 3L,
            agencyId = "WWI",
            locationId = 12L,
            appointmentTypeCode = "VLB",
            startTime = LocalDateTime.of(2020, 1, 1, 0, 0),
            endTime = null
          ),
        )
      )

    val specification = AppointmentLocationsSpecification(
      LocalDate.of(2021, 1, 1),
      "WWI",
      listOf(),
      listOf()
    )

    AppointmentLocationsService(prisonApiService, appointmentLocationsFinderService)
      .findLocationsForAppointmentIntervals(specification)

    verify(prisonApiService)
      .getScheduledAppointmentsByAgencyAndDate(eq("WWI"), eq(LocalDate.of(2021, 1, 1)))

    verify(appointmentLocationsFinderService)
      .find(
        eq(specification),
        eq(emptyList()),
        eq(
          listOf(
            ScheduledAppointmentDto(
              id = 2L,
              agencyId = "WWI",
              locationId = 11L,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 0, 0),
              endTime = LocalDateTime.of(2020, 1, 1, 1, 0)
            )
          )
        )
      )
  }

  @Test
  fun `it translates LocationsForAppointmentIntervals to AvaliableLocations`() {
    whenever(prisonApiService.getScheduledAppointmentsByAgencyAndDate(anyString(), any())).thenReturn(emptyList())

    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted(anyString(), anyString()))
      .thenReturn(
        listOf(
          PrisonApiLocation(
            locationId = 1L,
            locationType = "VIDE",
            description = "L1",
            locationUsage = "Not Used",
            agencyId = "WWI",
            currentOccupancy = 0,
            locationPrefix = "Not Used",
            operationalCapacity = 0,
            userDescription = "Not Used",
            internalLocationCode = "Not Used"
          ),
          PrisonApiLocation(
            locationId = 2L,
            locationType = "VIDE",
            description = "L2",
            locationUsage = "Not Used",
            agencyId = "WWI",
            currentOccupancy = 0,
            locationPrefix = "Not Used",
            operationalCapacity = 0,
            userDescription = "Not Used",
            internalLocationCode = "Not Used"
          )
        )
      )

    val interval1 = Interval(LocalTime.of(9, 0), LocalTime.of(9, 30))
    val interval2 = Interval(LocalTime.of(10, 0), LocalTime.of(10, 30))

    whenever(appointmentLocationsFinderService.find(any(), anyList(), anyList()))
      .thenReturn(
        listOf(
          LocationsForAppointmentIntervals(interval1, 1L, 2L, 3L),
          LocationsForAppointmentIntervals(interval2, 2L),
        )
      )

    val availableLocations = AppointmentLocationsService(prisonApiService, appointmentLocationsFinderService)
      .findLocationsForAppointmentIntervals(
        AppointmentLocationsSpecification(LocalDate.now(), "WWI", listOf(), listOf())
      )

    assertThat(availableLocations).containsExactly(
      AvailableLocations(
        interval1,
        listOf(
          Location(1L, "L1"),
          Location(2L, "L2")
        )
      ),
      AvailableLocations(
        interval2,
        listOf(
          Location(2L, "L2")
        )
      )
    )
  }
}
