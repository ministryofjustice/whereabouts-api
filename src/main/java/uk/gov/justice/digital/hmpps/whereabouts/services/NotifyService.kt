package uk.gov.justice.digital.hmpps.whereabouts.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.whereabouts.model.NotifyRequest
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

@Service
class NotifyService(
  @Value("\${notify.templates.prison.transferred}") private val offenderTransferredPrisonEmailTemplateId: String,
  @Value("\${notify.templates.court.transferred}") private val offenderTransferredCourtEmailTemplateId: String,
  @Value("\${notify.templates.prison.transferred-but-no-court-email}") private val offenderTransferredPrisonEmailButNoCourtEmailTemplateId: String,
  @Value("\${notify.templates.prison.released}") private val offenderReleasedPrisonEmailTemplateId: String,
  @Value("\${notify.templates.court.released}") private val offenderReleasedCourtEmailTemplateId: String,
  @Value("\${notify.templates.prison.released-but-no-court-email}") private val offenderReleasedPrisonEmailButNoCourtEmailTemplateId: String,

  @Value("\${notify.templates.appointment-canceled.prison}") private val appointmentCanceledPrisonEmailTemplateId: String,
  @Value("\${notify.templates.appointment-canceled.court}") private val appointmentCanceledCourtEmailTemplateId: String,
  @Value("\${notify.templates.appointment-canceled.no-court-email}") private val appointmentCanceledPrisonEmailButNoCourtEmailTemplateId: String,
  private val client: NotificationClient,
) {

  fun sendAppointmentCanceledEmailToCourtAndPrison(
    notifyRequest: NotifyRequest,
    courtEmail: String,
    prisonEmail: String,
  ) {
    val values = notifyRequest.constructMapOfNotifyRequest()
    sendEmail(appointmentCanceledCourtEmailTemplateId, courtEmail, values, null)
    sendEmail(appointmentCanceledPrisonEmailTemplateId, prisonEmail, values, null)

    log.info(
      "BVL appointment cancellation. " +
        "Email sent to prison: $prisonEmail and court $courtEmail" +
        "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
        "offender Noms number: ${values["offenderId"]}, " +
        "BVL booking id: ${values["videoLinkBookingId"]} ",
    )
  }

  fun sendAppointmentCanceledEmailToPrisonOnly(notifyRequest: NotifyRequest, prisonEmail: String) {
    val values = notifyRequest.constructMapOfNotifyRequest()
    sendEmail(appointmentCanceledPrisonEmailButNoCourtEmailTemplateId, prisonEmail, values, null)

    log.info(
      "BVL appointment cancellation. " +
        "Email sent to prison: $prisonEmail" +
        "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
        "offender Noms number: ${values["offenderId"]}, " +
        "BVL booking id: ${values["videoLinkBookingId"]} ",
    )
  }

  fun sendOffenderTransferredEmailToCourtAndPrison(
    notifyRequest: NotifyRequest,
    courtEmail: String,
    prisonEmail: String,
  ) {
    val values = notifyRequest.constructMapOfNotifyRequest()
    sendEmail(offenderTransferredCourtEmailTemplateId, courtEmail, values, null)
    sendEmail(offenderTransferredPrisonEmailTemplateId, prisonEmail, values, null)

    log.info(
      "BVL appointment cancellation following offender transfer. " +
        "Email sent to prison: $prisonEmail and court $courtEmail" +
        "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
        "offender Noms number: ${values["offenderId"]}, " +
        "BVL booking id: ${values["videoLinkBookingId"]} ",
    )
  }
  fun sendOffenderTransferredEmailToPrisonOnly(notifyRequest: NotifyRequest, prisonEmail: String) {
    val values = notifyRequest.constructMapOfNotifyRequest()
    sendEmail(offenderTransferredPrisonEmailButNoCourtEmailTemplateId, prisonEmail, values, null)

    log.info(
      "BVL appointment cancellation following offender transfer. " +
        "Email sent to prison: $prisonEmail" +
        "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
        "offender Noms number: ${values["offenderId"]}, " +
        "BVL booking id: ${values["videoLinkBookingId"]} ",
    )
  }

  fun sendOffenderReleasedEmailToCourtAndPrison(notifyRequest: NotifyRequest, courtEmail: String, prisonEmail: String) {
    val values = notifyRequest.constructMapOfNotifyRequest()
    sendEmail(offenderReleasedPrisonEmailTemplateId, prisonEmail, values, null)
    sendEmail(offenderReleasedCourtEmailTemplateId, courtEmail, values, null)

    log.info(
      "BVL appointment cancellation following offender release. " +
        "Email sent to prison: $prisonEmail and court $courtEmail" +
        "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
        "offender Noms number: ${values["offenderId"]}, " +
        "BVL booking id: ${values["videoLinkBookingId"]} ",
    )
  }

  fun sendOffenderReleasedEmailToPrisonOnly(notifyRequest: NotifyRequest, prisonEmail: String) {
    val values = notifyRequest.constructMapOfNotifyRequest()
    sendEmail(offenderReleasedPrisonEmailButNoCourtEmailTemplateId, prisonEmail, values, null)

    log.info(
      "BVL appointment cancellation following offender release. " +
        "Email sent to prison: $prisonEmail" +
        "Offender name: ${values["firstName"]} ${values["lastName"]}, " +
        "offender Noms number: ${values["offenderId"]}, " +
        "BVL booking id: ${values["videoLinkBookingId"]} ",
    )
  }

  fun sendEmail(templateId: String, emailAddress: String, values: Map<String, String?>, reference: String?) {
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
