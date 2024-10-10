package uk.gov.justice.digital.hmpps.whereabouts.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer

class LocationApiMockServer : WireMockServer(8095)
