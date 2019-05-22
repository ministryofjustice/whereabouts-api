package uk.gov.justice.digital.hmpps.whereabouts.repository


import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate
import javax.validation.ConstraintViolationException

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
open class AttendanceRepositoryTest {

    @Autowired
    lateinit var attendanceRepository: AttendanceRepository

    @Test
    fun `should insert attendance without error` () {
        attendanceRepository.save(Attendance.builder()
                .attended(true)
                .paid(true)
                .eventDate(LocalDate.now())
                .eventId(1)
                .eventLocationId(1)
                .absentReason(AbsentReason.Refused)
                .prisonId("LEI")
                .period(TimePeriod.AM)
                .build()
        )
    }

    @Test(expected = ConstraintViolationException::class)
    fun `should throw error on missing fields` () {
            attendanceRepository.save(Attendance.builder().build())
    }

}
