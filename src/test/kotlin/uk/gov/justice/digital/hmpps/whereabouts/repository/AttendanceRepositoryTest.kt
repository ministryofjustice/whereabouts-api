package uk.gov.justice.digital.hmpps.whereabouts.repository


import org.assertj.core.api.Assertions.assertThat
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

  @Test
  fun `should insert attendance`() {
    SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("user", "pw")

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

}
