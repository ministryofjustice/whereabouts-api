package uk.gov.justice.digital.hmpps.whereabouts.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.boot.info.BuildProperties
import java.util.*

class HealthInfoTest {
    @Test
    fun shouldIncludeVersionInfo() {
        val properties = Properties()
        properties.setProperty("version", "somever")
        assertThat(HealthInfo(BuildProperties(properties)).health().details).isEqualTo(mapOf("version" to "somever"))
    }
}
