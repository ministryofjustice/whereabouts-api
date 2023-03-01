package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@DataJpaTest
@Transactional
class CourtRepositoryTest(
  @Autowired
  val courtRepository: CourtRepository,
) {

  @Test
  fun `should return courts`() {
    assertThat(courtRepository.findAll()).hasSizeGreaterThan(0)
  }

  @Test
  fun `all courts should have id and name`() {
    assertThat(courtRepository.findAll()).noneMatch { court -> court.id.isNullOrEmpty() }
    assertThat(courtRepository.findAll()).noneMatch { court -> court.name.isNullOrEmpty() }
  }
}
