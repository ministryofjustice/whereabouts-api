package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentSearchDto
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.utils.DataHelpers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Optional

class VideoLinkBookingOptionsServiceTest {

  private val prisonApiService: PrisonApiService = mockk()
  private val videoLinkBookingRepository: VideoLinkBookingRepository = mockk()
  private val videoLinkBookingOptionsFinder: VideoLinkBookingOptionsFinder = mockk()

  fun service(): VideoLinkBookingOptionsService =
    VideoLinkBookingOptionsService(
      prisonApiService,
      videoLinkBookingRepository,
      videoLinkBookingOptionsFinder,
    )

  @Test
  fun `it doesn't fall over`() {
    val theResult = VideoLinkBookingOptions(matched = true, alternatives = emptyList())

    every { prisonApiService.getScheduledAppointments(any(), any(), isNull(), any()) } returns emptyList()
    every { videoLinkBookingOptionsFinder.findOptions(any(), any()) } returns theResult

    assertThat(
      service()
        .findVideoLinkBookingOptions(
          VideoLinkBookingSearchSpecification(
            date = searchDate,
            agencyId = AGENCY_ID,
            preAppointment = null,
            mainAppointment = LocationAndInterval(LOCATION_1, dontCareInterval),
          ),
        ),
    ).isEqualTo(theResult)
  }

  @Test
  fun `It filters ScheduledAppointmentDtos from prisonApiService`() {
    every {
      videoLinkBookingOptionsFinder.findOptions(any(), any())
    } returns VideoLinkBookingOptions(
      matched = true,
      alternatives = emptyList(),
    )

    every {
      prisonApiService.getScheduledAppointments(any(), any(), isNull(), any())
    } returns
      mutableListOf(
        appt1_location1,
        appt2_location1,
        // appt3 excluded because endTime is null
        appt3_location1_no_end_time,
      )

    val specification = VideoLinkBookingSearchSpecification(
      date = searchDate,
      agencyId = AGENCY_ID,
      preAppointment = null,
      mainAppointment = LocationAndInterval(LOCATION_1, dontCareInterval),
    )

    service().findVideoLinkBookingOptions(specification)

    verify {
      prisonApiService.getScheduledAppointments(eq(AGENCY_ID), eq(searchDate), isNull(), eq(LOCATION_1))
    }

    verify {
      videoLinkBookingOptionsFinder
        .findOptions(
          specification,
          listOf(
            appt1_location1,
            appt2_location1,
          ),
        )
    }
  }

  @Test
  fun `The appointments for an excluded booking are removed from the set of appointments for a location`() {
    // The returned value doesn't matter here.
    every {
      videoLinkBookingOptionsFinder.findOptions(any(), any())
    } returns VideoLinkBookingOptions(
      matched = true,
      alternatives = emptyList(),
    )

    every {
      videoLinkBookingRepository.findById(any())
    } returns Optional.of(excludedVideoLinkBookingMainOnly)

    every {
      prisonApiService.getScheduledAppointments(any(), any(), null, any())
    } returns
      listOf(
        // excluded because id matches the id of the excluded video link booking's appointment id in the spec below
        appt_location1_excl_main,
        appt2_location1,
      )

    val specification = VideoLinkBookingSearchSpecification(
      date = searchDate,
      agencyId = AGENCY_ID,
      preAppointment = null,
      mainAppointment = LocationAndInterval(LOCATION_1, dontCareInterval),
      vlbIdToExclude = EXCLUDED_VIDEO_LINK_BOOKING_ID,
    )
    service().findVideoLinkBookingOptions(specification)

    verify {
      videoLinkBookingOptionsFinder
        .findOptions(
          specification,
          listOf(appt2_location1),
        )
    }

    verify { videoLinkBookingRepository.findById(EXCLUDED_VIDEO_LINK_BOOKING_ID) }
  }

  @Test
  fun `It filters ScheduledAppointmentDtos from prisonApiService for the spec's main, pre and post appointments`() {
    every {
      videoLinkBookingOptionsFinder.findOptions(any(), any())
    } returns VideoLinkBookingOptions(
      matched = true,
      alternatives = emptyList(),
    )

    every {
      videoLinkBookingRepository.findById(any())
    } returns
      Optional.of(excludedVideoLinkBooking)

    every {
      prisonApiService.getScheduledAppointments(any(), any(), isNull(), LOCATION_1)
    } returns
      listOf(appt_location1_excl_main, appt2_location1)

    every {
      prisonApiService.getScheduledAppointments(any(), any(), isNull(), LOCATION_2)
    } returns
      listOf(appt_location2_excl_pre, appt2_location2)

    every {
      prisonApiService.getScheduledAppointments(any(), any(), isNull(), LOCATION_3)
    } returns
      listOf(appt_location3_excl_post, appt2_location3)

    val specification = VideoLinkBookingSearchSpecification(
      date = searchDate,
      agencyId = AGENCY_ID,
      preAppointment = LocationAndInterval(LOCATION_2, dontCareInterval),
      mainAppointment = LocationAndInterval(LOCATION_1, dontCareInterval),
      postAppointment = LocationAndInterval(LOCATION_3, dontCareInterval),
      vlbIdToExclude = EXCLUDED_VIDEO_LINK_BOOKING_ID,
    )

    service().findVideoLinkBookingOptions(specification)

    verify { prisonApiService.getScheduledAppointments(AGENCY_ID, searchDate, null, LOCATION_1) }
    verify { prisonApiService.getScheduledAppointments(AGENCY_ID, searchDate, null, LOCATION_2) }
    verify { prisonApiService.getScheduledAppointments(AGENCY_ID, searchDate, null, LOCATION_3) }

    verify {
      videoLinkBookingOptionsFinder
        .findOptions(
          specification,
          listOf(
            appt2_location2,
            appt2_location1,
            appt2_location3,
          ),
        )
    }
  }

