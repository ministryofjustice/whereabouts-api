package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import io.mockk.Matcher
import io.mockk.MockKMatcherScope
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import uk.gov.justice.digital.hmpps.whereabouts.model.VideoLinkBooking
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingOptions
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingOptionsFinder
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.VideoLinkBookingSearchSpecification
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * This test class uses Mockk because I couldn't find a way to get mockito or mockitokotlin2 to compare Sequences.
 * Not even with a custom Argument matcher.
 * Show me how to do it and I'll revert to Mockito.
 */
class AppointmentLocationServiceTest2 {

  private val prisonApiService: PrisonApiService = mockk()
  private val appointmentLocationsFinderService: AppointmentLocationsFinderService = mockk()
  private val videoLinkBookingRepository: VideoLinkBookingRepository = mockk()
  private val videoLinkBookingOptionsFinder: VideoLinkBookingOptionsFinder = mockk()

  fun service(): AppointmentLocationsService =
    AppointmentLocationsService(
      prisonApiService,
      appointmentLocationsFinderService,
      videoLinkBookingRepository,
      videoLinkBookingOptionsFinder
    )

  @Test
  fun `it doesn't fall over`() {
    val theResult = VideoLinkBookingOptions(matched = true, alternatives = emptyList())

    every { prisonApiService.getScheduledAppointments(any(), any()) } returns emptyList()
    every { videoLinkBookingOptionsFinder.findOptions(any(), any()) } returns theResult

    assertThat(
      service()
        .findVideoLinkBookingOptions(
          VideoLinkBookingSearchSpecification(
            date = dontCareDate,
            agencyId = dontCareAgencyId,
            preAppointment = null,
            mainAppointment = LocationAndInterval(location1, dontCareInterval)
          )
        )
    ).isEqualTo(theResult)
  }

