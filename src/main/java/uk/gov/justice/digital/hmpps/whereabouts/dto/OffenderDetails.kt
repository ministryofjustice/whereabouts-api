package uk.gov.justice.digital.hmpps.whereabouts.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class OffenderDetails(
    val bookingId: Long? = null,
    val eventId: Long? = null,
    val cellLocation: String? = null,
    val eventDate: LocalDate? = null,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,
    val timeSlot: String? = null
)
