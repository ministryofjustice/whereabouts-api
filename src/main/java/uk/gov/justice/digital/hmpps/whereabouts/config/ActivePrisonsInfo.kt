package uk.gov.justice.digital.hmpps.whereabouts.config

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService

@Component
class ActivePrisonsInfo(
  private val disabledPrisonsConfig: DisabledPrisonsConfig,
  private val prisonApiService: PrisonApiService,
) : InfoContributor {
  override fun contribute(builder: Info.Builder?) {
    val prisons = prisonApiService.activeAgencies
    val disabledPrisons = disabledPrisonsConfig.disabledPrisons.split(",")
    builder?.withDetail(
      "activeAgencies",
      prisons.filter { disabledPrisons.contains(it).not() },
    )
  }
}
