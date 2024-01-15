package uk.gov.justice.digital.hmpps.whereabouts.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.whereabouts.model.NotifyRequest
import uk.gov.service.notify.NotificationClient

class NotifyServiceTest {
  private val notifyClient: NotificationClient = mock()
  private val notifyRequest: NotifyRequest = mock()
  private val PRISON_EMAIL_ADDRESS = "some-prison@mail.com"
  private val COURT_EMAIL_ADDRESS = "some-court@mail.com"
  val map = mapOf<String, String>()

  private val service = NotifyService(
    "offenderTransferredPrisonEmailTemplateId",
    "offenderTransferredCourtEmailTemplateId",
    "offenderTransferredPrisonEmailTemplateIdButNoCourtEmailTemplateId",
    "offenderReleasedPrisonEmailTemplateId",
    "offenderReleasedCourtEmailTemplateId",
    "offenderReleasedPrisonEmailTemplateIdButNoCourtEmailTemplateId",
    client = notifyClient,
  )

  @BeforeEach
  fun beforeEach() {
    whenever(notifyRequest.constructMapOfNotifyRequest()).thenReturn(map)
  }

  @Test
  fun `should send email to prison and court for offender transfer`() {
    service.sendOffenderTransferredEmailToCourtAndPrison(
      notifyRequest,
      COURT_EMAIL_ADDRESS,
      PRISON_EMAIL_ADDRESS,
    )

    verify(notifyClient).sendEmail(
      eq("offenderTransferredCourtEmailTemplateId"),
      eq(COURT_EMAIL_ADDRESS),
      eq(map),
      eq(null),
    )
    verify(notifyClient).sendEmail(
      eq("offenderTransferredPrisonEmailTemplateId"),
      eq(PRISON_EMAIL_ADDRESS),
      eq(map),
      eq(null),
    )
  }

  @Test
  fun `should send email only prison only for offender transfer`() {
    service.sendOffenderTransferredEmailToPrisonOnly(
      notifyRequest,
      PRISON_EMAIL_ADDRESS,
    )

    verify(notifyClient).sendEmail(
      eq("offenderTransferredPrisonEmailTemplateIdButNoCourtEmailTemplateId"),
      eq(PRISON_EMAIL_ADDRESS),
      eq(map),
      eq(null),
    )
  }

  @Test
  fun `should send email to court and prison for offender release`() {
    service.sendOffenderReleasedEmailToCourtAndPrison(
      notifyRequest,
      COURT_EMAIL_ADDRESS,
      PRISON_EMAIL_ADDRESS,
    )

    verify(notifyClient).sendEmail(
      eq("offenderReleasedCourtEmailTemplateId"),
      eq(COURT_EMAIL_ADDRESS),
      eq(map),
      eq(null),
    )

    verify(notifyClient).sendEmail(
      eq("offenderReleasedPrisonEmailTemplateId"),
      eq(PRISON_EMAIL_ADDRESS),
      eq(map),
      eq(null),
    )
  }

  @Test
  fun `should send email only prison only for offender release`() {
    service.sendOffenderReleasedEmailToPrisonOnly(
      notifyRequest,
      PRISON_EMAIL_ADDRESS,
    )

    verify(notifyClient).sendEmail(
      eq("offenderReleasedPrisonEmailTemplateIdButNoCourtEmailTemplateId"),
      eq(PRISON_EMAIL_ADDRESS),
      eq(map),
      eq(null),
    )
  }
}
