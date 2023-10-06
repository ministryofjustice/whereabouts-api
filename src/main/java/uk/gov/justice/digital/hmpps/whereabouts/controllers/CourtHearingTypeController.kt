package uk.gov.justice.digital.hmpps.whereabouts.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.whereabouts.model.CourtHearingType

@Tag(name = "court")
@RestController
@RequestMapping(value = ["court"], produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasAnyRole('MAINTAIN_WHEREABOUTS')")
class CourtHearingTypeController() {
  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["/hearing-type"])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
    description = "Get court hearing types",
    summary = "Return all court hearing types",
  )
  fun getCourtHearingTypes() = CourtHearingType.values()
}
