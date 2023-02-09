package uk.gov.justice.digital.hmpps.whereabouts.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class OffenderBooking(
  val bookingId: Long,
  val bookingNo: @NotBlank String,
  val offenderNo: @NotBlank String,
  val firstName: @NotBlank String,
  val lastName: @NotBlank String,
  val agencyId: @NotBlank String,
  val dateOfBirth: @NotNull LocalDate,
  val assignedLivingUnitId: Long?,
  val assignedLivingUnitDesc: String?
)
