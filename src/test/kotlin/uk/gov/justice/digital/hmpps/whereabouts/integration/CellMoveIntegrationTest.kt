package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class CellMoveIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var clock: Clock

  @BeforeEach
  fun beforeEach() {
    whenever(clock.instant()).thenReturn(Instant.parse(SOME_OCCURRENCE_DATE_TIME + "Z"))
    whenever(clock.zone).thenReturn(ZoneId.of("UTC"))
  }

  @Test
  fun `make a request to change an offenders cell`() {
    stubCellMove()

    webTestClient.post()
      .uri("/cell/make-cell-move")
      .bodyValue(
        mapOf(
          "bookingId" to SOME_BOOKING_ID,
          "offenderNo" to SOME_OFFENDER_NO,
          "internalLocationDescriptionDestination" to SOME_INTERNAL_LOCATION_DESCRIPTION,
          "cellMoveReasonCode" to SOME_REASON_CODE,
          "commentText" to SOME_TEXT,
        ),
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus().isCreated
      .expectBody()
      .json(loadJsonFile("cell-move-details.json"))

    prisonApiMockServer.verify(
      putRequestedFor(
        urlEqualTo("/api/bookings/$SOME_BOOKING_ID/living-unit/$SOME_INTERNAL_LOCATION_DESCRIPTION?reasonCode=$SOME_REASON_CODE"),
      ),
    )
  }

  @Test
  fun `raise a case note`() {
    stubCellMove()

    webTestClient.post()
      .uri("/cell/make-cell-move")
      .bodyValue(
        mapOf(
          "bookingId" to SOME_BOOKING_ID,
          "offenderNo" to SOME_OFFENDER_NO,
          "internalLocationDescriptionDestination" to SOME_INTERNAL_LOCATION_DESCRIPTION,
          "cellMoveReasonCode" to SOME_REASON_CODE,
          "commentText" to SOME_TEXT,
        ),
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus().isCreated

    caseNotesMockServer.verify(
      postRequestedFor(
        urlEqualTo("/case-notes/$SOME_OFFENDER_NO"),
      ).withRequestBody(
        equalToJson(
          objectMapper.writeValueAsString(
            mapOf(
              "type" to "MOVED_CELL",
              "subType" to "ADM",
              "occurrenceDateTime" to "2020-10-03T20:00",
              "text" to SOME_TEXT,
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `should return cell move details`() {
    stubCellMove()

    webTestClient.post()
      .uri("/cell/make-cell-move")
      .bodyValue(
        mapOf(
          "bookingId" to SOME_BOOKING_ID,
          "offenderNo" to SOME_OFFENDER_NO,
          "internalLocationDescriptionDestination" to SOME_INTERNAL_LOCATION_DESCRIPTION,
          "cellMoveReasonCode" to SOME_REASON_CODE,
          "commentText" to SOME_TEXT,
        ),
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus().isCreated()

    webTestClient.get()
      .uri("/cell/cell-move-reason/booking/$SOME_BOOKING_ID/bed-assignment-sequence/$SOME_ASSIGNMENT_LIVING_UNIT_ID")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(loadJsonFile("cell-move-reason.json"))
  }

  private fun stubCellMove() {
    prisonApiMockServer.stubMakeCellMove(
      bookingId = SOME_BOOKING_ID,
      agencyId = SOME_AGENCY_ID,
      assignedLivingUnitId = SOME_ASSIGNMENT_LIVING_UNIT_ID,
      internalLocationDescription = SOME_INTERNAL_LOCATION_DESCRIPTION,
      bedAssignmentHistorySequence = SOME_BED_ASSIGNMENT_SEQUENCE,
    )
    caseNotesMockServer.stubCreateCaseNote(SOME_OFFENDER_NO, SOME_CASE_NOTE_ID)
  }

  companion object {
    private const val SOME_BOOKING_ID = 10L
    private const val SOME_AGENCY_ID = "MDI"
    private const val SOME_OFFENDER_NO = "A12345"
    private const val SOME_ASSIGNMENT_LIVING_UNIT_ID = 2L
    private const val SOME_INTERNAL_LOCATION_DESCRIPTION = "MDI-2-2-006"
    private const val SOME_REASON_CODE = "ADM"
    private const val SOME_TEXT = "Some comment defending the reason of move"
    private const val SOME_OCCURRENCE_DATE_TIME = "2020-10-03T20:00:00"
    private const val SOME_BED_ASSIGNMENT_SEQUENCE = 2
    private const val SOME_CASE_NOTE_ID = 100L
  }
}
