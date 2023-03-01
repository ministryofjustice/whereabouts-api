package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import io.swagger.annotations.ApiModelProperty
import jakarta.validation.constraints.NotNull
import java.time.Duration

data class LocationAndInterval(
  @field:NotNull
  val locationId: Long,

  @ApiModelProperty(value = "If present find the locations that can be used for the pre interval.", required = false)
  @field:ValidInterval
  val interval: Interval,
) {
  fun shift(duration: Duration): LocationAndInterval = copy(
    interval = Interval(interval.start.plus(duration), interval.end.plus(duration)),
  )
}
