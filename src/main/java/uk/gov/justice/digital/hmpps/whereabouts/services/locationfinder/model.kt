package uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import javax.validation.constraints.NotBlank
import kotlin.reflect.KClass

@ApiModel(description = "A specification that returned locations should satisfy.")
data class AppointmentLocationsSpecification(
  @ApiModelProperty(
    value = "The appointment intervals are all on this date. ISO-8601 date format (YYYY-MM-DD)",
    example = "2021-01-01"
  )
  val date: LocalDate,

  @ApiModelProperty(value = "The locations must be within the agency (prison) having this identifier.", example = "WWI")
  @field:NotBlank
  val agencyId: String,

  @ApiModelProperty(
    value = "When searching for locations include locations that are currently part of these Video Link Bookings.",
    example = "[1,3,5]"
  )
  val vlbIdsToExclude: List<Long>,

  @ApiModelProperty(value = "Find the sets of locations that are available for these intervals.")
  val appointmentIntervals: List<@ValidInterval Interval>
)

data class VideoLinkBookingLocationsSpecification(
  @ApiModelProperty(value = "The appointment intervals are all on this date.", example = "2021-01-01")
  val date: LocalDate,

  @ApiModelProperty(value = "The locations must be within the agency (prison) having this identifier.", example = "WWI")
  @field:NotBlank
  val agencyId: String,

  @ApiModelProperty(
    value = "When searching for locations include locations that are currently part of these Video Link Bookings.",
    example = "[1,3,5]"
  )
  val vlbIdsToExclude: List<Long>,

  @ApiModelProperty(value = "If present find the locations that can be used for the pre interval.", required = false)
  @field:ValidInterval
  val preInterval: Interval?,

  @ApiModelProperty(value = "Find the locations that can be used for the main interval.")
  @field:ValidInterval
  val mainInterval: Interval,

  @ApiModelProperty(value = "If present find the locations that can be used for the post interval", required = false)
  @field:ValidInterval
  val postInterval: Interval?
) {
  fun toAppointmentLocationsSpecification() =
    AppointmentLocationsSpecification(
      date = date,
      agencyId = agencyId,
      vlbIdsToExclude = vlbIdsToExclude,
      appointmentIntervals = listOfNotNull(preInterval, mainInterval, postInterval)
    )
}

/**
 * 'A time interval is the intervening time between two time points.' ISO 8601.
 * Expressed here as the combination of the interval's start and end times.
 *
 * These are closed intervals meaning that the interval includes its limit points.
 */
@ApiModel(description = "A closed time interval, being the intervening time between two time points including the start and end points themselves")
data class Interval(
  @ApiModelProperty(
    value = "The time at which the interval starts, inclusive. ISO-8601 format (hh:mm)",
    example = "09:00"
  )
  val start: LocalTime,

  @ApiModelProperty(
    value = "The time at which the interval end, inclusive. ISO-8601 format (hh:mm)",
    example = "09:30"
  )
  val end: LocalTime
)

@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [IntervalValidator::class])
annotation class ValidInterval(
  val message: String = "start must precede end",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Payload>> = []
)

class IntervalValidator : ConstraintValidator<ValidInterval, Interval> {
  override fun isValid(interval: Interval?, context: ConstraintValidatorContext?) =
    when (interval) {
      null -> true
      else -> interval.start.isBefore(interval.end)
    }
}

/**
 * Internal data class corresponding to AvailableLocations.  This version holds the locations as as
 * a mutable set of Long so that it may be built up over time.
 *
 * TODO: I'd give this package visibility, but I 'm not sure know how to do that in Kotlin.
 */
data class AppointmentIntervalLocations(
  val appointmentInterval: Interval,
  var locationIds: MutableSet<Long>?
) {
  constructor(
    appointmentInterval: Interval,
    vararg locationIds: Long
  ) : this(
    appointmentInterval,
    locationIds.toMutableSet()
  )
}

@ApiModel(description = "A minimal representation of a NOMIS agency internal location.")
data class LocationIdAndDescription(
  @ApiModelProperty(value = "The NOMIS agency internal location identifier of the location", example = "12345")
  val locationId: Long,

  @ApiModelProperty(value = "The NOMIS description of the location", example = "VCC Room 16")
  val description: String
)

/**
 * The set of Locations (as a list) which satisfies some part of an AppointmentLocationSpecification.
 * Each AvailableLocations object corresponds to an appointmentInterval in the specification.
 */
@ApiModel(description = "The set of Locations (as an array) which satisfies some part of an AppointmentLocationSpecification.")
data class AvailableLocations(
  @ApiModelProperty(value = "The appointmentInterval where the locations are available.")
  val appointmentInterval: Interval,

  @ApiModelProperty(value = "The locations that may be booked for the duration of the appointment interval.")
  val locations: List<LocationIdAndDescription>
)

@ApiModel(description = "The sets of Locations that are available for the main, and optionally pre/post Intervals of a VideoLinkBookingLocationsSpecification.")
data class AvailableVideoLinkBookingLocations(
  @ApiModelProperty(
    value = "If present the locations that may be booked for the duration of the pre appointment interval.",
    required = false
  )
  val pre: List<LocationIdAndDescription>?,

  @ApiModelProperty(value = "The locations that may be booked for the duration of the main appointment interval.")
  val main: List<LocationIdAndDescription>,

  @ApiModelProperty(
    value = "If present the locations that may be booked for the duration of the post appointment interval.",
    required = false
  )
  val post: List<LocationIdAndDescription>?
) {
  companion object {
    fun fromAvailableLocations(list: List<AvailableLocations>, hasPreInterval: Boolean) =
      list
        .map { it.locations }
        .let {
          when (it.size) {
            1 -> AvailableVideoLinkBookingLocations(null, it[0], null)

            2 ->
              if (hasPreInterval) {
                AvailableVideoLinkBookingLocations(it[0], it[1], null)
              } else {
                AvailableVideoLinkBookingLocations(null, it[0], it[1])
              }

            3 -> AvailableVideoLinkBookingLocations(it[0], it[1], it[2])

            else -> throw IllegalArgumentException("Should never happen.")
          }
        }
  }
}
