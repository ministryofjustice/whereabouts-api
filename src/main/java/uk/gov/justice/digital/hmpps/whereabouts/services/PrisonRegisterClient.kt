package uk.gov.justice.digital.hmpps.whereabouts.services

import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService

@Component
class PrisonRegisterClient(@Qualifier("prisonRegisterWebClient") private val webClient: WebClient) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun getPrisonEmailAddress(prisonId: String, departmentType: VideoLinkBookingService.DepartmentType): DepartmentDto? {
    return webClient.get()
      .uri("/secure/prisons/id/$prisonId/department/contact-details?departmentType=$departmentType")
      .retrieve()
      .bodyToMono(DepartmentDto::class.java)
      .block()
  }

  fun getPrisonDetails(prisonId: String): PrisonDetail? {
    return webClient.get()
      .uri("/prisons/id/$prisonId")
      .retrieve()
      .bodyToMono(PrisonDetail::class.java)
      .block()
  }

  @Schema(description = "Prison OMU or VCC details")
  data class DepartmentDto(
    val type: String,
    val emailAddress: String,
  )

  @Schema(description = "Prison details")
  data class PrisonDetail(
    val prisonId: String,
    val prisonName: String,
  )
}
