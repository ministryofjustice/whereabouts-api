package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AppointmentLocationsFinderTest {
  @Test
  fun `no new appointments, no locations, no scheduled appointments`() {
    assertLocationsForAppointments(
      listOf(),
      listOf(),
      listOf()
    ).isEmpty()
  }

  @Test
  fun `one appointment, no locations, no scheduled appointments`() {
    val appointmentInterval = Interval(T09_00, T09_30)
    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(),
      listOf()
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval))
  }

  @Test
  fun `one appointment, one location, no scheduled appointments`() {
    val appointmentInterval = Interval(T09_00, T09_30)
    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(1L),
      listOf()
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval, 1L))
  }

  @Test
  fun `one appointment, three locations, no scheduled appointments`() {
    val appointmentInterval = Interval(T09_00, T09_30)
    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(1L, 2L, 3L),
      listOf()
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval, 1L, 2L, 3L))
  }

  @Test
  fun `no appointments, one location, one scheduled appointment`() {
    assertLocationsForAppointments(
      listOf(),
      listOf(1L),
      listOf(scheduledAppointment(1L, T09_00, T09_30))
    ).isEmpty()
  }

  @Test
  fun `one location, one appointment before one scheduled appointment`() {
    val appointmentInterval = Interval(T09_00, T09_30)

    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(1L),
      listOf(scheduledAppointment(1L, T10_00, T10_30))
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval, 1L))
  }

  @Test
  fun `one location, one appointment ends when one scheduled appointment starts`() {
    val appointmentInterval = Interval(T09_00, T09_30)

    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(1L),
      listOf(scheduledAppointment(1L, T09_30, T10_00))
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval, 1L))
  }

  @Test
  fun `one location, one appointment ends after one scheduled appointment starts`() {
    val appointmentInterval = Interval(T09_00, T09_40)

    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(1L),
      listOf(scheduledAppointment(1L, T09_30, T10_00))
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval))
  }

  @Test
  fun `one location, one appointment brackets scheduled appointment`() {
    val appointmentInterval = Interval(T09_00, T10_00)

    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(1L),
      listOf(scheduledAppointment(1L, T09_20, T09_40))
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval))
  }

  @Test
  fun `one location, one appointment bracketed by scheduled appointment`() {
    val appointmentInterval = Interval(T09_30, T10_00)

    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(1L),
      listOf(scheduledAppointment(1L, T09_00, T10_30))
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval))
  }

  @Test
  fun `one location, one appointment overlaps scheduled appointment end`() {
    val appointmentInterval = Interval(T09_30, T10_30)

    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(1L),
      listOf(scheduledAppointment(1L, T09_00, T10_00))
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval))
  }

  @Test
  fun `one location, one appointment starts at scheduled appointment end`() {
    val appointmentInterval = Interval(T09_30, T10_00)

    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(1L),
      listOf(scheduledAppointment(1L, T09_00, T09_30))
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval, 1L))
  }

  @Test
  fun `one location, one appointment surrounded by scheduled appointments`() {
    val appointmentInterval = Interval(T09_30, T10_00)

    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(1L),
      listOf(
        scheduledAppointment(1L, T09_00, T09_30),
        scheduledAppointment(1L, T10_00, T10_30)
      )
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval, 1L))
  }

  @Test
  fun `three locations, one appointment one clashing scheduled appointment`() {
    val appointmentInterval = Interval(T09_30, T10_00)

    assertLocationsForAppointments(
      listOf(appointmentInterval),
      listOf(1L, 2L, 3L),
      listOf(scheduledAppointment(2L, T09_30, T10_00))
    ).containsExactly(LocationsForAppointmentIntervals(appointmentInterval, 1L, 3L))
  }

  @Test
  fun `one location, two appointments no scheduled appointments`() {
    val a1 = Interval(T09_30, T10_00)
    val a2 = Interval(T10_00, T10_30)

    assertLocationsForAppointments(
      listOf(a1, a2),
      listOf(1L),
      listOf()
    ).containsExactlyInAnyOrder(
      LocationsForAppointmentIntervals(a1, 1L),
      LocationsForAppointmentIntervals(a2, 1L)
    )
  }

  @Test
  fun `one location, multiple overlapping appointments, no scheduled appointments`() {
    val a1 = Interval(T09_30, T10_00)
    val a2 = Interval(T09_40, T10_30)
    val a3 = Interval(T10_00, T11_00)

    assertLocationsForAppointments(
      listOf(a1, a2, a3),
      listOf(1L),
      listOf()
    ).containsExactlyInAnyOrder(
      LocationsForAppointmentIntervals(a1, 1L),
      LocationsForAppointmentIntervals(a2, 1L),
      LocationsForAppointmentIntervals(a3, 1L)
    )
  }

  @Test
  fun `one location, multiple overlapping appointments pass through a scheduled appointment`() {
    val a1 = Interval(T09_00, T09_30)
    val a2 = Interval(T09_20, T09_40)
    val a3 = Interval(T09_30, T10_00)
    val a4 = Interval(T09_40, T10_30)

    assertLocationsForAppointments(
      listOf(a1, a2, a3, a4),
      listOf(1L),
      listOf(scheduledAppointment(1L, T09_30, T09_40))
    ).containsExactlyInAnyOrder(
      LocationsForAppointmentIntervals(a1, 1L),
      LocationsForAppointmentIntervals(a2),
      LocationsForAppointmentIntervals(a3),
      LocationsForAppointmentIntervals(a4, 1L)
    )
  }

  @Test
  fun `two locations, two appointments, scheduled appointments leave alternate locations available`() {
    val a1 = Interval(T09_00, T09_30)
    val a2 = Interval(T09_30, T10_00)

    assertLocationsForAppointments(
      listOf(a1, a2),
      listOf(1L, 2L),
      listOf(
        scheduledAppointment(1L, T09_20, T09_30),
        scheduledAppointment(2L, T09_30, T09_40),
      )
    ).containsExactlyInAnyOrder(
      LocationsForAppointmentIntervals(a1, 2L),
      LocationsForAppointmentIntervals(a2, 1L)
    )
  }

  @Test
  fun `two locations, two appointments, scheduled appointments leave one location available`() {
    val a1 = Interval(T09_00, T09_30)
    val a2 = Interval(T09_30, T10_00)

    assertLocationsForAppointments(
      listOf(a1, a2),
      listOf(1L, 2L),
      listOf(
        scheduledAppointment(1L, T09_20, T09_40),
        scheduledAppointment(2L, T09_30, T09_40),
      )
    ).containsExactlyInAnyOrder(
      LocationsForAppointmentIntervals(a1, 2L),
      LocationsForAppointmentIntervals(a2)
    )
  }

  @Test
  fun `two locations, three contiguous appointments, scheduled appointments leave alternate locations available for each appointment`() {
    val a1 = Interval(T09_00, T09_30)
    val a2 = Interval(T09_30, T10_00)
    val a3 = Interval(T10_00, T10_30)

    assertLocationsForAppointments(
      listOf(a1, a2, a3),
      listOf(1L, 2L),
      listOf(
        scheduledAppointment(2L, T09_10, T09_20),
        scheduledAppointment(1L, T09_40, T09_50),
        scheduledAppointment(2L, T10_00, T10_30),
      )
    ).containsExactlyInAnyOrder(
      LocationsForAppointmentIntervals(a1, 1L),
      LocationsForAppointmentIntervals(a2, 2L),
      LocationsForAppointmentIntervals(a3, 1L),
    )
  }

  private fun assertLocationsForAppointments(
    appointmentIntervals: List<Interval>,
    locationIds: List<Long>,
    scheduledAppointments: List<ScheduledAppointmentDto>
  ) =
    assertThat(
      AppointmentLocationsFinder(
        appointmentIntervals,
        locationIds,
        scheduledAppointments
      ).findLocationsForAppointmentIntervals()
    )

  companion object {
    private val aDate: LocalDate = LocalDate.of(2020, 8, 1)

    private val T09_00: LocalTime = LocalTime.of(9, 0)
    private val T09_10: LocalTime = LocalTime.of(9, 10)
    private val T09_20: LocalTime = LocalTime.of(9, 20)
    private val T09_30: LocalTime = LocalTime.of(9, 30)
    private val T09_40: LocalTime = LocalTime.of(9, 40)
    private val T09_50: LocalTime = LocalTime.of(9, 50)
    private val T10_00: LocalTime = LocalTime.of(10, 0)
    private val T10_30: LocalTime = LocalTime.of(10, 30)
    private val T11_00: LocalTime = LocalTime.of(11, 0)

    fun scheduledAppointment(locationId: Long, startTime: LocalTime, endTime: LocalTime): ScheduledAppointmentDto =
      ScheduledAppointmentDto(
        locationId = locationId,
        startTime = LocalDateTime.of(aDate, startTime),
        endTime = LocalDateTime.of(aDate, endTime),
        id = 0L,
        agencyId = "X",
        appointmentTypeCode = "X"
      )
  }
}
