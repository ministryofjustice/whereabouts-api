package uk.gov.justice.digital.hmpps.whereabouts.controllers

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint
import org.springframework.context.annotation.Configuration

import org.springframework.http.MediaType.TEXT_PLAIN_VALUE

@Configuration
@WebEndpoint(id = "ping")
class PingEndpoint {

  @ReadOperation(produces = [TEXT_PLAIN_VALUE])
  fun ping(): String = "pong"
}
