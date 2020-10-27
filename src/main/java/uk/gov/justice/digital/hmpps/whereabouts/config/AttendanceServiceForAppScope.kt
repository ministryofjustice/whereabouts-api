package uk.gov.justice.digital.hmpps.whereabouts.config

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceChangesRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import uk.gov.justice.digital.hmpps.whereabouts.services.IEPWarningService
import uk.gov.justice.digital.hmpps.whereabouts.services.NomisEventOutcomeMapper
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService

@Configuration
class AttendanceServiceForAppScope {
  @Bean(name = ["attendanceServiceAppScope"])
  fun getAttendanceServiceForAppScope(
    @Qualifier("elite2WebClientAppScope") webClient: WebClient,
    attendanceRepository: AttendanceRepository,
    attendanceChangesRepository: AttendanceChangesRepository,
    iepWarningService: IEPWarningService,
    nomisEventOutcomeMapper: NomisEventOutcomeMapper,
    telemetryClient: TelemetryClient
  ): AttendanceService {

    return AttendanceService(
      attendanceRepository,
      attendanceChangesRepository,
      PrisonApiService(webClient),
      iepWarningService,
      nomisEventOutcomeMapper,
      telemetryClient
    )
  }
}