  @Test
  fun `It filters ScheduledAppointmentDtos from prisonApiService`() {

    every {
      videoLinkBookingOptionsFinder.findOptions(any(), any())
    } returns VideoLinkBookingOptions(
      matched = true,
      alternatives = emptyList()
    )

    every {
      prisonApiService.getScheduledAppointments(any(), any())
    } returns
      listOf(
        appt1_location1,
        appt2_location1,
        // appt3 excluded because endTime is null
        appt3_location1_no_end_time,
        // Excluded because location2 id isn't in specification set below.
        appt1_location2
      )

    val specification = VideoLinkBookingSearchSpecification(
      date = dontCareDate,
      agencyId = dontCareAgencyId,
      preAppointment = null,
      mainAppointment = LocationAndInterval(location1, dontCareInterval)
    )

    service().findVideoLinkBookingOptions(specification)

    verify {
      prisonApiService.getScheduledAppointments(dontCareAgencyId, dontCareDate)
    }

    verify {
      videoLinkBookingOptionsFinder
        .findOptions(
          specification,
          eqSeq(
            sequenceOf(
              appt1_location1,
              appt2_location1
            )
          )
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
      alternatives = emptyList()
    )

    every {
      videoLinkBookingRepository.findAllById(any())
    } returns listOf(excludedVideoLinkBookingMainOnly)

    every {
      prisonApiService.getScheduledAppointments(any(), any())
    } returns
      listOf(
        // excluded because id matches the id of the excluded video link booking's appointment id in the spec below
        appt_location1_excl_main,
        appt2_location1
      )

    val specification = VideoLinkBookingSearchSpecification(
      date = dontCareDate,
      agencyId = dontCareAgencyId,
      preAppointment = null,
      mainAppointment = LocationAndInterval(location1, dontCareInterval),
      vlbIdToExclude = excludedVideoLinkBookingId
    )
    service().findVideoLinkBookingOptions(specification)

    verify {
      videoLinkBookingOptionsFinder
        .findOptions(
          specification,
          eqSeq(sequenceOf(appt2_location1))
        )
    }

    verify { videoLinkBookingRepository.findAllById(listOf(excludedVideoLinkBookingId)) }
  }

  @Test
  fun `It filters ScheduledAppointmentDtos from prisonApiService for the spec's main, pre and post appointments`() {

    every {
      videoLinkBookingOptionsFinder.findOptions(any(), any())
    } returns VideoLinkBookingOptions(
      matched = true,
      alternatives = emptyList()
    )

    every {
      videoLinkBookingRepository.findAllById(any())
    } returns listOf(excludedVideoLinkBooking)

    every {
      prisonApiService.getScheduledAppointments(any(), any())
    } returns
      listOf(
        appt_location1_excl_main,
        appt_location2_excl_pre,
        appt_location3_excl_post,
        appt2_location1,
        appt2_location2,
        appt2_location3
      )

    val specification = VideoLinkBookingSearchSpecification(
      date = dontCareDate,
      agencyId = dontCareAgencyId,
      preAppointment = LocationAndInterval(location2, dontCareInterval),
      mainAppointment = LocationAndInterval(location1, dontCareInterval),
      postAppointment = LocationAndInterval(location3, dontCareInterval),
      vlbIdToExclude = excludedVideoLinkBookingId
    )

    service().findVideoLinkBookingOptions(specification)

    verify {
      prisonApiService.getScheduledAppointments(dontCareAgencyId, dontCareDate)
    }

    verify {
      videoLinkBookingOptionsFinder
        .findOptions(
          specification,
          eqSeq(
            sequenceOf(
              appt2_location1,
              appt2_location2,
              appt2_location3
            )
          )
        )
    }
  }

  companion object {
    const val excludedVideoLinkBookingId = 100L
    const val excludedMainAppointmentId = 1L
    const val excludedPreAppointmentId = 2L
    const val excludedPostAppointmentId = 3L

    const val location1 = 11L
    const val location2 = 12L
    const val location3 = 13L

    // Tests aren't concerned with the following values
    val dontCareDate: LocalDate = LocalDate.of(2020, 1, 1)
    val dontCareTime: LocalTime = LocalTime.of(9, 0)
    val dontCareDateTime: LocalDateTime = dontCareDate.atTime(dontCareTime)

    const val dontCareAgencyId = "WWI"
    const val dontCareOffenderNo = "C3456CC"
    val dontCareInterval = Interval(dontCareTime, dontCareTime)

    // appointmentTypeCode doesn't matter. All appointments for a room make the room unavailable.
    val appt1_location1 = appointmentDto(location1, excludedMainAppointmentId, "VLAA")
    val appt2_location1 = appointmentDto(location1, 20L)

    // Always excluded because endTime == null
    val appt3_location1_no_end_time =
      ScheduledAppointmentDto(
        id = 30L,
        agencyId = dontCareAgencyId,
        locationId = location1,
        appointmentTypeCode = "VLB",
        startTime = dontCareDateTime,
        endTime = null,
        dontCareOffenderNo
      )

    val appt1_location2 = appointmentDto(location2, excludedPreAppointmentId)
    val appt2_location2 = appointmentDto(location2, 40L)

    val appt1_location3 = appointmentDto(location3, excludedPostAppointmentId)
    val appt2_location3 = appointmentDto(location3, 50L)

    val appt_location1_excl_main = appt1_location1
    val appt_location2_excl_pre = appt1_location2
    val appt_location3_excl_post = appt1_location3

    val excludedVideoLinkBookingMainOnly = VideoLinkBooking(
      id = excludedVideoLinkBookingId,
      offenderBookingId = 999L,
      courtName = "DONTCARE",
      courtId = "DONTCARE",
    ).apply {
      addMainAppointment(excludedMainAppointmentId, 9999L)
    }

    val excludedVideoLinkBooking = VideoLinkBooking(
      id = excludedVideoLinkBookingId,
      offenderBookingId = 999L,
      courtName = "DONTCARE",
      courtId = "DONTCARE",
    ).apply {
      addMainAppointment(excludedMainAppointmentId, 9999L)
      addPreAppointment(excludedPreAppointmentId, 9998L)
      addPostAppointment(excludedPostAppointmentId, 9997L)
    }

    fun appointmentDto(locationId: Long, id: Long, appointmentTypeCode: String = "VLB") =
      ScheduledAppointmentDto(
        id = id,
        agencyId = dontCareAgencyId,
        locationId = locationId,
        appointmentTypeCode = appointmentTypeCode,
        startTime = dontCareDateTime,
        endTime = dontCareDateTime,
        dontCareOffenderNo
      )

    inline fun <reified T : Any> MockKMatcherScope.eqSeq(value: Sequence<T>): Sequence<T> =
      match(SeqMatcher(value))
  }
}

/**
 * Mockk Matcher for Sequences - with toString() to make it slightly easier to see what's going on.
 * In general Sequences can't be matched because they can be infinite.
 * This Matcher assumes that the sequences to be matched are finite and can be converted to lists which is
 * fine for these tests.
 */
data class SeqMatcher<in T : Any>(private val value: Sequence<T>) : Matcher<Sequence<T>> {
  override fun match(arg: Sequence<T>?): Boolean = if (arg == null) false else arg.toList() == value.toList()
  override fun toString(): String = "seqEq(${value.toList()})"
}
