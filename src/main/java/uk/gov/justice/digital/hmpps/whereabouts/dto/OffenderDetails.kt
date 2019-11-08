package uk.gov.justice.digital.hmpps.whereabouts.dto

import java.time.LocalDate

data class OffenderDetails(
    val bookingId: Long? = null,
    val offenderNo: String? = null,
    val eventId: Long? = null,
    val cellLocation: String? = null,
    val eventDate: LocalDate? = null,
    val timeSlot: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val comment: String? = null
)
