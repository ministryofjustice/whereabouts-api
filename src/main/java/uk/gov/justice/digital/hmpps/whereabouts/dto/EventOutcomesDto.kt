package uk.gov.justice.digital.hmpps.whereabouts.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventOutcomesDto(
  val eventOutcome: String,
  val performance: String? = null,
  val outcomeComment: String? = null,
  val bookingActivities: Set<BookingActivity>
)
