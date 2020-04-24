package uk.gov.justice.digital.hmpps.whereabouts.repository

import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.model.*
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.time.LocalDate


@ActiveProfiles("test")
@Import(TestAuditConfiguration::class)
@DataJpaTest
@Transactional
class AttendanceChangesRepositoryTest {

  @MockBean
  lateinit var authenticationFacade: AuthenticationFacade

  @Autowired
  lateinit var attendanceChangesRepository: AttendanceChangesRepository

  @Autowired
  lateinit var attendanceRepository: AttendanceRepository

  @BeforeEach
  fun clearRepository() {
    whenever(authenticationFacade.currentUsername).thenReturn("user")

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
  }

  @Test
  fun `should be able to retrieve changes from the database`() {
    val attendance = Attendance.builder()
        .attended(true)
        .paid(true)
        .bookingId(121)
        .eventDate(LocalDate.now())
        .eventId(1)
        .eventLocationId(1)
        .absentReason(AbsentReason.NotRequired)
        .prisonId("LEI")
        .period(TimePeriod.AM)
        .build()

    val attendanceId = attendanceRepository.save(attendance).id

    val change = AttendanceChange(
         attendanceId = attendanceId,
         changedFrom = AttendanceChangeValues.Refused,
         changedTo = AttendanceChangeValues.NotRequired
     )

    val id = attendanceChangesRepository.save(change).id
    TestTransaction.flagForCommit()
    TestTransaction.end()

    val recordedChanges = attendanceChangesRepository.findById(id).get()

    assertThat(recordedChanges.attendanceId).isEqualTo(attendanceId)
    assertThat(recordedChanges.changedFrom).isEqualTo(AbsentReason.Refused)
    assertThat(recordedChanges.changedTo).isEqualTo(AbsentReason.NotRequired)
    assertThat(recordedChanges.createUserId).isEqualTo("user")
  }
}
