package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import uk.gov.justice.digital.hmpps.whereabouts.dto.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.elite.CaseNoteDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class IEPWarningServiceTest {
  @Mock
  private lateinit var elite2ApiService: Elite2ApiService

  @Mock
  private lateinit var caseNotesService: CaseNotesService

  private val today: LocalDate = LocalDate.now()

  private lateinit var service: IEPWarningService

  @Before
  fun before() {
    service = IEPWarningService(caseNotesService, elite2ApiService)
  }

  @Test
  fun `should create a negative case note using user supplied comment`() {

    val date = LocalDate.of(2019, 10, 10)

    whenever(caseNotesService.postCaseNote(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(LocalDateTime::class.java)))
        .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

    whenever(elite2ApiService.getOffenderNoFromBookingId(Mockito.anyLong())).thenReturn("AB1234C")

    service.postIEPWarningIfRequired(1, null, AbsentReason.Refused, "test comment", date)

    verify(caseNotesService)
        .postCaseNote(
            Mockito.eq("AB1234C"),
            Mockito.eq("NEG"),
            Mockito.eq("IEP_WARN"),
            Mockito.eq("Refused - test comment"),
            Mockito.eq(date.atStartOfDay()))
  }


  @Test
  fun `should post IEP reinstated case note amendment if going from unpaid (IEP warning) to paid attendance (IEP rescinded) to unpaid absent unacceptable`() {
    whenever(elite2ApiService.getOffenderNoFromBookingId(Mockito.anyLong())).thenReturn("AB1234C")

    val attendance = Attendance.builder()
        .bookingId(1)
        .eventLocationId(1)
        .eventId(1)
        .paid(true)
        .attended(true)
        .eventDate(today)
        .caseNoteId(1)
        .build()

    val updateAttendance =  UpdateAttendanceDto.builder()
        .attended(false)
        .paid(false)
        .absentReason(AbsentReason.UnacceptableAbsence)
        .comments("Unacceptable absence - No show.")
        .build()

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService)
        .putCaseNoteAmendment("AB1234C", 1, "IEP reinstated: Unacceptable absence")
  }

  @Test
  fun `should not post a case note amendment going from paid attendance to unpaid absent refused`() {

    whenever(caseNotesService.postCaseNote(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(LocalDateTime::class.java)))
        .thenReturn(CaseNoteDto.builder().caseNoteId(1).build())
    whenever(elite2ApiService.getOffenderNoFromBookingId(Mockito.anyLong())).thenReturn("AB1234C")

    val attendance = Attendance.builder()
        .bookingId(1)
        .eventLocationId(1)
        .eventId(1)
        .paid(true)
        .attended(true)
        .eventDate(today)
        .build()

    val updateAttendance = UpdateAttendanceDto.builder()
        .attended(false)
        .paid(false)
        .absentReason(AbsentReason.Refused)
        .comments("Refused!")
        .build()

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService, Mockito.never()).putCaseNoteAmendment(Mockito.anyString(), Mockito.anyLong(), Mockito.anyString())
    verify(caseNotesService).postCaseNote(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(LocalDateTime::class.java))
  }

  @Test
  fun `should sentence case absent reasons`() {
    whenever(elite2ApiService.getOffenderNoFromBookingId(Mockito.anyLong())).thenReturn("AB1234C")

    val attendance = Attendance.builder()
        .bookingId(1)
        .eventLocationId(1)
        .eventId(1)
        .paid(false)
        .attended(false)
        .caseNoteId(1)
        .absentReason(AbsentReason.Refused)
        .eventDate(today)
        .build()

    val updateAttendance =  UpdateAttendanceDto.builder()
        .attended(false)
        .paid(true)
        .absentReason(AbsentReason.NotRequired)
        .comments("not required to work today")
        .build()

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService).putCaseNoteAmendment("AB1234C", 1, "IEP rescinded: Not required")
  }


  @Test
  fun `should not raise case note or amendment IEP warnings`() {

    val attendance = Attendance.builder()
        .bookingId(1)
        .eventLocationId(1)
        .eventId(1)
        .eventDate(today)
        .paid(true)
        .attended(true)
        .build()

    val updateAttendance = UpdateAttendanceDto.builder()
        .attended(false)
        .comments("test")
        .absentReason(AbsentReason.NotRequired)
        .paid(true)
        .build()

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService, Mockito.never()).putCaseNoteAmendment(Mockito.anyString(), Mockito.anyLong(), Mockito.anyString())
    verify(caseNotesService, Mockito.never()).postCaseNote(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(LocalDateTime::class.java))
  }

  @Test
  fun `should not raise case note or amendment IEP warnings going from unpaid to unpaid`() {

    val attendance = Attendance.builder()
        .bookingId(1)
        .eventLocationId(1)
        .eventId(1)
        .eventDate(today)
        .absentReason(AbsentReason.AcceptableAbsence)
        .paid(false)
        .attended(false)
        .build()

    val updateAttendance = UpdateAttendanceDto.builder()
        .attended(false)
        .absentReason(AbsentReason.NotRequired)
        .comments("test")
        .paid(true)
        .build()

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService, Mockito.never()).putCaseNoteAmendment(Mockito.anyString(), Mockito.anyLong(), Mockito.anyString())
    verify(caseNotesService, Mockito.never()).postCaseNote(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(LocalDateTime::class.java))
  }

  @Test
  fun `should not trigger an IEP warning if one was previously triggered`() {

    val attendance = Attendance.builder()
        .bookingId(1)
        .eventLocationId(1)
        .eventId(1)
        .eventDate(today)
        .paid(false)
        .attended(false)
        .caseNoteId(1)
        .comments("do not overwrite")
        .absentReason(AbsentReason.Refused)
        .build()

    val updateAttendance = UpdateAttendanceDto.builder()
        .attended(false)
        .absentReason(AbsentReason.Refused)
        .comments("Never turned up")
        .paid(false)
        .build()

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService, Mockito.never()).putCaseNoteAmendment(Mockito.anyString(), Mockito.anyLong(), Mockito.anyString())
    verify(caseNotesService, Mockito.never()).postCaseNote(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(LocalDateTime::class.java))
  }

  @Test
  fun `should post case note amendment going from unpaid absent refused to paid attendance`() {
    whenever(elite2ApiService.getOffenderNoFromBookingId(Mockito.anyLong())).thenReturn("AB1234C")

    val attendance =   Attendance.builder()
        .bookingId(1)
        .eventLocationId(1)
        .eventId(1)
        .comments("Refused to turn up")
        .paid(false)
        .attended(false)
        .absentReason(AbsentReason.Refused)
        .eventDate(today)
        .caseNoteId(1)
        .build()

    val updateAttendance =
        UpdateAttendanceDto.builder().attended(true).paid(true).build()

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService)
        .putCaseNoteAmendment("AB1234C", 1, "IEP rescinded: attended")
  }

}
