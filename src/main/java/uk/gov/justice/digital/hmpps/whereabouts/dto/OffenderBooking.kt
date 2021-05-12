package uk.gov.justice.digital.hmpps.whereabouts.dto

import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

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
