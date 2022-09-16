package uk.gov.justice.digital.hmpps.whereabouts.config

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
    prisonApiService: PrisonApiService,
    attendanceRepository: AttendanceRepository,
    attendanceChangesRepository: AttendanceChangesRepository,
    iepWarningService: IEPWarningService,
    nomisEventOutcomeMapper: NomisEventOutcomeMapper,
    telemetryClient: TelemetryClient
  ): AttendanceService {
    return AttendanceService(
      attendanceRepository,
      attendanceChangesRepository,
      prisonApiService,
      iepWarningService,
      nomisEventOutcomeMapper,
      telemetryClient
    )
  }
}
