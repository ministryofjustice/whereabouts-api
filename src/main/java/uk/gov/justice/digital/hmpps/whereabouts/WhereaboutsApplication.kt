package uk.gov.justice.digital.hmpps.whereabouts

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class WhereaboutsApplication

fun main(args: Array<String>) {
  runApplication<WhereaboutsApplication>(*args)
}
