package uk.gov.justice.digital.hmpps.whereabouts.repository

import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.config.AuditConfiguration
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@Import(AuditConfiguration::class)
@DataJpaTest
@Transactional
class AttendanceRepositoryTest {

  @MockBean
  lateinit var authenticationFacade: AuthenticationFacade

  @Autowired
  lateinit var attendanceRepository: AttendanceRepository

  @Autowired
  lateinit var attendanceChangesRepository: AttendanceChangesRepository

  private val now = LocalDate.now()

  private val attendance = Attendance.builder()
    .attended(true)
    .paid(true)
    .bookingId(121)
    .eventDate(LocalDate.now())
    .eventId(1)
    .eventLocationId(1)
    .absentReason(AbsentReason.UnacceptableAbsenceIncentiveLevelWarning)
    .absentSubReason(AbsentSubReason.ExternalMoves)
    .prisonId("LEI")
    .period(TimePeriod.AM)
    .build()

  @BeforeEach
  fun clearRepository() {
    whenever(authenticationFacade.currentUsername).thenReturn("user")

    attendanceChangesRepository.deleteAll()
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
    assertThat(savedAttendance.absentReason).isEqualTo(AbsentReason.UnacceptableAbsenceIncentiveLevelWarning)
    assertThat(savedAttendance.absentSubReason).isEqualTo(AbsentSubReason.ExternalMoves)
    assertThat(savedAttendance.prisonId).isEqualToIgnoringCase("LEI")
    assertThat(savedAttendance.period).isEqualTo(TimePeriod.AM)

    assertThat(savedAttendance.createUserId).isEqualTo("user")
    assertThat(savedAttendance.createDateTime.toLocalDate()).isEqualTo(now)
  }

  @Test
  fun `should throw error on missing fields`() {
    assertThatThrownBy {
      attendanceRepository.save(Attendance.builder().build())
    }.isInstanceOf(ConstraintViolationException::class.java)
  }

  @Test
  fun `should find attendance by date range`() {
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
      .period(TimePeriod.AM)
      .eventDate(LocalDate.now().minusMonths(1))
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

    attendanceRepository.saveAll(
      setOf(
        attendanceLastMonth,
        attendanceLastYear,
        attendanceToday
      )
    )

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

    attendanceRepository.saveAll(
      setOf(
        attendanceLastMonth,
        attendanceLastYear,
        attendanceToday,
        attendanceED
      )
    )

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
        .absentSubReason(AbsentSubReason.Courses)
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
      .findByPrisonIdAndEventDateBetweenAndPeriodInAndAbsentReason(
        "MDI",
        LocalDate.now(),
        LocalDate.now().plusMonths(12),
        setOf(TimePeriod.AM),
        AbsentReason.Refused
      )

    assertThat(result).extracting("bookingId", "eventId")
      .containsExactlyInAnyOrder(Tuple.tuple(1L, 2L), Tuple.tuple(1L, 1L))
  }

  @Test
  fun `should match on date range, period and booking ids`() {
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
        .eventDate(LocalDate.now().plusMonths(13))
        .prisonId("MDI")
        .period(TimePeriod.AM)
        .attended(false)
        .paid(true)
        .absentReason(AbsentReason.SessionCancelled)
        .build(),
      Attendance.builder()
        .bookingId(2)
        .eventId(3)
        .eventLocationId(1)
        .eventDate(LocalDate.now().plusMonths(1))
        .prisonId("MDI")
        .period(TimePeriod.AM)
        .attended(false)
        .paid(true)
        .absentReason(AbsentReason.SessionCancelled)
        .build(),
      Attendance.builder()
        .bookingId(3)
        .eventId(3)
        .eventLocationId(1)
        .eventDate(LocalDate.now().plusMonths(1))
        .prisonId("MDI")
        .period(TimePeriod.PM)
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
      .findByPrisonIdAndBookingIdInAndEventDateBetweenAndPeriodIn(
        "MDI",
        setOf(1, 2),
        LocalDate.now(),
        LocalDate.now().plusMonths(12),
        setOf(TimePeriod.AM, TimePeriod.PM)
      )

    assertThat(result).extracting("bookingId", "eventId")
      .containsExactlyInAnyOrder(Tuple.tuple(1L, 2L), Tuple.tuple(1L, 1L), Tuple.tuple(2L, 3L))
  }
}
