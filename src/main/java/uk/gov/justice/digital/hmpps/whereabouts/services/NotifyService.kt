package uk.gov.justice.digital.hmpps.whereabouts.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.NotifyRequest
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

@Service
class NotifyService(
  @Value("\${notify.enabled}") private val enabled: Boolean,
  @Value("\${notify.templates.prison.transferred}") private val offenderTransferredPrisonTemplateId: String,
  @Value("\${notify.templates.prison.released}") private val offenderReleasedPrisonTemplateId: String,
  @Value("\${notify.templates.prison.transferred-but-no-court-email}") private val offenderTransferredPrisonButNoCourtEmailTemplateId: String,
  @Value("\${notify.templates.prison.released-but-no-court-email}") private val offenderReleasedPrisonButNoCourtEmailTemplateId: String,
  @Value("\${notify.templates.court.released}") private val offenderReleasedCourtTemplateId: String,
  @Value("\${notify.templates.court.transferred}") private val offenderTransferredCourtTemplateId: String,
  private val client: NotificationClient,
) {

  fun sendOffenderTransferredEmailToCourtAndPrison(
    notifyRequest: NotifyRequest,
    courtEmail: String,
    prisonEmail: String,
  ) {
    if (enabled) {
      val values = notifyRequest.constructMapOfNotifyRequest()
      sendEmail(offenderTransferredPrisonTemplateId, prisonEmail, values, null)
      sendEmail(offenderTransferredCourtTemplateId, courtEmail, values, null)

      log.info(
        "BVL appointment cancellation following offender transfer." +
          "Email sent to prison: $prisonEmail and court $courtEmail" +
          "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
          "offender Noms number: ${values["offenderId"]}, " +
          "BVL booking id: ${values["videoLinkBookingId"]} ",
      )
    }
  }
  fun sendOffenderTransferredEmailToPrisonOnly(notifyRequest: NotifyRequest, prisonEmail: String) {
    if (enabled) {
      val values = notifyRequest.constructMapOfNotifyRequest()
      sendEmail(offenderTransferredPrisonTemplateId, prisonEmail, values, null)

      log.info(
        "BVL appointment cancellation following offender transfer." +
          "Email sent to prison: $prisonEmail" +
          "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
          "offender Noms number: ${values["offenderId"]}, " +
          "BVL booking id: ${values["videoLinkBookingId"]} ",
      )
    }
  }

  fun sendOffenderReleasedEmailToCourtAndPrison(notifyRequest: NotifyRequest, courtEmail: String, prisonEmail: String) {
    if (enabled) {
      val values = notifyRequest.constructMapOfNotifyRequest()
      sendEmail(offenderTransferredPrisonTemplateId, prisonEmail, values, null)
      sendEmail(offenderTransferredCourtTemplateId, courtEmail, values, null)

      log.info(
        "BVL appointment cancellation following offender release." +
          "Email sent to prison: $prisonEmail and court $courtEmail" +
          "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
          "offender Noms number: ${values["offenderId"]}, " +
          "BVL booking id: ${values["videoLinkBookingId"]} ",
      )
    }
  }

  fun sendOffenderReleasedEmailToPrisonOnly(notifyRequest: NotifyRequest, prisonEmail: String) {
    if (enabled) {
      val values = notifyRequest.constructMapOfNotifyRequest()
      sendEmail(offenderTransferredPrisonTemplateId, prisonEmail, values, null)

      log.info(
        "BVL appointment cancellation following offender release." +
          "Email sent to prison: $prisonEmail" +
          "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
          "offender Noms number: ${values["offenderId"]}, " +
          "BVL booking id: ${values["videoLinkBookingId"]} ",
      )
    }
  }

  private fun sendEmail(templateId: String, emailAddress: String, values: Map<String, String?>, reference: String?) {
    if (!enabled) {
      log.info("Notification disabled: Did not send notification to $emailAddress for $templateId ref $reference")
      return
    }

    try {
      client.sendEmail(templateId, emailAddress, values, reference)
    } catch (e: NotificationClientException) {
      log.error("Notification failed - templateId $templateId to $emailAddress", e)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(NotifyService::class.java)
  }
}
