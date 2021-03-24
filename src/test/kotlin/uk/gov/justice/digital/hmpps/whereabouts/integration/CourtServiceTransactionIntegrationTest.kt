package uk.gov.justice.digital.hmpps.whereabouts.integration

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.jdbc.JdbcTestUtils
import uk.gov.justice.digital.hmpps.whereabouts.dto.Event
import uk.gov.justice.digital.hmpps.whereabouts.model.PrisonAppointment
import uk.gov.justice.digital.hmpps.whereabouts.services.PrisonApiService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.ApplicationInsightsEventListener
import uk.gov.justice.digital.hmpps.whereabouts.services.court.CourtServiceTest
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Moving the ApplicationInsightsEventHandler outside the transaction boundary resulted in a bug.
 * The 'transactional' part of the create, update and delete operations, expressed as functions annotated
 * with @Transactional was not in fact transactional.
 *
 * This happened because the 'transactional' methods were called from other methods within the service.
 * Because the methods were called from within the service the thread of control did not pass through the Spring
 * proxy which wraps the service and so the transactional and JPA session behaviour wasn't provided. Outcome:
 * the update failed to update the database tables so that the VLB referred to NOMIS appointments that
 * did not exist.
 *
 * This test exposes that problem and shows that the bug fix works.  To prove this comment out the @Transactional
 * annotation on TransactionHandler#runInTransaction. This test then fails.
 */
class CourtServiceTransactionIntegrationTest : IntegrationTest() {

  @MockBean
  lateinit var prisonApiService: PrisonApiService

  @MockBean
  lateinit var applicationInsightsEventListener: ApplicationInsightsEventListener

  @Autowired
  lateinit var clock: Clock

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate

  @BeforeEach
  fun clearDatabase() {
    JdbcTestUtils.deleteFromTables(
      jdbcTemplate,
      "VIDEO_LINK_BOOKING",
      "VIDEO_LINK_APPOINTMENT",
      "VIDEO_LINK_BOOKING_EVENT"
    )
  }

  @Test
  fun `verify update happens`() {
    val referenceTime = LocalDateTime
      .now(clock)
      .plusDays(8)
      .plusHours(10)
      .plusMinutes(30)

    whenever(prisonApiService.getLocation(ArgumentMatchers.anyLong()))
      .thenAnswer {
        CourtServiceTest.locationDto(it.arguments[0] as Long)
      }

    whenever(prisonApiService.postAppointment(ArgumentMatchers.anyLong(), any())).thenReturn(Event(20L, "WWI"))

    val createResult = webTestClient.post()
      .uri("/court/video-link-bookings")
      .headers(setHeaders())
      .bodyValue(
        mapOf(
          "bookingId" to 1000L,
          "court" to "Test Court 1",
          "madeByTheCourt" to false,
          "main" to mapOf(
            "locationId" to 1,
            "startTime" to referenceTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "endTime" to referenceTime.plusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
          )
        )
      )
      .exchange()
      .expectStatus().isCreated
      .expectBody()
      .returnResult()

    val videoLinkBookingId = String(createResult.responseBody).toLong()

    whenever(prisonApiService.getPrisonAppointment(any())).thenAnswer {
      PrisonAppointment(
        agencyId = "WWI",
        bookingId = -1L,
        startTime = referenceTime,
        endTime = referenceTime.plusMinutes(1),
        eventId = it.arguments[0] as Long,
        eventSubType = "meh",
        eventLocationId = it.arguments[0] as Long * 10,
        comment = "XXX${it.arguments[0] as Long}"
      )
    }

    webTestClient.get()
      .uri("/court/video-link-bookings/$videoLinkBookingId")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """
          {
            "videoLinkBookingId": 1,
            "bookingId": 1000,
            "agencyId" : "WWI",
            "court": "Test Court 1",
            "comment": "XXX20",
            "main": {
              "locationId": 200,
              "startTime": "${referenceTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
              "endTime": "${referenceTime.plusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            }
          }
        """
      )

    whenever(prisonApiService.postAppointment(ArgumentMatchers.anyLong(), any())).thenReturn(Event(21L, "WWI"))

    webTestClient.put()
      .uri("/court/video-link-bookings/$videoLinkBookingId")
      .bodyValue(
        """
              {
                "comment": "New comment",
                "madeByTheCourt": false,
                "main": {
                  "locationId" : 2,
                  "startTime" : "${referenceTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
                  "endTime": "${referenceTime.plusMinutes(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
                }
              }
            """
      )
      .headers(setHeaders())
      .exchange()
      .expectStatus().isNoContent

    webTestClient.get()
      .uri("/court/video-link-bookings/$videoLinkBookingId")
      .headers(setHeaders())
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """
          {
            "videoLinkBookingId": 1,
            "bookingId": 1000,
            "agencyId" : "WWI",
            "court": "Test Court 1",
            "comment": "XXX21",
            "main": {
              "locationId": 210,
              "startTime": "${referenceTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
              "endTime": "${referenceTime.plusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            }
          }
        """
      )
  }
}
