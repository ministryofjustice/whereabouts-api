package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.LocalTime
import kotlin.reflect.KClass

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
