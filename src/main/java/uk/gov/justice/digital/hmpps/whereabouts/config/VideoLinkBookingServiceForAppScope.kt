package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.NotifyService
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiServiceAuditable
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonRegisterClient
import uk.gov.justice.digital.hmpps.whereabouts.services.court.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingEventListener
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import java.time.Clock

@Configuration
class VideoLinkBookingServiceForAppScope(
  @Value("\${notify.enabled}") private val enabled: Boolean,
) {
  @Bean(name = ["videoLinkBookingServiceAppScope"])
  fun getPrisonServiceForAppScope(
    courtService: CourtService,
    @Qualifier("elite2WebClientAppScope") webClient: WebClient,
    prisonApiServiceAuditable: PrisonApiServiceAuditable,
    videoLinkAppointmentRepository: VideoLinkAppointmentRepository,
    videoLinkBookingRepository: VideoLinkBookingRepository,
    clock: Clock,
    videoLinkBookingEventListener: VideoLinkBookingEventListener,
    notifyService: NotifyService,
    prisonRegisterClient: PrisonRegisterClient,
    @Qualifier("prisonRegisterClientAppScope") prisonRegisterWebClient: WebClient,
  ): VideoLinkBookingService {
    return VideoLinkBookingService(
      enabled,
      courtService,
      PrisonApiService(webClient),
      prisonApiServiceAuditable,
      videoLinkAppointmentRepository,
      videoLinkBookingRepository,
      clock,
      videoLinkBookingEventListener,
      notifyService,
      PrisonRegisterClient(prisonRegisterWebClient),
    )
  }
}
