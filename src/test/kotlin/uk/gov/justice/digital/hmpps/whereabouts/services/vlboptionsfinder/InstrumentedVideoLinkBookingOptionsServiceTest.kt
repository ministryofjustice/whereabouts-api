package uk.gov.justice.digital.hmpps.whereabouts.services.vlboptionsfinder

import com.microsoft.applicationinsights.TelemetryClient
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthenticationFacade
import java.time.LocalDate
import java.time.LocalTime

class InstrumentedVideoLinkBookingOptionsServiceTest {
  private val delegate: VideoLinkBookingOptionsService = mockk()
  private val authenticationFacade: AuthenticationFacade = mockk()
  private val telemetryClient: TelemetryClient = mockk()

  private val service = InstrumentedVideoLinkBookingOptionsService(delegate, authenticationFacade, telemetryClient)

  @BeforeEach
  fun initialiseTest() {
    every { authenticationFacade.currentUsername } returns "user"
    every { telemetryClient.trackEvent(any(), any(), isNull()) } just Runs
  }

  @Test
  fun `Pre, main and post appointments, not matched, one alternative`() {
    val expectedResult = VideoLinkBookingOptions(
      matched = false,
      alternatives = listOf(
        VideoLinkBookingOption(
          pre = LocationAndInterval(1L, Interval(LocalTime.of(12, 0), LocalTime.of(12, 30))),
          main = LocationAndInterval(2L, Interval(LocalTime.of(12, 30), LocalTime.of(13, 0))),
          post = LocationAndInterval(3L, Interval(LocalTime.of(13, 0), LocalTime.of(13, 30)))
        )
      )
    )

    every { delegate.findVideoLinkBookingOptions(any()) } returns expectedResult

    val result = service.findVideoLinkBookingOptions(
      VideoLinkBookingSearchSpecification(
        agencyId = "WWI",
        date = LocalDate.of(2020, 6, 1),
        preAppointment = LocationAndInterval(1L, Interval(LocalTime.of(9, 30), LocalTime.of(10, 0))),
        mainAppointment = LocationAndInterval(2L, Interval(LocalTime.of(10, 0), LocalTime.of(10, 30))),
        postAppointment = LocationAndInterval(3L, Interval(LocalTime.of(10, 30), LocalTime.of(11, 0))),
        vlbIdToExclude = 10L
      )
    )

    assertThat(result).isEqualTo(expectedResult)

    verify {
      telemetryClient.trackEvent(
        "findVideoLinkBookingOptions",
        mutableMapOf(
          "user" to "user",
          "agencyId" to "WWI",
          "date" to "2020-06-01",
          "vlbToExclude" to "10",
          "matched" to "false",
          "alternativesCount" to "1",
          "alternativeMainStartTimes" to "12:30",
          "preLocationId" to "1",
          "preStart" to "09:30",
          "preEnd" to "10:00",
          "mainLocationId" to "2",
          "mainStart" to "10:00",
          "mainEnd" to "10:30",
          "postLocationId" to "3",
          "postStart" to "10:30",
          "postEnd" to "11:00"
        ),
        null
      )
    }
  }

  @Test
  fun `Main appointment only, matched`() {
    val expectedResult = VideoLinkBookingOptions(matched = true, alternatives = listOf())

    every { delegate.findVideoLinkBookingOptions(any()) } returns expectedResult

    val result = service.findVideoLinkBookingOptions(
      VideoLinkBookingSearchSpecification(
        agencyId = "WWI",
        date = LocalDate.of(2020, 6, 1),
        mainAppointment = LocationAndInterval(2L, Interval(LocalTime.of(10, 0), LocalTime.of(10, 30)))
      )
    )

    assertThat(result).isEqualTo(expectedResult)

    verify {
      telemetryClient.trackEvent(
        "findVideoLinkBookingOptions",
        mutableMapOf(
          "user" to "user",
          "agencyId" to "WWI",
          "date" to "2020-06-01",
          "vlbToExclude" to "",
          "matched" to "true",
          "alternativesCount" to "0",
          "alternativeMainStartTimes" to "",
          "mainLocationId" to "2",
          "mainStart" to "10:00",
          "mainEnd" to "10:30"
        ),
        null
      )
    }
  }

  @Test
  fun `Main appointment only, not matched, 2 alternatives`() {
    val expectedResult = VideoLinkBookingOptions(
      matched = false,
      alternatives = listOf(
        VideoLinkBookingOption(main = LocationAndInterval(2L, Interval(LocalTime.of(12, 30), LocalTime.of(13, 0)))),
        VideoLinkBookingOption(main = LocationAndInterval(2L, Interval(LocalTime.of(13, 30), LocalTime.of(14, 0))))
      )
    )

    every { delegate.findVideoLinkBookingOptions(any()) } returns expectedResult

    val result = service.findVideoLinkBookingOptions(
      VideoLinkBookingSearchSpecification(
        agencyId = "WWI",
        date = LocalDate.of(2020, 6, 1),
        mainAppointment = LocationAndInterval(2L, Interval(LocalTime.of(10, 0), LocalTime.of(10, 30)))
      )
    )

    assertThat(result).isEqualTo(expectedResult)

    verify {
      telemetryClient.trackEvent(
        "findVideoLinkBookingOptions",
        mutableMapOf(
          "user" to "user",
          "agencyId" to "WWI",
          "date" to "2020-06-01",
          "vlbToExclude" to "",
          "matched" to "false",
          "alternativesCount" to "2",
          "alternativeMainStartTimes" to "12:30,13:30",
          "mainLocationId" to "2",
          "mainStart" to "10:00",
          "mainEnd" to "10:30"
        ),
        null
      )
    }
  }
}
