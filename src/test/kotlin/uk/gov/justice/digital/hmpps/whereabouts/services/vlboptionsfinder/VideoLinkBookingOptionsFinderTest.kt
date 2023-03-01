package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.whereabouts.dto.prisonapi.ScheduledAppointmentSearchDto
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.AppointmentBuilder.Companion.from
import uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder.ScheduledAppointmentDtoBuilder.Companion.room
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VideoLinkBookingOptionsFinderTest {

  private val finder = VideoLinkBookingOptionsFinder(tenUntilFourInQuarterHoursGenerator, 3)

  @ParameterizedTest
  @MethodSource("scenarios")
  fun testFinder(
    heading: String,
    specification: VideoLinkBookingSearchSpecification,
    schedule: List<ScheduledAppointmentSearchDto>,
    matchExpected: Boolean,
    expectedAlternatives: Iterable<VideoLinkBookingOption> = emptyList()
  ) {
    val options = finder.findOptions(specification, schedule)
    assertAll(
      heading,
      { assertThat(options.matched).isEqualTo(matchExpected) },
      { assertThat(options.alternatives).containsExactlyElementsOf(expectedAlternatives) }
    )
  }

  fun scenarios(): Stream<Arguments> =
    Stream.of(
      arguments(
        "Main appointment is available when a room is not booked",
        specification(main = from(11, 0).until(11, 30).inRoom(Room1)),
        emptySchedule(),
        true,
        noOptionsExpected()
      ),

      arguments(
        "Room is occupied all day: No match and no alternatives",
        specification(main = from(11, 0).until(11, 30).inRoom(Room1)),
        roomOccupiedAllDay(Room1),
        false,
        noOptionsExpected()
      ),

      arguments(
        "Room booked contiguously before spec: Match expected",
        specification(main = from(11, 0).until(11, 30).inRoom(Room1)),
        schedule(
          room(Room1).from(10, 50).until(11, 0)
        ),
        true,
        noOptionsExpected()
      ),

      arguments(
        "Room booked contiguously after spec: Match expected",
        specification(main = from(11, 0).until(11, 30).inRoom(Room1)),
        schedule(
          room(Room1).from(11, 30).until(11, 40)
        ),
        true,
        noOptionsExpected()
      ),

      arguments(
        "Room booked contiguously before and after spec: Match expected",
        specification(main = from(11, 0).until(11, 30).inRoom(Room1)),
        schedule(
          room(Room1).from(10, 50).until(11, 0),
          room(Room1).from(11, 30).until(11, 40)
        ),
        true,
        noOptionsExpected()
      ),

      arguments(
        "Spec brackets a scheduled appointment: Three alternatives offered.",
        specification(main = from(11, 0).until(11, 30).inRoom(Room1)),
        schedule(
          room(Room1).from(11, 10).until(11, 20)
        ),
        false,
        expectedOptions(
          option(main = from(10, 15).until(10, 45).inRoom(Room1)),
          option(main = from(10, 30).until(11, 0).inRoom(Room1)),
          option(main = from(11, 30).until(12, 0).inRoom(Room1))
        )
      ),

      arguments(
        "Room fully booked upto and including appointment time. Alternatives offered after spec",
        specification(main = from(11, 0).until(11, 30).inRoom(Room1)),
        schedule(
          room(Room1).from(startOfDay).until(11, 10)
        ),
        false,
        expectedOptions(
          option(main = from(11, 15).until(11, 45).inRoom(Room1)),
          option(main = from(11, 30).until(12, 0).inRoom(Room1)),
          option(main = from(11, 45).until(12, 15).inRoom(Room1))
        )
      ),

      arguments(
        "Room fully booked from appointment time to end of day. Alternatives offered preceding spec.",
        specification(main = from(11, 0).until(11, 30).inRoom(Room1)),
        schedule(
          room(Room1).from(11, 0).until(endOfDay)
        ),
        false,
        expectedOptions(
          option(main = from(10, 0).until(10, 30).inRoom(Room1)),
          option(main = from(10, 15).until(10, 45).inRoom(Room1)),
          option(main = from(10, 30).until(11, 0).inRoom(Room1))
        )
      ),

      arguments(
        "pre, main and post appointments in different rooms, no scheduled appointments. Match expected",
        specification(
          pre = from(10, 50).until(11, 0).inRoom(Room2),
          main = from(11, 0).until(11, 30).inRoom(Room1),
          post = from(11, 30).until(11, 40).inRoom(Room3)
        ),
        emptySchedule(),
        true,
        noOptionsExpected()
      ),

      arguments(
        "pre, main and post appointments in different rooms, pre appointment room fully booked: No match, no alternatives.",
        specification(
          pre = from(10, 50).until(11, 0).inRoom(Room2),
          main = from(11, 0).until(11, 30).inRoom(Room1),
          post = from(11, 30).until(11, 40).inRoom(Room3)
        ),
        roomOccupiedAllDay(Room2),
        false,
        noOptionsExpected()
      ),

      arguments(
        "pre, main and post appointments in different rooms, post appointment room fully booked: No match, no alternatives.",
        specification(
          pre = from(10, 50).until(11, 0).inRoom(Room2),
          main = from(11, 0).until(11, 30).inRoom(Room1),
          post = from(11, 30).until(11, 40).inRoom(Room3)
        ),
        roomOccupiedAllDay(Room3),
        false,
        noOptionsExpected()
      ),

      arguments(
        "pre, main and post appointments in different rooms, pre room occupied: alternatives offered",
        specification(
          pre = from(11, 45).until(12, 0).inRoom(Room2),
          main = from(12, 0).until(12, 30).inRoom(Room1),
          post = from(12, 30).until(12, 45).inRoom(Room3)
        ),
        schedule(
          room(Room2).from(11, 45).until(12, 0)
        ),
        false,
        expectedOptions(
          option(
            pre = from(11, 15).until(11, 30).inRoom(Room2),
            main = from(11, 30).until(12, 0).inRoom(Room1),
            post = from(12, 0).until(12, 15).inRoom(Room3)
          ),
          option(
            pre = from(11, 30).until(11, 45).inRoom(Room2),
            main = from(11, 45).until(12, 15).inRoom(Room1),
            post = from(12, 15).until(12, 30).inRoom(Room3)
          ),
          option(
            pre = from(12, 0).until(12, 15).inRoom(Room2),
            main = from(12, 15).until(12, 45).inRoom(Room1),
            post = from(12, 45).until(13, 0).inRoom(Room3)
          )
        )
      ),

      arguments(
        "pre, main and post appointments in different rooms, post room occupied: alternatives offered",
        specification(
          pre = from(11, 45).until(12, 0).inRoom(Room2),
          main = from(12, 0).until(12, 30).inRoom(Room1),
          post = from(12, 30).until(12, 45).inRoom(Room3)
        ),
        schedule(
          room(Room3).from(12, 30).until(12, 45)
        ),
        false,
        expectedOptions(
          option(
            pre = from(11, 15).until(11, 30).inRoom(Room2),
            main = from(11, 30).until(12, 0).inRoom(Room1),
            post = from(12, 0).until(12, 15).inRoom(Room3)
          ),
          option(
            pre = from(11, 30).until(11, 45).inRoom(Room2),
            main = from(11, 45).until(12, 15).inRoom(Room1),
            post = from(12, 15).until(12, 30).inRoom(Room3)
          ),
          option(
            pre = from(12, 0).until(12, 15).inRoom(Room2),
            main = from(12, 15).until(12, 45).inRoom(Room1),
            post = from(12, 45).until(13, 0).inRoom(Room3)
          )
        )
      ),

      arguments(
        "pre, main and post appointments in different rooms, all rooms occupied except at times in spec: Spec matched",
        specification(
          pre = from(11, 45).until(12, 0).inRoom(Room2),
          main = from(12, 0).until(12, 30).inRoom(Room1),
          post = from(12, 30).until(12, 45).inRoom(Room3)
        ),
        schedule(
          room(Room2).from(startOfDay).until(11, 45),
          room(Room2).from(12, 0).until(endOfDay),

          room(Room1).from(startOfDay).until(12, 0),
          room(Room1).from(12, 30).until(endOfDay),

          room(Room3).from(startOfDay).until(12, 30),
          room(Room3).from(12, 45).until(endOfDay)
        ),
        true,
        noOptionsExpected()
      ),

      arguments(
        "pre, main and post appointments in different rooms each room occupied at different times forcing alternatives to be spread widely",
        specification(
          pre = from(11, 45).until(12, 0).inRoom(Room2),
          main = from(12, 0).until(12, 30).inRoom(Room1),
          post = from(12, 30).until(12, 45).inRoom(Room3)
        ),
        schedule(
          room(Room2).from(10, 15).until(12, 0),
          room(Room1).from(12, 0).until(14, 0),
          room(Room3).from(14, 45).until(15, 45)
        ),
        false,
        expectedOptions(
          option(
            pre = from(10, 0).until(10, 15).inRoom(Room2),
            main = from(10, 15).until(10, 45).inRoom(Room1),
            post = from(10, 45).until(11, 0).inRoom(Room3)
          ),
          option(
            pre = from(13, 45).until(14, 0).inRoom(Room2),
            main = from(14, 0).until(14, 30).inRoom(Room1),
            post = from(14, 30).until(14, 45).inRoom(Room3)
          ),
          option(
            pre = from(15, 0).until(15, 15).inRoom(Room2),
            main = from(15, 15).until(15, 45).inRoom(Room1),
            post = from(15, 45).until(16, 0).inRoom(Room3)
          )
        )
      ),

      arguments(
        "pre, main and post appointments in different rooms. Rooms occupation prevents match. Only 2 alternatives available.",
        specification(
          pre = from(11, 45).until(12, 0).inRoom(Room2),
          main = from(12, 0).until(12, 30).inRoom(Room1),
          post = from(12, 30).until(12, 45).inRoom(Room3)
        ),
        schedule(
          room(Room2).from(10, 15).until(12, 0),
          room(Room1).from(12, 0).until(14, 0),
          room(Room3).from(14, 45).until(16, 0)
        ),
        false,
        expectedOptions(
          option(
            pre = from(10, 0).until(10, 15).inRoom(Room2),
            main = from(10, 15).until(10, 45).inRoom(Room1),
            post = from(10, 45).until(11, 0).inRoom(Room3)
          ),
          option(
            pre = from(13, 45).until(14, 0).inRoom(Room2),
            main = from(14, 0).until(14, 30).inRoom(Room1),
            post = from(14, 30).until(14, 45).inRoom(Room3)
          )
        )
      ),

      arguments(
        "pre, and main appointments in different rooms. Rooms occupation prevents match. Only 2 alternatives available.",
        specification(
          pre = from(11, 45).until(12, 0).inRoom(Room2),
          main = from(12, 0).until(12, 30).inRoom(Room1)
        ),
        schedule(
          room(Room2).from(10, 15).until(12, 0),
          room(Room1).from(12, 0).until(14, 0),
          room(Room1).from(14, 30).until(endOfDay)
        ),
        false,
        expectedOptions(
          option(
            pre = from(10, 0).until(10, 15).inRoom(Room2),
            main = from(10, 15).until(10, 45).inRoom(Room1)
          ),
          option(
            pre = from(13, 45).until(14, 0).inRoom(Room2),
            main = from(14, 0).until(14, 30).inRoom(Room1)
          )
        )
      ),

      arguments(
        "main and post appointments in different rooms. Rooms occupation prevents match. Only 1 alternative available.",
        specification(
          main = from(12, 0).until(12, 30).inRoom(Room1),
          post = from(12, 30).until(12, 45).inRoom(Room3)
        ),
        schedule(
          room(Room1).from(startOfDay).until(14, 0),
          room(Room3).from(14, 45).until(16, 0)
        ),
        false,
        expectedOptions(
          option(
            main = from(14, 0).until(14, 30).inRoom(Room1),
            post = from(14, 30).until(14, 45).inRoom(Room3)
          )
        )
      ),

      arguments(
        "pre, main and post appointments in same room with gaps. schedule matches exactly",
        specification(
          pre = from(11, 45).until(12, 0).inRoom(Room1),
          main = from(12, 15).until(12, 30).inRoom(Room1),
          post = from(12, 45).until(13, 0).inRoom(Room1)
        ),
        schedule(
          room(Room1).from(startOfDay).until(11, 45),
          room(Room1).from(12, 0).until(12, 15),
          room(Room1).from(12, 30).until(12, 45),
          room(Room1).from(13, 0).until(endOfDay)
        ),
        true,
        noOptionsExpected()
      )
    )

  companion object {
    val appointmentDate: LocalDate = LocalDate.of(2021, Month.MAY, 1)
    val startOfDay: LocalTime = time(10, 0)
    val endOfDay: LocalTime = time(16, 0)

    private val agencyId = "AGYID"

    private const val Room1 = 1L
    private const val Room2 = 2L
    private const val Room3 = 3L

    val tenUntilFourInQuarterHoursGenerator = OptionsGenerator(
      dayStart = startOfDay,
      dayEnd = endOfDay,
      step = Duration.ofMinutes(15)
    )

    private fun schedule(vararg scheduleAppointments: ScheduledAppointmentSearchDto) = scheduleAppointments.asList()
    private fun emptySchedule() = schedule()

    private fun roomOccupiedAllDay(locationId: Long) = schedule(
      room(locationId).from(startOfDay).until(endOfDay)
    )

    private fun expectedOptions(vararg options: VideoLinkBookingOption) = options.asList()
    private fun noOptionsExpected(): List<VideoLinkBookingOption> = expectedOptions()

    private fun time(h: Int, m: Int) = LocalTime.of(h, m)

    private fun specification(
      pre: LocationAndInterval? = null,
      main: LocationAndInterval,
      post: LocationAndInterval? = null
    ) =
      VideoLinkBookingSearchSpecification(
        agencyId = agencyId,
        date = appointmentDate,
        preAppointment = pre,
        mainAppointment = main,
        postAppointment = post
      )

    fun option(pre: LocationAndInterval? = null, main: LocationAndInterval, post: LocationAndInterval? = null) =
      VideoLinkBookingOption(pre, main, post)
  }
}

