package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import com.fasterxml.jackson.annotation.JsonFormat
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate

data class AbsenceDto(
  val attendanceId: Long?,
  val bookingId: Long?,
  val offenderNo: String?,

  val eventId: Long?,
  val eventLocationId: Long?,

  @JsonFormat(pattern = "yyyy-MM-dd")
  val eventDate: LocalDate?,

  val period: TimePeriod?,
  val reason: AbsentReason?,
  val subReason: AbsentSubReason?,
  val subReasonDescription: String?,

  val eventDescription: String?,
  val comments: String?,
  val cellLocation: String?,
  val firstName: String?,
  val lastName: String?,
  val suspended: Boolean?
)
