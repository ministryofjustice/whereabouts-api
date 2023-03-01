package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import io.swagger.annotations.ApiModelProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class VideoLinkBookingSearchSpecification(
  @ApiModelProperty(value = "The locations must be within the agency (prison) having this identifier.", example = "WWI")
  @field:NotBlank
  val agencyId: String,

  @ApiModelProperty(value = "The appointment intervals are all on this date.", example = "2021-01-01")
  val date: LocalDate,

  @ApiModelProperty(value = "If present specifies the desired pre-appointment start, end and location.")
  @field:Valid
  val preAppointment: LocationAndInterval? = null,

  @ApiModelProperty(value = "Specifies the desired main apointment start, end and location.", required = true)
  @field:Valid
  val mainAppointment: LocationAndInterval,

  @ApiModelProperty(value = "If present specifies the desired post-appointment start, end and location.")
  @field:Valid
  val postAppointment: LocationAndInterval? = null,

  @ApiModelProperty(
    value = "When checking that the appointment locations and intervals are free, or when searching for alternatives treat appointments for this video link booking as free",
    example = "[1,3,5]"
  )
  val vlbIdToExclude: Long? = null
)
