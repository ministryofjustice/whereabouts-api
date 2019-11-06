package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.ConstraintViolationException


@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
open class AttendanceRepositoryTest {

  @Autowired
  lateinit var attendanceRepository: AttendanceRepository

  private val now = LocalDate.now()

  private val attendance = Attendance.builder()
      .attended(true)
      .paid(true)
      .bookingId(121)
      .eventDate(LocalDate.now())
      .eventId(1)
      .eventLocationId(1)
      .absentReason(AbsentReason.Refused)
      .prisonId("LEI")
      .period(TimePeriod.AM)
      .build()


  @Before
  fun clearRepository() {
    SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("user", "pw")
    attendanceRepository.deleteAll()

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
  }

  @Test
  fun `should insert attendance`() {
    val id = attendanceRepository.save(attendance).id

    TestTransaction.flagForCommit()
    TestTransaction.end()

    val savedAttendance = attendanceRepository.findById(id).get()

    assertThat(savedAttendance.attended).isEqualTo(true)
    assertThat(savedAttendance.paid).isEqualTo(true)
    assertThat(savedAttendance.eventDate).isEqualTo(now)
    assertThat(savedAttendance.eventId).isEqualTo(1)
    assertThat(savedAttendance.eventLocationId).isEqualTo(1)
    assertThat(savedAttendance.absentReason).isEqualTo(AbsentReason.Refused)
    assertThat(savedAttendance.prisonId).isEqualToIgnoringCase("LEI")
    assertThat(savedAttendance.period).isEqualTo(TimePeriod.AM)

    assertThat(savedAttendance.createUserId).isEqualTo("user")
    assertThat(savedAttendance.createDateTime.toLocalDate()).isEqualTo(now)
  }

  @Test(expected = ConstraintViolationException::class)
  fun `should throw error on missing fields`() {
    attendanceRepository.save(Attendance.builder().build())
  }

  @Test
  fun `should find attendance by date range`() {
    val attendanceToday =  Attendance
        .builder()
        .bookingId(1)
        .attended(true)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .eventDate(LocalDate.now())
        .eventId(1)
        .eventLocationId(1)
        .createUserId("test")
        .createDateTime(LocalDateTime.now())
        .build()

    val attendanceLastMonth =  Attendance
        .builder()
        .bookingId(1)
        .attended(true)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .eventDate(LocalDate.now().minusMonths(1))
        .eventId(2)
        .eventLocationId(1)
        .createUserId("test")
        .createDateTime(LocalDateTime.now())
        .build()

    val attendanceLastYear =  Attendance
        .builder()
        .bookingId(1)
        .attended(true)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .eventDate(LocalDate.now().minusYears(1))
        .eventId(3)
        .eventLocationId(1)
        .createUserId("test")
        .createDateTime(LocalDateTime.now())
        .build()

    attendanceRepository.saveAll(setOf(
        attendanceLastMonth,
        attendanceLastYear,
        attendanceToday
    ))

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val results = attendanceRepository
        .findByPrisonIdAndPeriodAndEventDateBetween(
            "LEI",
            TimePeriod.AM,
            LocalDate.now().minusMonths(2),
            LocalDate.now()
        )

    assertThat(results).extracting("eventId").contains(2L, 1L)
  }

  @Test
  fun `should return AM and PM attendances`() {
    val attendanceToday = Attendance
        .builder()
        .bookingId(1)
        .attended(true)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .eventDate(LocalDate.now())
        .eventId(1)
        .eventLocationId(1)
        .createUserId("test")
        .createDateTime(LocalDateTime.now())
        .build()

    val attendanceLastMonth = Attendance
        .builder()
        .bookingId(1)
        .attended(true)
        .prisonId("LEI")
        .period(TimePeriod.PM)
        .eventDate(LocalDate.now())
        .eventId(2)
        .eventLocationId(1)
        .createUserId("test")
        .createDateTime(LocalDateTime.now())
        .build()

    val attendanceLastYear = Attendance
        .builder()
        .bookingId(1)
        .attended(true)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .eventDate(LocalDate.now().minusYears(1))
        .eventId(3)
        .eventLocationId(1)
        .createUserId("test")
        .createDateTime(LocalDateTime.now())
        .build()

    val attendanceED = Attendance
        .builder()
        .bookingId(1)
        .attended(true)
        .prisonId("LEI")
        .period(TimePeriod.ED)
        .eventDate(LocalDate.now())
        .eventId(4)
        .eventLocationId(1)
        .createUserId("test")
        .createDateTime(LocalDateTime.now())
        .build()

    attendanceRepository.saveAll(setOf(
        attendanceLastMonth,
        attendanceLastYear,
        attendanceToday,
        attendanceED
    ))

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val results = attendanceRepository
        .findByPrisonIdAndEventDateBetweenAndPeriodIn(
            "LEI",
            LocalDate.now().minusMonths(2),
            LocalDate.now(),
            setOf(TimePeriod.AM, TimePeriod.PM)
        )

    assertThat(results).extracting("eventId").containsExactly(2L, 1L)
  }

  @Test
  fun `should match on date range, period and absent reason`() {
    val attendances = setOf(
        Attendance.builder()
            .bookingId(1)
            .eventId(1)
            .eventLocationId(1)
            .eventDate(LocalDate.now().atStartOfDay().toLocalDate())
            .prisonId("MDI")
            .period(TimePeriod.AM)
            .attended(false)
            .paid(true)
            .absentReason(AbsentReason.Refused)
            .build(),
        Attendance.builder()
            .bookingId(1)
            .eventId(2)
            .eventLocationId(1)
            .eventDate(LocalDate.now().plusMonths(1))
            .prisonId("MDI")
            .period(TimePeriod.AM)
            .attended(false)
            .paid(true)
            .absentReason(AbsentReason.Refused)
            .build(),
        Attendance.builder()
            .bookingId(1)
            .eventId(3)
            .eventLocationId(1)
            .eventDate(LocalDate.now().plusMonths(1))
            .prisonId("MDI")
            .period(TimePeriod.AM)
            .attended(false)
            .paid(true)
            .absentReason(AbsentReason.SessionCancelled)
            .build()
    )

    attendanceRepository.saveAll(attendances)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val result = attendanceRepository
        .findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason("MDI", LocalDate.now(),
            LocalDate.now().plusMonths(12), setOf(TimePeriod.AM), AbsentReason.Refused)

    assertThat(result).extracting("bookingId", "eventId").containsExactlyInAnyOrder(Tuple.tuple(1L, 2L), Tuple.tuple(1L, 1L))
  }
}
