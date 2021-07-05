package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.whereabouts.services.locationfinder.LocationAndInterval
import java.time.format.DateTimeFormatter

/**
 * Decorates VideoLinkBookingOptionsService.
 * Emits an Application Insights custom event for each findVideoLinkBookingOptions
 */
@Primary
@Service
class InstrumentedVideoLinkBookingOptionsService(
  private val delegate: VideoLinkBookingOptionsService,
  private val authenticationFacade: AuthenticationFacade,
  private val telemetryClient: TelemetryClient
) : IVideoLinkBookingOptionsService {

  override fun findVideoLinkBookingOptions(specification: VideoLinkBookingSearchSpecification): VideoLinkBookingOptions {
    val result = delegate.findVideoLinkBookingOptions(specification)

    val properties: Map<String, String> = mutableMapOf(
      "user" to authenticationFacade.currentUsername,
      "agencyId" to specification.agencyId,
      "date" to specification.date.format(DateTimeFormatter.ISO_DATE),
      "vlbToExclude" to (specification.vlbIdToExclude?.toString() ?: ""),
      "matched" to result.matched.toString(),
      "alternatives" to result.alternatives.size.toString()
    ) +
      appointmentDetail("main", specification.mainAppointment) +
      (specification.preAppointment?.let { appointmentDetail("pre", it) } ?: emptyMap()) +
      (specification.postAppointment?.let { appointmentDetail("post", it) } ?: emptyMap())

    telemetryClient.trackEvent("findVideoLinkBookingOptions", properties, null)

    return result
  }

  private fun appointmentDetail(prefix: String, locationAndInterval: LocationAndInterval): Map<String, String> =
    mapOf(
      "${prefix}Locationid" to locationAndInterval.locationId.toString(),
      "${prefix}Start" to locationAndInterval.interval.start.toString(),
      "${prefix}End" to locationAndInterval.interval.end.toString(),
    )
}
