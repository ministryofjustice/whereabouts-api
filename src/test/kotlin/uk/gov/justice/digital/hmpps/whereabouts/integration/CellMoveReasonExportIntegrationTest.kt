package uk.gov.justice.digital.hmpps.whereabouts.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.whereabouts.model.CellMoveReason
import uk.gov.justice.digital.hmpps.whereabouts.repository.CellMoveReasonRepository

/**
 * The export that lets hmpps-change-someones-cell-api copy CELL_MOVE_REASON across ahead of this
 * service's decommission.
 */
class CellMoveReasonExportIntegrationTest : IntegrationTest() {

  @Autowired
  private lateinit var cellMoveReasonRepository: CellMoveReasonRepository

  @BeforeEach
  fun seed() {
    cellMoveReasonRepository.deleteAll()
    // Deliberately saved out of key order, and with two sequences on one booking, so the ordering
    // assertions below are real.
    cellMoveReasonRepository.save(CellMoveReason(bookingId = 300, bedAssignmentsSequence = 1, caseNoteId = 33))
    cellMoveReasonRepository.save(CellMoveReason(bookingId = 100, bedAssignmentsSequence = 2, caseNoteId = 12))
    cellMoveReasonRepository.save(CellMoveReason(bookingId = 100, bedAssignmentsSequence = 1, caseNoteId = 11))
    cellMoveReasonRepository.save(CellMoveReason(bookingId = 200, bedAssignmentsSequence = 1, caseNoteId = 21))
  }

  @Test
  fun `requires the sync role`() {
    // 401 not 403: this service's ControllerAdvice has always mapped AccessDeniedException to
    // 401, so a valid token without the role gets the same status as no token. Matched here
    // rather than changed - the export caller treats both as fatal anyway.
    webTestClient.get()
      .uri("/cell/cell-move-reasons")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `exports every row in key order`() {
    webTestClient.get()
      .uri("/cell/cell-move-reasons")
      .headers(setExportHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.cellMoveReasons.length()").isEqualTo(4)
      .jsonPath("$.cellMoveReasons[0].bookingId").isEqualTo(100)
      .jsonPath("$.cellMoveReasons[0].bedAssignmentsSequence").isEqualTo(1)
      .jsonPath("$.cellMoveReasons[1].bookingId").isEqualTo(100)
      .jsonPath("$.cellMoveReasons[1].bedAssignmentsSequence").isEqualTo(2)
      .jsonPath("$.cellMoveReasons[2].bookingId").isEqualTo(200)
      .jsonPath("$.cellMoveReasons[3].bookingId").isEqualTo(300)
  }

  @Test
  fun `resumes from the last key of the previous page`() {
    webTestClient.get()
      .uri("/cell/cell-move-reasons?lastBookingId=100&lastBedAssignmentSequence=1&pageSize=2")
      .headers(setExportHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.cellMoveReasons.length()").isEqualTo(2)
      // Strictly after (100, 1): the same booking's next sequence first, then the next booking.
      .jsonPath("$.cellMoveReasons[0].bookingId").isEqualTo(100)
      .jsonPath("$.cellMoveReasons[0].bedAssignmentsSequence").isEqualTo(2)
      .jsonPath("$.cellMoveReasons[1].bookingId").isEqualTo(200)
  }

  @Test
  fun `an empty page means the export is complete`() {
    webTestClient.get()
      .uri("/cell/cell-move-reasons?lastBookingId=300&lastBedAssignmentSequence=1")
      .headers(setExportHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.cellMoveReasons.length()").isEqualTo(0)
  }

  @Test
  fun `clamps a page size beyond the cap`() {
    webTestClient.get()
      .uri("/cell/cell-move-reasons?pageSize=5000")
      .headers(setExportHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.cellMoveReasons.length()").isEqualTo(4)
  }

  private fun setExportHeaders() = jwtAuthHelper.setAuthorisationHeader(
    username = "HMPPS_CHANGE_SOMEONES_CELL_API",
    roles = listOf("ROLE_CELL_MOVEMENTS__SYNC__RW"),
    clientId = "hmpps-change-someones-cell-api",
  )
}
