package uk.gov.justice.digital.hmpps.whereabouts.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class PrisonAppointment(
  val agencyId: String,
  val bookingId: Long,
  val endTime: LocalDateTime? = null,
  val eventId: Long,
  val eventLocationId: Long,
  val eventSubType: String,
  val startTime: LocalDateTime,
  @field:JsonProperty("eventSourceDesc")
  val comment: String?,
  val createUserId: String? = null
)
