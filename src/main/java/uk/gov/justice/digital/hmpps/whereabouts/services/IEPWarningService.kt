package uk.gov.justice.digital.hmpps.whereabouts.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import uk.gov.justice.digital.hmpps.whereabouts.utils.AbsentReasonFormatter
import java.time.LocalDate
import java.util.*

@Service
open class IEPWarningService(private val caseNotesService: CaseNotesService, private val elite2ApiService: Elite2ApiService) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  open fun handleIEPWarningScenarios(attendance: Attendance, newAttendanceDetails: UpdateAttendanceDto): Optional<Long> {
    val alreadyTriggeredIEPWarning = attendance.absentReason != null &&
        AbsentReason.getIepTriggers().contains(attendance.absentReason)

    val shouldTriggerIEPWarning = newAttendanceDetails.absentReason != null &&
        AbsentReason.getIepTriggers().contains(newAttendanceDetails.absentReason)

    if (alreadyTriggeredIEPWarning && shouldTriggerIEPWarning) return Optional.empty()

    val shouldRevokePreviousIEPWarning = attendance.caseNoteId != null && !shouldTriggerIEPWarning
    val shouldReinstatePreviousIEPWarning = attendance.caseNoteId != null && shouldTriggerIEPWarning
    val formattedAbsentReason = if (newAttendanceDetails.absentReason != null) AbsentReasonFormatter.titlecase(newAttendanceDetails.absentReason.toString()) else null


    if (shouldRevokePreviousIEPWarning) {
      val rescindedReason = "IEP rescinded: " + if (newAttendanceDetails.attended) "attended" else formattedAbsentReason
      log.info("{} raised for {}", rescindedReason, attendance.toBuilder().comments(null))
      val offenderNo = elite2ApiService.getOffenderNoFromBookingId(attendance.bookingId)
      caseNotesService.putCaseNoteAmendment(offenderNo, attendance.caseNoteId, rescindedReason)
      return Optional.empty()
    }

    if (shouldReinstatePreviousIEPWarning) {
      val reinstatedReason = "IEP reinstated: $formattedAbsentReason"
      log.info("{} raised for {}", reinstatedReason, attendance.toBuilder().comments(null))
      val offenderNo = elite2ApiService.getOffenderNoFromBookingId(attendance.bookingId)
      caseNotesService.putCaseNoteAmendment(offenderNo, attendance.caseNoteId, reinstatedReason)
      return Optional.empty()
    }

    return postIEPWarningIfRequired(
        attendance.bookingId,
        attendance.caseNoteId,
        newAttendanceDetails.absentReason,
        newAttendanceDetails.comments,
        attendance.eventDate
    )
  }

  open fun postIEPWarningIfRequired(bookingId: Long?, caseNoteId: Long?, reason: AbsentReason?, text: String?, eventDate: LocalDate): Optional<Long> {
    if (caseNoteId == null && reason != null && AbsentReason.getIepTriggers().contains(reason)) {
      log.info("IEP Warning created for bookingId {}", bookingId)
      val offenderNo = elite2ApiService.getOffenderNoFromBookingId(bookingId)
      val modifiedTextWithReason = AbsentReasonFormatter.titlecase(reason.toString()) + " - " + text
      val caseNote = caseNotesService.postCaseNote(
          offenderNo,
          "NEG",  //"Negative Behaviour"
          "IEP_WARN",  //"IEP Warning",
          modifiedTextWithReason,
          eventDate.atStartOfDay())
      return Optional.of(caseNote.caseNoteId)
    }
    return Optional.empty()
  }
}
