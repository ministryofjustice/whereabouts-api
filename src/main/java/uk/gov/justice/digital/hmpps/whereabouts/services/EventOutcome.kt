package uk.gov.justice.digital.hmpps.whereabouts.services

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventOutcome (
    val eventOutcome: String? = null,
    val performance: String? = null,
    val outcomeComment: String? = null
)
