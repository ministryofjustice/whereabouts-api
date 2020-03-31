package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonFormat
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate

data class AbsenceDto(
    val attendanceId: Long? = null,
    val bookingId: Long? = null,
    val offenderNo: String? = null,

    val eventId: Long? = null,
    val eventLocationId: Long? = null,

    @JsonFormat(pattern = "yyyy-MM-dd")
    val eventDate: LocalDate? = null,

    val period: TimePeriod? = null,
    val reason: AbsentReason? = null,

    val eventDescription: String? = null,
    val comments: String? = null,
    val cellLocation: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val suspended: Boolean? = null
)
