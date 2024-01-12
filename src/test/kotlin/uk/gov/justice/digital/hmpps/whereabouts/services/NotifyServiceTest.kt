package uk.gov.justice.digital.hmpps.whereabouts.services

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.whereabouts.model.NotifyRequest
import uk.gov.service.notify.NotificationClient

class NotifyServiceTest {
  private val notifyClient: NotificationClient = mock()
  private val notifyRequest: NotifyRequest = mock()

  private companion object {
    const val PRISON_EMAIL_ADDRESS = "some-prison@mail.com"
    const val COURT_EMAIL_ADDRESS = "some-court@mail.com"
  }

  private val service = NotifyService(
    enabled = true,
    "offenderTransferredPrisonEmailTemplateId",
    "offenderTransferredCourtEmailTemplateId",
    "offenderTransferredPrisonEmailTemplateIdButNoCourtEmailTemplateId",
    "offenderReleasedPrisonEmailTemplateId",
    "offenderReleasedCourtEmailTemplateId",
    "offenderReleasedPrisonEmailTemplateIdButNoCourtEmailTemplateId",
    client = notifyClient,
  )

  @Test
  fun `should send email to prison and court for offender transfer`() {
    whenever(notifyRequest.constructMapOfNotifyRequest()).thenReturn(mapOf())
    service.sendOffenderTransferredEmailToCourtAndPrison(
      notifyRequest,
      PRISON_EMAIL_ADDRESS,
      COURT_EMAIL_ADDRESS,
    )

    verify(notifyClient).sendEmail(eq("offenderTransferredPrisonEmailTemplateId"), eq(COURT_EMAIL_ADDRESS), anyMap(), eq(null))
    verify(notifyClient).sendEmail(eq("offenderTransferredPrisonEmailTemplateId"), eq(PRISON_EMAIL_ADDRESS), any(), eq(null))
  }

  @Test
  fun `should send email only prison only for offender transfer`() {
  }

  @Test
  fun `should send email to prison and court for offender release`() {
  }

  @Test
  fun `should send email only prison only for offender release`() {
  }
}
