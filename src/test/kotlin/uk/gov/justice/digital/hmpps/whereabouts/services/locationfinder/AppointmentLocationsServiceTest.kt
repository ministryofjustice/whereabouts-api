package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.model.HearingType
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkAppointment
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import uk.gov.justice.digital.hmpps.whereabouts.model.Location as PrisonApiLocation

class AppointmentLocationsServiceTest {

  private val prisonApiService: PrisonApiService = mock()
  private val appointmentLocationsFinderService: AppointmentLocationsFinderService = mock()
  private val videoLinkBookingRepository: VideoLinkBookingRepository = mock()

  @Test
  fun `it does not fall over`() {
    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted(anyString(), anyString())).thenReturn(listOf())
    whenever(prisonApiService.getScheduledAppointments(anyString(), any())).thenReturn(emptyList())
    whenever(appointmentLocationsFinderService.find(any(), anyList(), anyList())).thenReturn(listOf())

    assertThat(
      AppointmentLocationsService(prisonApiService, appointmentLocationsFinderService, videoLinkBookingRepository)
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
    whenever(prisonApiService.getScheduledAppointments(anyString(), any())).thenReturn(emptyList())
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
            description = "A VIDE LOCATION",
            locationUsage = "Not Used",
            agencyId = "WWI",
            currentOccupancy = 0,
            locationPrefix = "Not Used",
            operationalCapacity = 0,
            userDescription = "A VIDE location",
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
    AppointmentLocationsService(prisonApiService, appointmentLocationsFinderService, videoLinkBookingRepository)
      .findLocationsForAppointmentIntervals(specification)

    verify(prisonApiService).getAgencyLocationsForTypeUnrestricted("WWI", "APP")
    verify(appointmentLocationsFinderService)
      .find(
        eq(listOf()),
        eq(listOf(2L)),
        eq(listOf())
      )
  }

  @Test
  fun `it filters ScheduledAppointmentDtos from prisonApiService`() {
    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted(anyString(), anyString())).thenReturn(listOf())
    whenever(appointmentLocationsFinderService.find(any(), anyList(), anyList())).thenReturn(listOf())

    whenever(prisonApiService.getScheduledAppointments(anyString(), any()))
      .thenReturn(
        listOf(
          ScheduledAppointmentDto(
            id = 1L,
            agencyId = "WWI",
            locationId = 10L,
            appointmentTypeCode = "VLAA",
            startTime = LocalDateTime.of(2020, 1, 1, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 1, 0),
            "A1234AA"
          ),
          ScheduledAppointmentDto(
            id = 2L,
            agencyId = "WWI",
            locationId = 11L,
            appointmentTypeCode = "VLB",
            startTime = LocalDateTime.of(2020, 1, 1, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 1, 0),
            "B2345BB"
          ),
          ScheduledAppointmentDto(
            id = 3L,
            agencyId = "WWI",
            locationId = 12L,
            appointmentTypeCode = "VLB",
            startTime = LocalDateTime.of(2020, 1, 1, 0, 0),
            endTime = null,
            "C3456CC"
          ),
        )
      )

    val specification = AppointmentLocationsSpecification(
      LocalDate.of(2021, 1, 1),
      "WWI",
      listOf(),
      listOf()
    )

    AppointmentLocationsService(prisonApiService, appointmentLocationsFinderService, videoLinkBookingRepository)
      .findLocationsForAppointmentIntervals(specification)

    verify(prisonApiService)
      .getScheduledAppointments(eq("WWI"), eq(LocalDate.of(2021, 1, 1)))