  companion object {
    const val DONT_CARE = "Don't care"
    const val EXCLUDED_VIDEO_LINK_BOOKING_ID = 100L
    const val EXCLUDED_MAIN_APPOINTMENT_ID = 1L
    const val EXCLUDED_PRE_APPOINTMENT_ID = 2L
    const val EXCLUDED_POST_APPOINTMENT_ID = 3L

    const val LOCATION_1 = 11L
    const val LOCATION_2 = 12L
    const val LOCATION_3 = 13L

    // Tests aren't concerned with the following values
    val searchDate: LocalDate = LocalDate.of(2020, 1, 1)
    val dontCareTime: LocalTime = LocalTime.of(9, 0)
    val dontCareDateTime: LocalDateTime = searchDate.atTime(dontCareTime)

    const val AGENCY_ID = "WWI"
    val startDateTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
    val endDateTime = LocalDateTime.of(2022, 1, 1, 11, 0, 0)
    const val DONT_CARE_OFFENDER_NO = "C3456CC"
    val dontCareInterval = Interval(dontCareTime, dontCareTime)

    // appointmentTypeCode doesn't matter. All appointments for a room make the room unavailable.
    val appt1_location1 = appointmentDto(LOCATION_1, EXCLUDED_MAIN_APPOINTMENT_ID, "VLAA")
    val appt2_location1 = appointmentDto(LOCATION_1, 20L)

    // Always excluded because endTime == null
    val appt3_location1_no_end_time =
      ScheduledAppointmentSearchDto(
        id = 30L,
        agencyId = AGENCY_ID,
        locationId = LOCATION_1,
        appointmentTypeCode = "VLB",
        startTime = dontCareDateTime,
        endTime = null,
        offenderNo = DONT_CARE_OFFENDER_NO,
        appointmentTypeDescription = DONT_CARE,
        createUserId = DONT_CARE,
        firstName = DONT_CARE,
        lastName = DONT_CARE,
        locationDescription = DONT_CARE,
      )

    val appt1_location2 = appointmentDto(LOCATION_2, EXCLUDED_PRE_APPOINTMENT_ID)
    val appt2_location2 = appointmentDto(LOCATION_2, 40L)

    val appt1_location3 = appointmentDto(LOCATION_3, EXCLUDED_POST_APPOINTMENT_ID)
    val appt2_location3 = appointmentDto(LOCATION_3, 50L)

    val appt_location1_excl_main = appt1_location1
    val appt_location2_excl_pre = appt1_location2
    val appt_location3_excl_post = appt1_location3

    val excludedVideoLinkBookingMainOnly = DataHelpers.makeVideoLinkBooking(
      id = EXCLUDED_VIDEO_LINK_BOOKING_ID,
      offenderBookingId = 999L,
      courtName = DONT_CARE,
      courtId = DONT_CARE,
      prisonId = AGENCY_ID,
    ).apply {
      addMainAppointment(EXCLUDED_MAIN_APPOINTMENT_ID, 20L, startDateTime, endDateTime, 9999L)
    }

    val excludedVideoLinkBooking = DataHelpers.makeVideoLinkBooking(
      id = EXCLUDED_VIDEO_LINK_BOOKING_ID,
      offenderBookingId = 999L,
      courtName = DONT_CARE,
      courtId = DONT_CARE,
      prisonId = AGENCY_ID,
    ).apply {
      addMainAppointment(EXCLUDED_MAIN_APPOINTMENT_ID, 20L, startDateTime, endDateTime, 9999L)
      addPreAppointment(EXCLUDED_PRE_APPOINTMENT_ID, 20L, startDateTime, endDateTime, 9998L)
      addPostAppointment(EXCLUDED_POST_APPOINTMENT_ID, 20L, startDateTime, endDateTime, 9997L)
    }

    fun appointmentDto(locationId: Long, id: Long, appointmentTypeCode: String = "VLB"): ScheduledAppointmentSearchDto =
      ScheduledAppointmentSearchDto(
        id = id,
        agencyId = AGENCY_ID,
        locationId = locationId,
        appointmentTypeCode = appointmentTypeCode,
        startTime = dontCareDateTime,
        endTime = dontCareDateTime,
        offenderNo = DONT_CARE_OFFENDER_NO,
        appointmentTypeDescription = DONT_CARE,
        createUserId = DONT_CARE,
        firstName = DONT_CARE,
        lastName = DONT_CARE,
        locationDescription = DONT_CARE,
      )
  }
}
