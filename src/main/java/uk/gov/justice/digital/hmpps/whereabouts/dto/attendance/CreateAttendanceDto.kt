package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentReason
import uk.gov.justice.digital.hmpps.whereabouts.model.AbsentSubReason
import uk.gov.justice.digital.hmpps.whereabouts.model.TimePeriod
import java.time.LocalDate

@ApiModel(description = "Create an attendance for a booking")
data class CreateAttendanceDto(
  @ApiModelProperty(required = true, value = "Id of active booking", example = "1")
  @field:NotNull
  val bookingId: Long? = null,

  @ApiModelProperty(required = true, value = "Id of event", example = "2")
  @field:NotNull
  val eventId: Long? = null,

  @ApiModelProperty(required = true, value = "Id of the location the event is taking place", example = "4")
  @field:NotNull
  val eventLocationId: Long? = null,

  @ApiModelProperty(required = true, value = "Time period for the event", example = "AM")
  @field:NotNull
  val period: TimePeriod? = null,

  @ApiModelProperty(required = true, value = "Id of prison the event is taking place", example = "LEI")
  @field:NotNull
  val prisonId: String? = null,

  @ApiModelProperty(required = true, value = "Flag to indicate the offender attended the event", example = "true")
  @field:NotNull
  val attended: Boolean? = null,

  @ApiModelProperty(required = true, value = "Flag to indicate the offender should be paid", example = "true")
  @field:NotNull
  val paid: Boolean? = null,

  @ApiModelProperty(value = "Reason the offender did not attendance the event", example = "Refused")
  val absentReason: AbsentReason? = null,

  @ApiModelProperty(value = "Absence reason the offender did not attendance the event", example = "Courses")
  val absentSubReason: AbsentSubReason? = null,

  @JsonFormat(pattern = "yyyy-MM-dd")
  @ApiModelProperty(required = true, value = "Date the event is scheduled", example = "2019-10-01")
  @field:NotNull
  val eventDate: LocalDate? = null,

  @ApiModelProperty(value = "Comments about non attendance. This also gets used for the IEP warning text ")
  @Size(max = 240)
  val comments: String? = null
)
