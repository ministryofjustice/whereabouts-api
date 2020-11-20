package uk.gov.justice.digital.hmpps.whereabouts.repository

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import uk.gov.justice.digital.hmpps.whereabouts.model.CellMoveReason

@DataJpaTest
class MoveCellRepositoryTest {
  @Autowired
  lateinit var cellMoveRepository: CellMoveReasonRepository

  @Test
  fun `should save without error`() {
    cellMoveRepository.save(
      CellMoveReason(
        bookingId = 1L,
        bedAssignmentsSequence = 2,
        caseNoteId = 3L
      )
    )
  }
}
