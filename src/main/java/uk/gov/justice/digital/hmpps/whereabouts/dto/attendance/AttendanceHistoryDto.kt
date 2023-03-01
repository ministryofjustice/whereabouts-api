package uk.gov.justice.digital.hmpps.whereabouts.dto.attendance

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate

@ApiModel(description = "Attendance data")
data class AttendanceHistoryDto(
  @JsonFormat(pattern = "yyyy-MM-dd")
  @ApiModelProperty(required = true, value = "Date the event occurred", example = "2021-10-01")
  val eventDate: LocalDate,

  @ApiModelProperty(required = true, value = "Activity name", example = "Industries - Food Packing")
  val activity: String?,

  @ApiModelProperty(required = true, value = "Activity description", example = "Workshop 7")
  val activityDescription: String?,

  @ApiModelProperty(required = true, value = "Prison.", example = "MDI")
  val location: String?,

  @ApiModelProperty(
    value = "Any activity outcome captured (n.b. for outcomes captured via Whereabouts, this is also the Case note text)",
    example = "Healthcare issue - speak to SO",
  )
  val comments: String? = null,
)