    verify(appointmentLocationsFinderService)
      .find(
        eq(listOf()),
        eq(emptyList()),
        eq(
          listOf(
            ScheduledAppointmentDto(
              id = 1L,
              agencyId = "WWI",
              locationId = 10L,
              appointmentTypeCode = "VLAA",
              startTime = LocalDateTime.of(2020, 1, 1, 0, 0),
              endTime = LocalDateTime.of(2020, 1, 1, 1, 0),
              "A1234AA"
            ),
            ScheduledAppointmentDto(
              id = 2L,
              agencyId = "WWI",
              locationId = 11L,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 0, 0),
              endTime = LocalDateTime.of(2020, 1, 1, 1, 0),
              "B2345BB"
            ),
          )
        )
      )
  }

  @Test
  fun `it excludes ScheduledAppointmentDtos that correspond with excluded videoLinkBookingIds`() {
    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted(anyString(), anyString())).thenReturn(listOf())
    whenever(appointmentLocationsFinderService.find(any(), anyList(), anyList())).thenReturn(listOf())

    whenever(prisonApiService.getScheduledAppointments(anyString(), any()))
      .thenReturn(
        listOf(
          ScheduledAppointmentDto(
            id = 1L,
            agencyId = "WWI",
            locationId = 10L,
            appointmentTypeCode = "VLB",
            startTime = LocalDateTime.of(2020, 1, 1, 0, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 1, 0),
            offenderNo = "A1234AA"
          ),
          ScheduledAppointmentDto(
            id = 2L,
            agencyId = "WWI",
            locationId = 11L,
            appointmentTypeCode = "VLB",
            startTime = LocalDateTime.of(2020, 1, 1, 1, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 2, 0),
            offenderNo = "B2345BB"
          ),
          ScheduledAppointmentDto(
            id = 3L,
            agencyId = "WWI",
            locationId = 12L,
            appointmentTypeCode = "VLB",
            startTime = LocalDateTime.of(2020, 1, 1, 2, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 3, 0),
            offenderNo = "C3456CC"
          ),
          ScheduledAppointmentDto(
            id = 4L,
            agencyId = "WWI",
            locationId = 13L,
            appointmentTypeCode = "VLB",
            startTime = LocalDateTime.of(2020, 1, 1, 3, 0),
            endTime = LocalDateTime.of(2020, 1, 1, 4, 0),
            offenderNo = "D4567DD"
          ),
        )
      )

    whenever(videoLinkBookingRepository.findAllById(anyList()))
      .thenReturn(
        listOf(
          VideoLinkBooking(
            id = 1L,
            main = VideoLinkAppointment(
              appointmentId = 1L,
              id = 1L,
              bookingId = 9999L,
              court = "Don't care",
              hearingType = HearingType.MAIN
            )
          ),
          VideoLinkBooking(
            id = 2L,
            main = VideoLinkAppointment(
              appointmentId = 10L,
              id = 2L,
              bookingId = 9999L,
              court = "Don't care",
              hearingType = HearingType.MAIN
            ),
            pre = VideoLinkAppointment(
              appointmentId = 3L,
              id = 3L,
              bookingId = 9999L,
              court = "Don't care",
              hearingType = HearingType.PRE
            ),
            post = VideoLinkAppointment(
              appointmentId = 4L,
              id = 4L,
              bookingId = 9999L,
              court = "Don't care",
              hearingType = HearingType.POST
            ),
          )

        )
      )

    val specification = AppointmentLocationsSpecification(
      LocalDate.of(2021, 1, 1),
      "WWI",
      listOf(1L, 2L),
      listOf()
    )

    AppointmentLocationsService(prisonApiService, appointmentLocationsFinderService, videoLinkBookingRepository)
      .findLocationsForAppointmentIntervals(specification)

    verify(videoLinkBookingRepository).findAllById(listOf(1L, 2L))

    verify(appointmentLocationsFinderService)
      .find(
        eq(listOf()),
        eq(emptyList()),
        eq(
          listOf(
            ScheduledAppointmentDto(
              id = 2L,
              agencyId = "WWI",
              locationId = 11L,
              appointmentTypeCode = "VLB",
              startTime = LocalDateTime.of(2020, 1, 1, 1, 0),
              endTime = LocalDateTime.of(2020, 1, 1, 2, 0),
              offenderNo = "B2345BB"
            )
          )
        )
      )
  }

  @Test
  fun `it translates LocationsForAppointmentIntervals to AvaliableLocations`() {
    whenever(prisonApiService.getScheduledAppointments(anyString(), any(), anyOrNull(), anyOrNull())).thenReturn(emptyList())

    whenever(prisonApiService.getAgencyLocationsForTypeUnrestricted(anyString(), anyString()))
      .thenReturn(
        listOf(
          PrisonApiLocation(
            locationId = 1L,
            locationType = "VIDE",
            description = "ROOM 1",
            locationUsage = "Not Used",
            agencyId = "WWI",
            currentOccupancy = 0,
            locationPrefix = "Not Used",
            operationalCapacity = 0,
            userDescription = "Room 1",
            internalLocationCode = "Not Used"
          ),
          PrisonApiLocation(
            locationId = 2L,
            locationType = "VIDE",
            description = "ROOM 2",
            locationUsage = "Not Used",
            agencyId = "WWI",
            currentOccupancy = 0,
            locationPrefix = "Not Used",
            operationalCapacity = 0,
            userDescription = "Room 2",
            internalLocationCode = "Not Used"
          )
        )
      )

    val interval1 = Interval(LocalTime.of(9, 0), LocalTime.of(9, 30))
    val interval2 = Interval(LocalTime.of(10, 0), LocalTime.of(10, 30))

    whenever(appointmentLocationsFinderService.find(any(), anyList(), anyList()))
      .thenReturn(
        listOf(
          AppointmentIntervalLocations(interval1, 1L, 2L, 3L),
          AppointmentIntervalLocations(interval2, 2L),
        )
      )

    val availableLocations =
      AppointmentLocationsService(prisonApiService, appointmentLocationsFinderService, videoLinkBookingRepository)
        .findLocationsForAppointmentIntervals(
          AppointmentLocationsSpecification(LocalDate.now(), "WWI", listOf(), listOf())
        )

    assertThat(availableLocations).containsExactly(
      AvailableLocations(
        interval1,
        listOf(
          LocationIdAndDescription(1L, "Room 1"),
          LocationIdAndDescription(2L, "Room 2")
        )
      ),
      AvailableLocations(
        interval2,
        listOf(
          LocationIdAndDescription(2L, "Room 2")
        )
      )
    )
  }
}
