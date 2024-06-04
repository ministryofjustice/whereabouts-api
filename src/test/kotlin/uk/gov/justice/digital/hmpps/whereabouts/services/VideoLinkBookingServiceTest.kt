package uk.gov.justice.digital.hmpps.whereabouts.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkAppointmentRepository
import uk.gov.justice.digital.hmpps.whereabouts.repository.VideoLinkBookingRepository
import uk.gov.justice.digital.hmpps.whereabouts.services.court.CourtService
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingEventListener
import uk.gov.justice.digital.hmpps.whereabouts.services.court.VideoLinkBookingService
import java.time.Clock

class VideoLinkBookingServiceTest {

  private val courtService: CourtService = mock()
  private val prisonApiService: PrisonApiService = mock()
  private val prisonApiServiceAuditable: PrisonApiServiceAuditable = mock()
  private val videoLinkAppointmentRepository: VideoLinkAppointmentRepository = mock()
  private val videoLinkBookingRepository: VideoLinkBookingRepository = mock()
  private val clock: Clock = mock()
  private val videoLinkBookingEventListener: VideoLinkBookingEventListener = mock()
  private val notifyService: NotifyService = mock()
  private val prisonRegisterClient: PrisonRegisterClient = mock()

  private val videoLinkBookingService = VideoLinkBookingService(
    false,
    courtService,
    prisonApiService,
    prisonApiServiceAuditable,
    videoLinkAppointmentRepository,
    videoLinkBookingRepository,
    clock,
    videoLinkBookingEventListener,
    notifyService,
    prisonRegisterClient,
  )

  @Test
  fun `getPrisonEmail - happy path`() {
    whenever(
      prisonRegisterClient.getPrisonEmailAddress(
        "NMI",
        VideoLinkBookingService.DepartmentType.VIDEOLINK_CONFERENCING_CENTRE,
      ),
    ).thenReturn(
      PrisonRegisterClient.DepartmentDto("VIDEOLINK_CONFERENCING_CENTRE", "someEmailAddress@gov.uk"),
    )

    Assertions.assertEquals(videoLinkBookingService.getPrisonEmail("NMI"), "someEmailAddress@gov.uk")
  }

  @Test
  fun `getPrisonEmail - should handle not found exception`() {
    whenever(
      prisonRegisterClient.getPrisonEmailAddress(
        any(),
        any(),
      ),
    ).thenThrow(WebClientResponseException.NotFound::class.java)

    Assertions.assertEquals(videoLinkBookingService.getPrisonEmail("NMI"), null)
  }

  @Test
  fun `getPrisonName - happy path`() {
    whenever(
      prisonRegisterClient.getPrisonDetails(
        "NMI",
      ),
    ).thenReturn(
      PrisonRegisterClient.PrisonDetail("NMI", "HMP Nottingham"),
    )

    Assertions.assertEquals(videoLinkBookingService.getPrisonName("NMI"), "HMP Nottingham")
  }

  @Test
  fun `getPrisonName - should handle not found exception`() {
    whenever(
      prisonRegisterClient.getPrisonDetails(
        any(),
      ),
    ).thenThrow(WebClientResponseException.NotFound::class.java)

    Assertions.assertEquals(videoLinkBookingService.getPrisonName("NMI"), null)
  }
}