data class AppointmentBuilder(
  var start: LocalTime,
  var end: LocalTime? = null,
  var locationId: Long? = null
) {
  fun until(h: Int, m: Int) = apply { this.end = LocalTime.of(h, m) }
  fun inRoom(locationId: Long) = LocationAndInterval(locationId, Interval(start, end!!))

  companion object {
    fun from(h: Int, m: Int) = AppointmentBuilder(LocalTime.of(h, m))
  }
}

data class ScheduledAppointmentDtoBuilder(
  var start: LocalTime? = null,
  var end: LocalTime? = null,
  var locationId: Long
) {
  fun from(h: Int, m: Int) = apply { this.start = LocalTime.of(h, m) }
  fun from(time: LocalTime) = apply { this.start = time }
  fun until(h: Int, m: Int) = until(LocalTime.of(h, m))
  fun until(time: LocalTime) = ScheduledAppointmentSearchDto(
    id = 999L,
    agencyId = "AGYID",
    locationId = locationId,
    appointmentTypeCode = "XXX",
    startTime = VideoLinkBookingOptionsFinderTest.appointmentDate.atTime(start),
    endTime = VideoLinkBookingOptionsFinderTest.appointmentDate.atTime(time),
    offenderNo = DONT_CARE,
    locationDescription = DONT_CARE,
    lastName = DONT_CARE,
    firstName = DONT_CARE,
    createUserId = DONT_CARE,
    appointmentTypeDescription = DONT_CARE
  )

  companion object {
    const val DONT_CARE = "Don't care"

    fun room(locationId: Long) = ScheduledAppointmentDtoBuilder(locationId = locationId)
  }
}
