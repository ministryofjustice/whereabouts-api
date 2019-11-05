package uk.gov.justice.digital.hmpps.whereabouts.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class OffenderDetails(
    val bookingId: Long,
    val eventId: Long,
    val cellLocation: String? = null,
    val eventDate: LocalDate,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val timeSlot: String
)