package uk.gov.justice.digital.hmpps.whereabouts.services

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import uk.gov.justice.digital.hmpps.whereabouts.dto.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.dto.elite.CaseNoteDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import java.time.LocalDate
import java.time.LocalDateTime

class IEPWarningServiceTest {


  private val prisonApiService: PrisonApiService = mock()
  private val caseNotesService: CaseNotesService = mock()

  private val today: LocalDate = LocalDate.now()

  private lateinit var service: IEPWarningService

  @BeforeEach
  fun before() {
    service = IEPWarningService(caseNotesService, prisonApiService)
  }

  @Test
  fun `should create a negative case note using user supplied comment`() {

    val date = LocalDate.of(2019, 10, 10)

    whenever(caseNotesService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
        .thenReturn(CaseNoteDto.builder().caseNoteId(100L).build())

    whenever(prisonApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

    service.postIEPWarningIfRequired(1, null, AbsentReason.RefusedIncentiveLevelWarning, "test comment", date)

    verify(caseNotesService)
        .postCaseNote(
            eq("AB1234C"),
            eq("NEG"),
            eq("IEP_WARN"),
            eq("Refused - Incentive Level warning - test comment"),
            eq(date.atStartOfDay()))
  }


  @Test
  fun `should post IEP reinstated case note amendment if going from unpaid (IEP warning) to paid attendance (IEP rescinded) to unpaid absent unacceptable`() {
    whenever(prisonApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

    val attendance = Attendance.builder()
        .bookingId(1)
        .eventLocationId(1)
        .eventId(1)
        .paid(true)
        .attended(true)
        .eventDate(today)
        .caseNoteId(1)
        .build()

    val updateAttendance =  UpdateAttendanceDto(
        attended=false,
        paid=false,
        absentReason=AbsentReason.UnacceptableAbsence,
        comments="Unacceptable absence - No show."
    )

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService)
        .putCaseNoteAmendment("AB1234C", 1, "Incentive Level warning reinstated: Unacceptable absence")
  }

  @Test
  fun `should not post a case note amendment going from paid attendance to unpaid absent refused`() {

    whenever(caseNotesService.postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java)))
        .thenReturn(CaseNoteDto.builder().caseNoteId(1).build())
    whenever(prisonApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

    val attendance = Attendance.builder()
        .bookingId(1)
        .eventLocationId(1)
        .eventId(1)
        .paid(true)
        .attended(true)
        .eventDate(today)
        .build()

    val updateAttendance = UpdateAttendanceDto(
        attended=false,
        paid=false,
        absentReason=AbsentReason.RefusedIncentiveLevelWarning,
        comments="Refused!"
    )

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService, never()).putCaseNoteAmendment(anyString(), anyLong(), anyString())
    verify(caseNotesService).postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
  }

  @Test
  fun `should sentence case absent reasons`() {
    whenever(prisonApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

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

    val updateAttendance =  UpdateAttendanceDto(
        attended=false,
        paid=true,
        absentReason=AbsentReason.NotRequired,
        comments="not required to work today"
    )

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService).putCaseNoteAmendment("AB1234C", 1, "Incentive Level warning rescinded: Not required")
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

    val updateAttendance = UpdateAttendanceDto(
        attended=false,
        comments="test",
        absentReason=AbsentReason.Refused,
        paid=true
    )

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService, never()).putCaseNoteAmendment(anyString(), anyLong(), anyString())
    verify(caseNotesService, never()).postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
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

    val updateAttendance = UpdateAttendanceDto(
        attended=false,
        absentReason=AbsentReason.NotRequired,
        comments="test",
        paid=true
    )

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService, never()).putCaseNoteAmendment(anyString(), anyLong(), anyString())
    verify(caseNotesService, never()).postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
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

    val updateAttendance = UpdateAttendanceDto(
        attended=false,
        absentReason=AbsentReason.Refused,
        comments="Never turned up",
        paid=false
    )

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService, never()).putCaseNoteAmendment(anyString(), anyLong(), anyString())
    verify(caseNotesService, never()).postCaseNote(anyString(), anyString(), anyString(), anyString(), any(LocalDateTime::class.java))
  }

  @Test
  fun `should post case note amendment going from unpaid absent refused to paid attendance`() {
    whenever(prisonApiService.getOffenderNoFromBookingId(anyLong())).thenReturn("AB1234C")

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
        UpdateAttendanceDto(attended=true, paid=true)

    service.handleIEPWarningScenarios(attendance, updateAttendance)

    verify(caseNotesService)
        .putCaseNoteAmendment("AB1234C", 1, "Incentive Level warning rescinded: attended")
  }

}
