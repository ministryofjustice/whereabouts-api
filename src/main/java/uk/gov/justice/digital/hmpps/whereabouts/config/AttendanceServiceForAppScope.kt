package uk.gov.justice.digital.hmpps.whereabouts.config

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import uk.gov.justice.digital.hmpps.whereabouts.repository.AttendanceRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.AttendanceService
import uk.gov.justice.digital.hmpps.whereabouts.services.Elite2ApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.IEPWarningService
import uk.gov.justice.digital.hmpps.whereabouts.services.NomisEventOutcomeMapper

@Configuration
class AttendanceServiceForAppScope {
  @Bean(name = ["attendanceServiceAppScope"])
  fun getAttendanceServiceForAppScope(
      @Qualifier("elite2ApiRestTemplateAppScope") restTemplate: OAuth2RestTemplate,
      attendanceRepository: AttendanceRepository,
      iepWarningService: IEPWarningService,
      nomisEventOutcomeMapper: NomisEventOutcomeMapper,
      telemetryClient: TelemetryClient
  ): AttendanceService {

    return AttendanceService(attendanceRepository, Elite2ApiService(restTemplate), iepWarningService, nomisEventOutcomeMapper, telemetryClient)
  }
}
