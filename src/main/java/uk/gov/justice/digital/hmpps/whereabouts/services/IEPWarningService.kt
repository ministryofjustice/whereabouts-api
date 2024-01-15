package uk.gov.justice.digital.hmpps.whereabouts.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.dto.attendance.UpdateAttendanceDto
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason
import uk.gov.justice.digital.hmpps.whereabouts.model.Attendance
import java.time.LocalDate
import java.util.Optional

@Service
class IEPWarningService(
  private val caseNotesService: CaseNotesService,
  private val prisonApiService: PrisonApiService,
) {
  private companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun handleIEPWarningScenarios(attendance: Attendance, newAttendanceDetails: UpdateAttendanceDto): Optional<Long> {
    val alreadyTriggeredIEPWarning = attendance.absentReason != null &&
      AbsentReason.iepTriggers.contains(attendance.absentReason)
    val shouldTriggerIEPWarning = newAttendanceDetails.absentReason != null &&
      AbsentReason.iepTriggers.contains(newAttendanceDetails.absentReason)

    if (alreadyTriggeredIEPWarning && shouldTriggerIEPWarning) return Optional.empty()

    val shouldRevokePreviousIEPWarning = attendance.caseNoteId != null && !shouldTriggerIEPWarning
    val shouldReinstatePreviousIEPWarning = attendance.caseNoteId != null && shouldTriggerIEPWarning
    val formattedAbsentReason = newAttendanceDetails.absentReason?.labelWithWarning

    if (shouldRevokePreviousIEPWarning) {
      val rescindedReason =
        "Incentive level warning removed: " + if (newAttendanceDetails.attended) "attended" else formattedAbsentReason
      log.info("{} raised for {}", rescindedReason, attendance.toBuilder().comments(null))
      val offenderNo = prisonApiService.getOffenderNoFromBookingId(attendance.bookingId)
      caseNotesService.putCaseNoteAmendment(offenderNo, attendance.caseNoteId, rescindedReason)
      return Optional.empty()
    }

    if (shouldReinstatePreviousIEPWarning) {
      val reinstatedReason = "Incentive level warning added: $formattedAbsentReason"
      log.info("{} raised for {}", reinstatedReason, attendance.toBuilder().comments(null))
      val offenderNo = prisonApiService.getOffenderNoFromBookingId(attendance.bookingId)
      caseNotesService.putCaseNoteAmendment(offenderNo, attendance.caseNoteId, reinstatedReason)
      return Optional.empty()
    }

    return postIEPWarningIfRequired(
      attendance.bookingId,
      attendance.caseNoteId,
      newAttendanceDetails.absentReason,
      newAttendanceDetails.absentSubReason,
      newAttendanceDetails.comments,
      attendance.eventDate,
    )
  }

  fun postIEPWarningIfRequired(
    bookingId: Long?,
    caseNoteId: Long?,
    reason: AbsentReason?,
    subReason: AbsentSubReason?,
    text: String?,
    eventDate: LocalDate,
  ): Optional<Long> {
    if (caseNoteId == null && reason != null && AbsentReason.iepTriggers.contains(reason)) {
      val offenderNo = prisonApiService.getOffenderNoFromBookingId(bookingId)
      val modifiedTextWithReason = formatReasonAndComment(reason, subReason, text)
      val caseNote = caseNotesService.postCaseNote(
        offenderNo,
        // "Negative Behaviour"
        "NEG",
        // "IEP Warning",
        "IEP_WARN",
        modifiedTextWithReason,
        eventDate.atStartOfDay(),
      )

      log.info("IEP Warning created for bookingId {}", bookingId)
      return Optional.of(caseNote.caseNoteId)
    }
    return Optional.empty()
  }

  private fun formatReasonAndComment(reason: AbsentReason, subReason: AbsentSubReason?, comment: String?): String {
    val caseNoteComment = subReason?.let { "${subReason.label}. $comment" } ?: comment
    return "${reason.labelWithWarning} - $caseNoteComment"
  }
}
