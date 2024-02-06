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
  private val prisonEmailAddress = "some-prison@mail.com"
  private val courtEmailAddress = "some-court@mail.com"
  val map = mapOf<String, String>()

  private val service = NotifyService(
    "offenderTransferredPrisonEmailTemplateId",
    "offenderTransferredCourtEmailTemplateId",
    "offenderTransferredPrisonEmailTemplateIdButNoCourtEmailTemplateId",
    "offenderReleasedPrisonEmailTemplateId",
    "offenderReleasedCourtEmailTemplateId",
    "offenderReleasedPrisonEmailTemplateIdButNoCourtEmailTemplateId",
    "appointmentCanceledPrisonEmailTemplateId",
    "appointmentCanceledCourtEmailTemplateId",
    "appointmentCanceledPrisonEmailButNoCourtEmailTemplateId",
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
      courtEmailAddress,
      prisonEmailAddress,
    )

    verify(notifyClient).sendEmail(
      eq("offenderTransferredCourtEmailTemplateId"),
      eq(courtEmailAddress),
      eq(map),
      eq(null),
    )
    verify(notifyClient).sendEmail(
      eq("offenderTransferredPrisonEmailTemplateId"),
      eq(prisonEmailAddress),
      eq(map),
      eq(null),
    )
  }

  @Test
  fun `should send email only prison only for offender transfer`() {
    service.sendOffenderTransferredEmailToPrisonOnly(
      notifyRequest,
      prisonEmailAddress,
    )

    verify(notifyClient).sendEmail(
      eq("offenderTransferredPrisonEmailTemplateIdButNoCourtEmailTemplateId"),
      eq(prisonEmailAddress),
      eq(map),
      eq(null),
    )
  }

  @Test
  fun `should send email to court and prison for offender release`() {
    service.sendOffenderReleasedEmailToCourtAndPrison(
      notifyRequest,
      courtEmailAddress,
      prisonEmailAddress,
    )

    verify(notifyClient).sendEmail(
      eq("offenderReleasedCourtEmailTemplateId"),
      eq(courtEmailAddress),
      eq(map),
      eq(null),
    )

    verify(notifyClient).sendEmail(
      eq("offenderReleasedPrisonEmailTemplateId"),
      eq(prisonEmailAddress),
      eq(map),
      eq(null),
    )
  }

  @Test
  fun `should send email to prison only for offender release`() {
    service.sendOffenderReleasedEmailToPrisonOnly(
      notifyRequest,
      prisonEmailAddress,
    )

    verify(notifyClient).sendEmail(
      eq("offenderReleasedPrisonEmailTemplateIdButNoCourtEmailTemplateId"),
      eq(prisonEmailAddress),
      eq(map),
      eq(null),
    )
  }

  @Test
  fun `should send email to prison and court when appointment changed`() {
    service.sendAppointmentCanceledEmailToCourtAndPrison(
      notifyRequest,
      courtEmailAddress,
      prisonEmailAddress,
    )

    verify(notifyClient).sendEmail(
      eq("appointmentCanceledCourtEmailTemplateId"),
      eq(courtEmailAddress),
      eq(map),
      eq(null),
    )
    verify(notifyClient).sendEmail(
      eq("appointmentCanceledPrisonEmailTemplateId"),
      eq(prisonEmailAddress),
      eq(map),
      eq(null),
    )
  }

  @Test
  fun `should send email to prison only when appointment changed and court email not found`() {
    service.sendAppointmentCanceledEmailToPrisonOnly(
      notifyRequest,
      prisonEmailAddress,
    )

    verify(notifyClient).sendEmail(
      eq("appointmentCanceledPrisonEmailButNoCourtEmailTemplateId"),
      eq(prisonEmailAddress),
      eq(map),
      eq(null),
    )
  }
}
